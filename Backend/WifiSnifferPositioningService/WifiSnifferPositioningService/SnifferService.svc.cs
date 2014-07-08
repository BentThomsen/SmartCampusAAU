/*
Copyright (c) 2014, Aalborg University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

using System;
using System.Collections.Generic;
using System.Data.Services;
using System.Data.Services.Common;
using System.Linq;
using System.ServiceModel.Web;
using System.Web;
using WifiSnifferPositioningService.radiomap;
using WifiSnifferPositioningService.extensions.wifi;
using WifiSnifferPositioningService.location.wifi;
using System.Collections.Concurrent;
using System.Data.Services.Client;
using System.Net.Sockets;
using System.Net;
using System.Text;
using System.Threading;
using System.Runtime.CompilerServices;
using System.IO;

namespace WifiSnifferPositioningService
{
    public class SnifferService : DataService< SnifferModel >
    {
        private static int Count { get; set; }

        // This method is called only once to initialize service-wide policies.
        public static void InitializeService(DataServiceConfiguration config)
        {
            // TODO: set rules to indicate which entity sets and service operations are visible, updatable, etc.
            // Examples:
            config.SetEntitySetAccessRule("PositionEstimates", EntitySetRights.AllRead);
            
            config.SetServiceOperationAccessRule("*", ServiceOperationRights.All);
            
            config.DataServiceBehavior.MaxProtocolVersion = DataServiceProtocolVersion.V2;

            new Thread(new ThreadStart(StartTcpServer)).Start();
        }
        
        public SnifferService()
            : base()
        {
            //Yep - the constructor gets called on each request
            Count++;
        }
              
        /// <summary>
        /// A client requests a PositionEstimate. 
        /// Precondition: A client must have called StartWifiPositioning(clientMac) before requesting a position.
        /// </summary>
        /// <param name="clientMac"></param>
        /// <returns></returns>
        [WebGet]
        public PositionEstimate GetPosition(string clientMac)
        {
            /**
             * [WebGet] = SnifferReceiver operation (cf: http://msdn.microsoft.com/en-us/library/cc668788.aspx )
             * usage example: http://localhost:38244/SnifferService.svc/GetPosition?mac='Big Mac'
             */

            if (clientMac == null)
                return null;

            SnifferWifiMeasurement curMeas;
            if (!(currentOnlineMeasurements.TryGetValue(clientMac, out curMeas)))
            {
                return null; //we do not have a measurement yet to base an estimate on
            }

            //Check whether the client is associated with a building
            //If not, positioning has (JUST) been requested, but the correct building not established yet.
            WifiPosEngine posEngine;
            if (!(currentWifiPosEngines.TryGetValue(clientMac, out posEngine)))
            {
                return null; //we do not have a wifi pos engine. Reason: User has probably not called StartWifiPositioning. 
            }

            if (posEngine.getCurrentBuilding() == null) //It is the very first estimate
            {
                posEngine.setCurrentBuilding(getCorrectBuilding(curMeas));
            }

            EstimateResult currentEstimate = posEngine.getEstimate(curMeas);

            //We have a result
            if (currentEstimate != null && currentEstimate.getVertex() != null)
            {
                //convert estimated location to PositionEstimate
                PositionEstimate result = new PositionEstimate();
                Vertex v = currentEstimate.getVertex();
                AbsoluteLocation a = v.AbsoluteLocations.First();

                result.VertexID = v.ID;
                result.Building_ID = v.Building_ID;
                result.Latitude = (double)a.latitude;
                result.Longitude = (double)a.longitude;
                result.Altitude = (double)a.longitude;
                result.Accuracy = currentEstimate.getErrorEstimate();
                result.HasAccuracy = true;
                result.HasBearing = false;
                result.HasSpeed = false;
                result.Provider = WIFI_PROVIDER;
                result.Time = DateTime.Now;

                return result;
            }
            else //We do not have a result
            {
                return null;
            }
        }
        
        //Is annotated to all PositionEstimates. Currently, we only ever return Wi-Fi estimates from the SnifferService
        private const string WIFI_PROVIDER = "Wi-Fi";

        private static bool _isDownloadingRadioMap;
        public static bool IsDownloadingRadioMap
        {
            [MethodImpl(MethodImplOptions.Synchronized)]
            get { return _isDownloadingRadioMap; }
            [MethodImpl(MethodImplOptions.Synchronized)]
            set { _isDownloadingRadioMap = value; }
        }

        private static radiomapEntities context;
        private static readonly Uri radiomapUri =
            new Uri("http://smartcampusaau.cs.aau.dk/RadioMapService3/RadioMapService.svc/");

        private static List<Building> LoadedBuildings;

        #region 'Current' data structures for offline clients
        //Offline measurements currently being built for clients (who are identified by their mac adress)
        private static ConcurrentDictionary<string, SnifferWifiMeasurement> currentOfflineMeasurements = new ConcurrentDictionary<string, SnifferWifiMeasurement>();

        //mac adress and of all clients that are currently "measuring" signal strengths together with the vertex (location) where they are measuring. 
        private static ConcurrentDictionary<string, Vertex> currentMeasuringClients = new ConcurrentDictionary<string, Vertex>();

        //Measurements that have been concluded, but not yet saved in the database
        //Purpose: To enable calling StopMeasurement and SaveMeasurement in two distinct steps. 
        //When a measurement is done it is moved from currentOfflineMeasurements to savedOfflineMeasurements
        private static Dictionary<string, SnifferWifiMeasurement> savedOfflineMeasurements = new Dictionary<string, SnifferWifiMeasurement>();

        //mac adress and of all clients that have a 'saved' measurement (i.e., one that has been successfully started and stopped but not yet saved in the db) 
        private static Dictionary<string, Vertex> savedMeasuringClients = new Dictionary<string, Vertex>();
        
        #endregion

        #region 'Current' data structures for online clients
        //mac address of all clients that are currently being positioned together with the vertex (location) they were last estimated to be at. 
        //NOTE: Consider saving their history instead of just the last location
        private static ConcurrentDictionary<string, Vertex> currentPositioningClients = new ConcurrentDictionary<string, Vertex>();

        //A list of clients and the building they are currently in. 
        //Current purpose: to define primary- and secondary search space in positioning, thus mainly of use to online clients. 
        private static ConcurrentDictionary<string, Building> currentClient_Buildings = new ConcurrentDictionary<string, Building>();

        //Online measurements currently being built for clients (who are identified by their mac adress)
        //NB: The only reason we use two datastructures is that we do not want the context to track changes in online fingerprints (at least at the moment)
        private static ConcurrentDictionary<string, SnifferWifiMeasurement> currentOnlineMeasurements = new ConcurrentDictionary<string, SnifferWifiMeasurement>();
        
        //WifiPosEngines used to improve accuracy on a per-client basis
        private static ConcurrentDictionary<string, WifiPosEngine> currentWifiPosEngines = new ConcurrentDictionary<string, WifiPosEngine>();
        #endregion

        #region Data structures for clients that have requested positioning
        #endregion

        private enum Mode { Online, Offline }

        private static void addToMeasurement(string snifferMac, string clientMac, int rssi)
        {
            addToMeasurement(snifferMac, clientMac, rssi, DateTime.Now);
        }

        /// <summary>
        /// A sniffer entry (sniffer-mac, client-mac, rssi, and time) is added to currently built measurements. 
        /// The procedures is the same for online and offline measurements - the only difference is that we do not track changes to 
        /// online measurements as we are not interested in saving them persistently (at least not at the present time)
        /// </summary>
        /// <param name="snifferMac">The sniffer's mac address</param>
        /// <param name="clientMac">The client's mac adress</param>
        /// <param name="rssi">The client's rssi as measured by the sniffer</param>
        /// <param name="mode">'Offline' or 'Online' to help distinguish whether we want to save the data persistently</param>
        /// <param name="time">The time the sniffer entry was made. </param>
        private static void addToMeasurement(string snifferMac, string clientMac, int rssi, DateTime time)
        {
            Mode mode;
            //First, determine the mode. 
            if (currentOfflineMeasurements.ContainsKey(clientMac))
            {
                mode = Mode.Offline;
            }
            else if (currentOnlineMeasurements.ContainsKey(clientMac))
            {
                mode = Mode.Online;
            }
            else
            {
                return; //Unknown client mac
            }
            ConcurrentDictionary<string, SnifferWifiMeasurement> dic = getMeasurementDictionary(mode);

            SnifferWifiMeasurement curWifiMeasurement = dic[clientMac];

            //Update end meas time (even more may come..)
            curWifiMeasurement.meas_time_end = time;

            //Add or update histogram with snifferMac, rssi value
            SnifferHistogram curHist = curWifiMeasurement.SnifferHistograms.FirstOrDefault(h => h.Mac == snifferMac && h.value == rssi);
            if (curHist == null) //insert new histogram 
            {
                curHist = new SnifferHistogram();
                curHist.Mac = snifferMac;
                curHist.value = rssi;
                curHist.count = 0;
                curWifiMeasurement.SnifferHistograms.Add(curHist);
                curHist.SnifferWifiMeasurement = curWifiMeasurement;

                if (mode == Mode.Offline)
                {
                    context.AddRelatedObject(curWifiMeasurement, "SnifferHistograms", curHist);
                }
            }
            curHist.count++; //yet another count for this rssi value

            //In the offline mode we actually save the data. 
            if (mode == Mode.Offline)
            {
                context.UpdateObject(curWifiMeasurement);
                context.UpdateObject(curHist);
            }
        }

        private static Building getCorrectBuilding(SnifferWifiMeasurement meas)
        {
            //consider strategy pattern		
            return getNearestAPMatchBuilding(meas);
        }       

        private static ConcurrentDictionary<string, SnifferWifiMeasurement> getMeasurementDictionary(Mode mode)
        {
            if (mode == Mode.Offline)
                return currentOfflineMeasurements;
            else
                return currentOnlineMeasurements;
        }

        private static Building getNearestAPMatchBuilding(SnifferWifiMeasurement meas)
        {
            if (LoadedBuildings == null)
                return null;
            if (meas == null)
                return null;

            Building bestMatch = null;
            //find best match by matching number of common APs
            int maxCommonAPs = 0;
            foreach (Building curBuilding in LoadedBuildings)
            {
                int commonAPs = getNumberOfIdenticalMacs(meas.getMACs(), curBuilding);
                if (commonAPs > maxCommonAPs)
                {
                    maxCommonAPs = commonAPs;
                    bestMatch = curBuilding;
                }
            }
            return bestMatch;
        }

        private static int getNumberOfIdenticalMacs(IEnumerable<String> measMacs, Building b)
        {
            int numMatches = 0;
            foreach (Building_MacInfos macInfo in b.Building_MacInfos)
            {
                foreach (String curA in measMacs)
                {
                    if (macInfo.Mac == curA)
                        numMatches++;
                }
            }
            return numMatches;
        }


        /// <summary>
        /// Converts the 'sniffer time' to dateTime (while adding day info to the sniffer time)
        /// Sniffer format = HH:MM:SS.ssssss
        /// (example) = 10:01:21.185194
        /// </summary>
        /// <param name="hms">Sniffer time in format HH:MM:SS.ssssss</param>
        /// <returns>Sniffer time converted to DateTime</returns>
        private static DateTime getTime(string hms)
        {
            int hour = int.Parse(hms.Substring(0, 2));
            int minute = int.Parse(hms.Substring(3, 2));
            int second = int.Parse(hms.Substring(6, 2));
            int milliSecond = int.Parse(hms.Substring(9)) / 1000; //givet i mikrosekunder

            //får dagen fra DateTime - resten fra timestamp fra sniffer
            DateTime now = DateTime.Now;
            DateTime time = new DateTime(now.Year, now.Month, now.Day, hour, minute, second, milliSecond);
            return time;
        }
        
        //Just enumerates the LoadedBuildings to check the result
        private static void checkBuildings()
        {
            foreach (Building b in LoadedBuildings)
            {
                foreach (Vertex v in b.Vertices)
                {
                    foreach (AbsoluteLocation a in v.AbsoluteLocations)
                    {

                    }

                    foreach (SymbolicLocation s in v.SymbolicLocations)
                    {

                    }

                    foreach (SnifferWifiMeasurement sw in v.SnifferWifiMeasurements)
                    {
                        foreach (SnifferHistogram sh in sw.SnifferHistograms)
                        {

                        }
                    }

                    foreach (Edge e in v.Edges)
                    {

                    }
                }

                foreach (Edge e in b.Edges)
                {
                    foreach (Vertex v in e.Vertices)
                    {

                    }
                }

                foreach (Building_MacInfos bm in b.Building_MacInfos)
                {

                }

                foreach (Building_Floors bf in b.Building_Floors)
                {

                }
            }
        }

        public static bool isCurrentlyMeasuring(string clientMac)
        {
            return currentMeasuringClients.Keys.Contains(clientMac);
        }

        private static Socket socket;
        private const int MAX_CONCURRENT_SNIFFERS = 10; //10 concurrent sniffers

        private static void StartTcpServer()
        {
            LoadRadioMap();

            socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            try
            {
                socket.Bind(new IPEndPoint(IPAddress.Any, 5555));
            }
            catch (SocketException ex) //til at forhindre socketException under debugging
            {
                Console.WriteLine("Lukker ballet pga:\n");
                Console.WriteLine(ex.Message);
                socket.Close();
                return;
            }
            
            socket.Listen(200);
            Console.WriteLine("Server mounted, listening to port 5555");

            for (int i = 0; i < MAX_CONCURRENT_SNIFFERS; i++)
            {
                Thread t = new Thread(new ThreadStart(SnifferReceiver));
                t.Start();
            }            
        }

        private static void SnifferReceiver()
        {
            while (true)
            {
                Socket soc = socket.Accept(); // listener.AcceptSocket();

                Console.WriteLine("Connected: {0}", soc.RemoteEndPoint);

                try
                {
                    Stream s = new NetworkStream(soc);
                    StreamReader sr = new StreamReader(s);
                    StreamWriter sw = new StreamWriter(s);
                    sw.AutoFlush = true; // enable automatic flushing
                    while (true)
                    {
                        string msg = sr.ReadLine();

                        string[] msgParts = msg.Split(' ');

                        try
                        {
                            string snifferMac = msgParts[0];
                            string hms = msgParts[1]; //e.g.: 10:01:21.185194
                            DateTime time = getTime(hms);
                            string strRssi = msgParts[2];
                            int rssi = int.Parse(strRssi);
                            string clientMac = msgParts[3];

                            if (isCurrentlyMeasuring(clientMac))
                            {
                                addToMeasurement(snifferMac, clientMac, rssi, time);
                            }
                            Console.WriteLine("Sniffer mac = {0}; time = {1}; RSSI = {2}; clientMac = {3}", snifferMac, time, strRssi, clientMac);
                        }
                        catch (Exception ex)
                        {
                            Console.WriteLine(ex.Message);
                            break;
                        }
                    }
                    s.Close();
                }
                catch (Exception e)
                {
                    Console.WriteLine(e.Message);
                }

                Console.WriteLine("Disconnected: {0}", soc.RemoteEndPoint);
                soc.Close();
            }
        }

        private static void StartUdpServer()
        {
            LoadRadioMap();

            //Start listening for sniffer measurements
            UdpClient udpc = new UdpClient(5555);
            Console.WriteLine("Server started, servicing on port 2055");
            IPEndPoint ep = null;
            while (true)
            {
                byte[] rawData = udpc.Receive(ref ep);
                string strData = Encoding.ASCII.GetString(rawData);
                string[] msgParts = strData.Split(' ');

                try
                {
                    string snifferMac = msgParts[0];
                    string hms = msgParts[1]; //e.g.: 10:01:21.185194
                    DateTime time = getTime(hms);
                    string strRssi = msgParts[2];
                    int rssi = int.Parse(strRssi);
                    string clientMac = msgParts[3];

                    if (isCurrentlyMeasuring(clientMac))
                    {
                        addToMeasurement(snifferMac, clientMac, rssi, time);
                    }
                    Console.WriteLine("Sniffer mac = {0}; time = {1}; RSSI = {2}; clientMac = {3}", snifferMac, time, strRssi, clientMac);
                }
                catch (Exception ex)
                {
                    Console.WriteLine(ex.Message);
                }
            }
        }

        //UDP udgaven:
        /*
        private static void StartTcpServer()
        {
            LoadRadioMap();

            //Start listening for sniffer measurements
            UdpClient udpc = new UdpClient(5555);
            Console.WriteLine("Server started, servicing on port 2055");
            IPEndPoint ep = null;
            while (true)
            {
                byte[] rawData = udpc.Receive(ref ep);
                string strData = Encoding.ASCII.GetString(rawData);
                string[] msgParts = strData.Split(' ');

                try
                {
                    string snifferMac = msgParts[0];
                    string hms = msgParts[1]; //e.g.: 10:01:21.185194
                    DateTime time = getTime(hms);
                    string strRssi = msgParts[2];
                    int rssi = int.Parse(strRssi);
                    string clientMac = msgParts[3];

                    if (isCurrentlyMeasuring(clientMac))
                    {
                        addToMeasurement(snifferMac, clientMac, rssi, time);
                    }
                    Console.WriteLine("Sniffer mac = {0}; time = {1}; RSSI = {2}; clientMac = {3}", snifferMac, time, strRssi, clientMac);
                }
                catch (Exception ex)
                {
                    Console.WriteLine(ex.Message);
                }
            }
        }
         */ 

        private static void LoadRadioMap()
        {
            if (!IsDownloadingRadioMap)
            {
                IsDownloadingRadioMap = true;

                context = new radiomapEntities(radiomapUri);
                //Load LoadedBuildings
                String expandOptions = "Building_MacInfos,Building_Floors,Edges,Vertices,Vertices/AbsoluteLocations,Vertices/SymbolicLocations,Vertices/SnifferWifiMeasurements,Vertices/SnifferWifiMeasurements/SnifferHistograms";
                DataServiceQuery<Building> query = context.Buildings.Expand(expandOptions);

                //NOTE: Maybe surround with try-catch and try again
                LoadedBuildings = query.Execute().ToList();

                IsDownloadingRadioMap = false;
                checkBuildings();
            }
        }

        //NOTE: Remove WebGet after testing is done. 
        [WebGet]
        public void TestIt(string clientMac)
        {
            string testSniffer1 = "testsniffer 1";
            string testSniffer2 = "testsniffer 2";
            
            //StartMeasuringAtUnboundLocation(testClient, 170);

            addToMeasurement(testSniffer1, clientMac, -60, DateTime.Now);
            addToMeasurement(testSniffer2, clientMac, -55, DateTime.Now);

            addToMeasurement(testSniffer1, clientMac, -60, DateTime.Now);
            addToMeasurement(testSniffer2, clientMac, -55, DateTime.Now);

            addToMeasurement(testSniffer1, clientMac, -50, DateTime.Now);
            addToMeasurement(testSniffer2, clientMac, -50, DateTime.Now);

            //StopMeasuring(testClient);
        }

        /// <summary>
        /// A client calls this method to start a new sniffer measurement at the specified BOUND location (given by the vertex id). 
        /// The start time of the measurement is given by DateTime.Now
        /// </summary>
        /// <param name="clientMac">The client requesting the sniffer measurement</param>
        /// <param name="vertexId">The id of the vertex where the user is located</param>
        /// <returns>False if 1) or 2), true otherwise. 1) clientMac is null or empty; 2) the buildingId-vertexID combination does not refer to a bound location</returns>
        [WebGet]
        public bool StartMeasuringAtBoundLocation(string clientMac, int buildingId, int vertexId)
        { 
            //NOTE: MAYBE LOAD BUILDINGS HERE

            //Find vertex with id
            Vertex vertex = null;
            try
            {
                Building b = LoadedBuildings.Find(b1 => b1.ID == buildingId);
                if (b != null)
                    vertex = b.Vertices.FirstOrDefault(v => v.ID == vertexId);                
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
            if (vertex == null)
                return false; //No such vertex
            else
                return StartMeasuring(clientMac, vertex);
        }

        /**
         * Unbound vertices will get a fake (negative) id to identify them until they are saved persistently
         * (when they will be assigned an id by the db)
         */ 
        private static readonly int UNBOUND_ID_STARTVALUE = -99;
        private static int NEXT_UNBOUND_ID = UNBOUND_ID_STARTVALUE;
        private static int GetNextFakeVertexId()
        {
            if (NEXT_UNBOUND_ID == int.MinValue)
            {
                NEXT_UNBOUND_ID = UNBOUND_ID_STARTVALUE;
            }
            return NEXT_UNBOUND_ID--;
        }

        /// <summary>
        /// A client calls this method to start a new sniffer measurement at the specified UNBOUND location. 
         /// Note that Latitude and longitude are specified as latE6 and lonE6 - that is multiplied by a million
        /// This is done in order to avoid potential problems with types that use a dot (.). 
        /// Altitude, however, specifies a floor.
        /// ( http://stackoverflow.com/questions/5094276/how-to-pass-a-decimal-number-to-a-rest-web-service )
        /// </summary>
        /// <param name="clientMac"></param>
        /// <param name="buildingId"></param>
        /// <param name="latE6"></param>
        /// <param name="lonE6"></param>
        /// <param name="altE6"></param>
        /// <returns>true, unless clientMac is empty or null</returns>
        [WebGet]
        public bool StartMeasuringAtUnboundLocation(string clientMac, int buildingId, int latE6, int lonE6, int alt)
        {
            /// A temporary vertex is created at the specified coordinates in the specified building
            /// but the vertex is not materialized until SaveMeasurement(...) is called.
            /// (until that time the vertex is given a fake id)
       
            //Create new vertex in the specified building at the specified coordinates with a fake id
            Vertex v = new Vertex();
            v.ID = GetNextFakeVertexId();
            v.Building_ID = buildingId;
            const decimal e6 = 1000000;
            AbsoluteLocation absLoc = new AbsoluteLocation();
            absLoc.latitude = latE6 / e6;
            absLoc.longitude = lonE6 / e6;
            absLoc.altitude = alt;
            v.AbsoluteLocations.Add(absLoc);

            return StartMeasuring(clientMac, v);
        }

        /// <summary>
        /// A client calls this method to start a new sniffer measurement at the specified location (with the specified start time). 
        /// </summary>
        /// <param name="clientMac">The client requesting the sniffer measurement</param>
        /// <param name="vertexId">The id of the vertex where the user is located</param>
        /// <param name="startMeasTime">The start time of the measurement (as specified by the user)</param>
        private bool StartMeasuring(string clientMac, Vertex vertex)
        {
            if (clientMac == null || vertex == null)
                return false;

            if (context == null || LoadedBuildings == null)
            {
                LoadRadioMap(); //Ja, eller bare opdater det lokale radio map hver gang en bruger foretager en maaling
                return false;
            }            

            //Create new sniffer measurement
            SnifferWifiMeasurement newMeas = new SnifferWifiMeasurement();
            newMeas.meas_time_start = DateTime.Now;

            //Add sniffer-meas to vertex
            vertex.SnifferWifiMeasurements.Add(newMeas);
            newMeas.Vertex = vertex;
            
            //Add measurement to 'currently building' datastructures.
            currentOfflineMeasurements.TryAdd(clientMac, newMeas);
            currentMeasuringClients.TryAdd(clientMac, vertex);

            return true;
        }

        /// <summary>
        /// A client calls this to indicate that he wants to be positioned.  
        /// The server will then start saving sniffer measurements for this client (rather than throwing them away). 
        /// The server will keep saving measurements until the client de-registers with the corresponding StopWifiPositioning(clientMac) method
        /// </summary>
        /// <param name="clientMac">Mac address of client that wants to be positioned</param>
        ///
        [WebGet]
        public bool StartWifiPositioning(string clientMac)
        {
            if (clientMac == null)
                return false;
            
            //Create new sniffer measurement
            DateTime time = DateTime.Now;
            SnifferWifiMeasurement newMeas = new SnifferWifiMeasurement();
            newMeas.meas_time_start = DateTime.Now;

            //Add measurement to 'currently building' datastructures.
            currentOnlineMeasurements.TryAdd(clientMac, newMeas);
            //add client to 'currently positioning' and set initial location = null. 
            currentPositioningClients.TryAdd(clientMac, null); //no 'last position'  
            //add engine for user
            currentWifiPosEngines.TryAdd(clientMac, new WifiPosEngine(null)); //unknown building		

            return true;
        }

       
        /// <summary>
        /// The client stops a current sniffer wifi measurement (if one is indeed under way). 
        /// The WifiSnifferMeasurement is added to the Vertex where it was conducted.
        /// Prerequisites: 
        /// - A sniffer infrastructure must be in place.
        /// - A sniffer measurement must be started first - with either of the StartMeasuring.. methods
        /// This call simply moves a successful measurement to the 'saved' region where it can later 
        /// be saved persistently with SaveMeasurement(clientMac). 
        /// (It was found to be more intuitive to be able to stop and save a measurement in two steps rather than doing it all at once)
        /// <param name="clientMac">The client requesting to stop a curWifi sniffer measurement</param>
        /// <returns>True if a measurement was successfully saved. False otherwise</returns>
        /// </summary>
        [WebGet]
        public bool StopMeasuring(string clientMac)
        {
            if (clientMac == null)
                return false;

            if (!(currentMeasuringClients.ContainsKey(clientMac) && currentOfflineMeasurements.ContainsKey(clientMac)))
                return false;

            //Remove the measurement from from the 'current' list. 
            SnifferWifiMeasurement curWifi;
            currentOfflineMeasurements.TryRemove(clientMac, out curWifi);
            Vertex curVertex;
            currentMeasuringClients.TryRemove(clientMac, out curVertex);

            //DEBUG: ADD THE FOLLOWING CODE (0-CHECK) - IT IS ONLY OMITTED FOR DEBUGGING
            //We do not want to save empty measurements persistently
            if (curWifi.getMACs().Count == 0)
                return false; //It is an empty measurement, i.e., no data received from sniffers

            //Finalize the measurement, i.e., set the stop time
            curWifi.meas_time_end = DateTime.Now;

            //Now move to the 'saved' region
            if (savedOfflineMeasurements.ContainsKey(clientMac))
                savedOfflineMeasurements[clientMac] = curWifi;
            else
                savedOfflineMeasurements.Add(clientMac, curWifi);
            if (savedMeasuringClients.ContainsKey(clientMac))
                savedMeasuringClients[clientMac] = curVertex;
            else
                savedMeasuringClients.Add(clientMac, curVertex);
            return true;
        }

        /* StopMeasuring(clientMac) versionen hvor en måling blev stoppet og gemt i ét hug. 
      [WebGet]
      public bool StopMeasuring(string clientMac)
      {
          if (clientMac == null)
              return false;

          if (!(currentMeasuringClients.ContainsKey(clientMac) && currentOfflineMeasurements.ContainsKey(clientMac)))
              return false; 

          //Finalize the measurement, i.e., set the stop time
          SnifferWifiMeasurement curWifi = currentOfflineMeasurements[clientMac];
          curWifi.meas_time_end = DateTime.Now;
          if (curWifi.getMACs().Count == 0)
              return false; //It is an empty measurement, i.e., no data received from sniffers

          //Link the vertex and measurement
          Vertex curVertex = currentMeasuringClients[clientMac];
          curVertex.SnifferWifiMeasurements.Add(curWifi);
            
          //save to db                      
          try
          {
              //get context 
              radiomapEntities localContext = new radiomapEntities(radiomapUri);
              localContext.AttachTo("Vertices", curVertex);
              localContext.AddRelatedObject(curVertex, "SnifferWifiMeasurements", curWifi);
              foreach (SnifferHistogram curHist in curWifi.SnifferHistograms)
              {
                  localContext.AddRelatedObject(curWifi, "SnifferHistograms", curHist);
              }
              //save to database (NB: use local context in order not to save pending measurements)
              DataServiceResponse response = localContext.SaveChanges(SaveChangesOptions.Batch);

              SnifferWifiMeasurement addedMeasurement = null;
              // Enumerate the returned responses. Ikke brugt til noget fornuftigt pt. 
              foreach (ChangeOperationResponse change in response)
              {
                  // Get the descriptor for the entity.
                  EntityDescriptor descriptor = change.Descriptor as EntityDescriptor;

                  if (descriptor != null)
                  {
                      addedMeasurement = descriptor.Entity as SnifferWifiMeasurement;

                      if (addedMeasurement != null)
                      {
                          break; //Yes, the measurement was succesfully saved to the db.
                      }
                  }
              }

              //Remove the measurement from from the 'current' list. 
              SnifferWifiMeasurement dummyMeas;
              currentOfflineMeasurements.TryRemove(clientMac, out dummyMeas);
              Vertex dummyVert;
              currentMeasuringClients.TryRemove(clientMac, out dummyVert);

              return true;
          }
          catch (DataServiceRequestException ex)
          {
              return false; 
          }            
      }
      */   

        /// <summary>
        /// Saves a measurement by the specified client persistently
        /// Requires that a measurement has been conducted first. I.e., this call has been superseded by the two calls: 
        /// 1)StartMeasurement(clientMac, vertexId)
        /// 2) StopMeasurement(clientMac)
        /// </summary>
        /// <param name="clientMac"></param>
        /// <returns>-1 if the measurement was not successfully saved; the id of the vertex (new or existing) otherwise</returns>
        [WebGet]
        public int SaveMeasurement(string clientMac)
        {
            const int error = -1;
            int result = error; //change if success

            if (clientMac == null)
                return error;

            if (!(savedMeasuringClients.ContainsKey(clientMac) && savedOfflineMeasurements.ContainsKey(clientMac)))
                return error;

            //Remove the measurement from from the 'current' list. 
            SnifferWifiMeasurement curWifi = savedOfflineMeasurements[clientMac];
            Vertex curVertex = savedMeasuringClients[clientMac];
            savedOfflineMeasurements.Remove(clientMac);
            savedMeasuringClients.Remove(clientMac);

            Building curBuilding = LoadedBuildings.First(b => b.ID == curVertex.Building_ID);
            //save changes persistently                      
            try
            {
                //Notify context about changes 
                radiomapEntities localContext = new radiomapEntities(radiomapUri);
                if (curVertex.ID <= UNBOUND_ID_STARTVALUE) //unbound location
                {
                    AbsoluteLocation absLoc = curVertex.AbsoluteLocations.First();
                    localContext.AttachTo("Buildings", curBuilding);
                    localContext.AddRelatedObject(curBuilding, "Vertices", curVertex);
                    localContext.AddRelatedObject(curVertex, "AbsoluteLocations", absLoc);
                }
                else
                {
                    localContext.AttachTo("Vertices", curVertex);
                    result = curVertex.ID;
                }
                localContext.AddRelatedObject(curVertex, "SnifferWifiMeasurements", curWifi);
                foreach (SnifferHistogram curHist in curWifi.SnifferHistograms)
                {
                    localContext.AddRelatedObject(curWifi, "SnifferHistograms", curHist);
                }

                //save to database
                DataServiceResponse response = localContext.SaveChanges(SaveChangesOptions.Batch);

                // Enumerate the returned responses. If a new vertex, add it to the current building and return its id. 
                foreach (ChangeOperationResponse change in response)
                {
                    // Get the descriptor for the entity.
                    EntityDescriptor descriptor = change.Descriptor as EntityDescriptor;

                    if (descriptor != null)
                    {
                        if (descriptor.Entity is SnifferWifiMeasurement)
                        {
                            //Yes, the measurement got saved
                            Console.WriteLine("Measurement saved."); //dummy statement                            
                        }
                        else if (descriptor.Entity is Vertex)
                        {
                            Vertex newVertex = descriptor.Entity as Vertex;
                            curBuilding.Vertices.Add(newVertex);
                            result = newVertex.ID;
                        }
                    }
                }

                return result;
            }
            catch (DataServiceRequestException)
            {
                return error;
            }
        }           

        [WebGet]
        public bool StopWifiPositioning(string clientMac)
        {
            if (clientMac == null)
                return false;

            //Remove from 'ongoing' online datastructures.
            //Remove current wifi measurement
            SnifferWifiMeasurement dummyMeasurement;
            currentOnlineMeasurements.TryRemove(clientMac, out dummyMeasurement);
            //Remove current location (vertex)
            Vertex dummyVertex;
            currentPositioningClients.TryRemove(clientMac, out dummyVertex);
            //Remove positioning engine
            WifiPosEngine dummyPosE;
            currentWifiPosEngines.TryRemove(clientMac, out dummyPosE);
            
            return true;
        }  
    
        [WebGet]
        public PositionEstimate TestGetRandomPosition(int buildingId)
        {
            Building curBuilding = LoadedBuildings.First(b => b.ID == buildingId);
            Random r = new Random();
            int randomNum = r.Next(curBuilding.Vertices.Count);
            int i = 1; 
            foreach (Vertex v in curBuilding.Vertices)
            {
                if (i >= randomNum)
                {
                    AbsoluteLocation absLoc = v.AbsoluteLocations.First();
                    return new PositionEstimate()
                    {
                        ID = v.ID, 
                        VertexID = v.ID,
                        Building_ID = v.Building_ID,
                        Latitude = (double)absLoc.latitude,
                        Longitude = (double)absLoc.longitude,
                        Altitude = (int)absLoc.altitude,
                        Provider = WIFI_PROVIDER,
                        Time = DateTime.Now,
                        Accuracy = 10 //dummy accuracy
                    };
                }
                i++;
            }            
            return null;
        }
        
    }
}
