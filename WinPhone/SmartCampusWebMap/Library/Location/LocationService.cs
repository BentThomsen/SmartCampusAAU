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
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using SmartCampusWebMap.RadiomapBackend;
using System.Data.Services.Client;
using System.Linq;
using System.Collections.Generic;
using System.Threading;
using SmartCampusWebMap.WifiSnifferPositioningService;
using Microsoft.Phone.Info;
using Microsoft.Phone.Net.NetworkInformation;
using System.IO.IsolatedStorage;

namespace SmartCampusWebMap.Library.Location
{
	public static class LocationService
    {
        public static radiomapEntities radiomapContext { get; private set; }
		private static readonly Uri radiomapUri =
			new Uri("http://smartcampusaau.cs.aau.dk/RadioMapService3/RadioMapService.svc/");

		private static DataServiceCollection<Building> buildings;
		
		public static readonly String DEFAULT_TILE_URL = "http://smartcampus.cs.aau.dk/tiles/sl300/no_tiles.php";

		public static Building CurrentBuilding { get; set; }
        
        public static event EventHandler OnBuildingDownload;

		//This is akin to ServiceConnected in Android Map2DOnline
		private static void buildings_LoadCompleted(object sender, LoadCompletedEventArgs e)
		{
            if (e.Error == null)
			{
				// Handling for a paged data feed.
				if (buildings.Continuation != null)
				{
					// Automatically load the next page.
					buildings.LoadNextPartialSetAsync();
				}
				else
				{
					//The data is ready
					foreach (Building b in buildings)
					{
						CurrentBuilding = b;
                        //TODO: Currently, LoadEdges just loops through the edges and does nothing
						LoadEdges(CurrentBuilding);
						break;
					}
				}
			}
			else
			{
				CurrentBuilding = null;
			}

			//Now notify the observers
			//we just let the observers know that something has happened. 
			//They can then check if CurrentBuilding != null
            //TODO: MAYBE just replace this with WifiStatusChanged, where the Status is RADIOMAP_DOWNLOADED
			
            if (OnBuildingDownload != null) //any observers?        
				OnBuildingDownload(null, null);     
		}

		/// <summary>
		/// NOTE: This is a temporary solution to fill out edges' vertices. 
		/// In the future do this already when we are setting up edges. 
		/// </summary>
		/// <param name="building"></param>
		private static void LoadEdges(Building building)
		{
			foreach (Edge e in building.Edges)
			{
				Vertex origin = building.Vertices.FirstOrDefault(v => v.ID == e.vertexOrigin);
				Vertex destination = building.Vertices.FirstOrDefault(v => v.ID == e.vertexDestination);
				if (!e.Vertices.Contains(origin))
				{
					e.Vertices.Add(origin);
                    //TODO: Kender vertices deres edges?
                    origin.Edges.Add(e);
                }
				if (!e.Vertices.Contains(destination))
				{
					e.Vertices.Add(destination);

                    destination.Edges.Add(e);
				}
			}
		}   
		
        public static void initializeSniffer()
        {
            snifferContext = new SnifferModel(snifferUri);
        }       

		/// <summary>
		/// Downloads a radio map for the building with the specified id. 
		/// </summary>
		/// <param name="buildingId"></param>
		public static void DownloadRadioMap(int buildingId)
		{
			radiomapContext = new radiomapEntities(radiomapUri);
			buildings = new DataServiceCollection<Building>(radiomapContext);
            
            String expandOptions = "Building_Floors,Vertices,Vertices/AbsoluteLocations,Vertices/SymbolicLocations,Vertices/Edges,Edges,Edges/Vertices,Edges/Vertices/AbsoluteLocations";
			var query = from b in radiomapContext.Buildings.Expand(expandOptions)
						where b.ID == buildingId 
						select b;

			// Register for the LoadCompleted event.
			buildings.LoadCompleted
				+= new EventHandler<LoadCompletedEventArgs>(buildings_LoadCompleted);
			buildings.LoadAsync(query);
		}

		#region Wi-Fi positioning
        //used to save/retrieve mac address
        private const string macKey = "MacAddress";
        private static String _macAddress;
        public static string MacAddress
        {
            get 
            {
                if (string.IsNullOrEmpty(_macAddress))
                {                    
                    IsolatedStorageSettings settings = IsolatedStorageSettings.ApplicationSettings;

                    if (settings.Contains(macKey))
                    {
                        _macAddress = (string)settings[macKey]; //may still be null
                    }
                }
                return _macAddress;
            }
            set
            {
                _macAddress = value ?? ""; //cannot save null values
                IsolatedStorageSettings.ApplicationSettings[macKey] = _macAddress;
            }
        }

        //Client-id is an auto-generated id != mac address such that a particular client device
        //cannot be identified in the list of tracking data.
        private const string idKey = "ClientId";
        private static String _clientId;
        public static string ClientId
        {
            get
            {
                if (string.IsNullOrEmpty(_clientId))
                {
                    IsolatedStorageSettings settings = IsolatedStorageSettings.ApplicationSettings;

                    if (settings.Contains(idKey))
                    {
                        _clientId = (string)settings[idKey]; //may still be null
                    }
                    else
                    {
                        _clientId = Guid.NewGuid().ToString();
                        IsolatedStorageSettings.ApplicationSettings[idKey] = _clientId;
                    }
                }
                return _clientId;
            }
        }

        //Event and EventArgs to notify about new position updates        
        public static event EventHandler<PositionEstimateEventArgs> OnPositionEstimateChange;
        public class PositionEstimateEventArgs : EventArgs
        {
            public WifiSnifferPositioningService.PositionEstimate PositionEstimate { get; private set; }
            public PositionEstimateEventArgs(WifiSnifferPositioningService.PositionEstimate pos)
            {
                this.PositionEstimate = pos;
            }
        }
        private static void OnPositionEstimateChanged(object sender, PositionEstimateEventArgs pea)
        {
            if (OnPositionEstimateChange != null) //any listeners?
            {
                OnPositionEstimateChange(sender, pea);
            }
        }

        private static bool isDoingWifiPositioning { get; set; }
        private static int mWifiPositioningUpdateInterval = 10000;
        private static WifiSnifferPositioningService.PositionEstimate PreviousPositionEstimate { get; set; }
        private static WifiSnifferPositioningService.PositionEstimate CurrentPositionEstimate { get; set; }
               
		/**
		  * This setter is used to define the Wi-Fi positioning update interval, i.curEdge., the interval between consecutive position estimates. 
		  * @param updateIntervalInMilliseconds The update interval in milliseconds. The lowest allowed value is 100
		  */
		public static void setWifiPositioningUpdateInterval(int updateIntervalInMilliseconds)
		{
			if (updateIntervalInMilliseconds >= 1000)
				mWifiPositioningUpdateInterval = updateIntervalInMilliseconds;
			else
				mWifiPositioningUpdateInterval = 1000;
		}

        #region Timeout handling 
        /// <summary>
        /// This is the timeout period (in seconds) that must elapse after calling 'startWifiPositioning' before 
        /// raising a 'snifferInfrastructureNotfound' event.  
        /// If a position estimate is returned before, the 'building ready' event is raised instead.
        /// </summary>
        public static int WifiTimeout
        {
            get { return _wifiTimeout;}
            set 
            {
                //3 seconds is the absolute minimum timeout
                _wifiTimeout = value > 3 ? value : 3;         

            }
        }
        private static int _wifiTimeout = 5;
        private static bool _hasReceivedWifiPosition;
                
        public static event EventHandler<BuildingIdentificationEventArgs> OnBuildingIdentificationChange;
        public class BuildingIdentificationEventArgs : EventArgs
        {
            public readonly bool BuildingFound;
            public readonly int BuildingId;
            public BuildingIdentificationEventArgs(bool buildingFound)
                : this(buildingFound, -1)
            {
                
            }
            public BuildingIdentificationEventArgs(bool buildingFound, int buildingId)
            {
                this.BuildingFound = buildingFound;
                this.BuildingId = buildingId;
            }
        }
        private static void OnBuildingIdentificationStatusChanged(object sender, BuildingIdentificationEventArgs wse)
        {
            if (OnBuildingIdentificationChange != null) //any listeners?
            {
                OnBuildingIdentificationChange(sender, wse);
            }
        }

        #endregion
        
        public static bool startWifiPositioning()
		{
            return startWifiPositioning(mWifiPositioningUpdateInterval, WifiTimeout);
		}

		public static bool startWifiPositioning(int updateIntervalInMilliseconds, int wifiTimeoutInSeconds)
		{            
			//Cancel any previous positioning first
			if (isDoingWifiPositioning)
			{
                return false;
			}

			isDoingWifiPositioning = true;
            setWifiPositioningUpdateInterval(updateIntervalInMilliseconds);
            //A position estimate must be received within the timeout period
            _hasReceivedWifiPosition = false;
			WifiTimeout = wifiTimeoutInSeconds;
			
			new Thread(new ThreadStart(doWifiPositioning)).Start();
            return true;
		}

        private static SnifferModel snifferContext;
        private static readonly Uri snifferUri =
            new Uri("http://smartcampus.cs.aau.dk/WifiSnifferPositioningService/SnifferService.svc/");

		private static void doWifiPositioning()
		{
            DateTime startTime = DateTime.Now;
                
            while (isDoingWifiPositioning)
            {
                if (snifferContext == null)
                    initializeSniffer();

                //The client COULD potentially be changing mac address in the middle of the positioning loop
                string queryString = string.Format("GetPosition?clientMac='{0}'", MacAddress);
                //string queryString = string.Format("TestGetRandomPosition?buildingId=16");
            
                try
                {
                    snifferContext.BeginExecute<WifiSnifferPositioningService.PositionEstimate>(
                        new Uri(queryString, UriKind.Relative),
                        OnGetPositionComplete, snifferContext);
                }
                catch (DataServiceQueryException)
                {
                    //may notify listeners?
                }

                //If we didn't get a position estimate within the timeout period, we stop the party.
                int elapsedSeconds = DateTime.Now.Subtract(startTime).Seconds;
                if (!_hasReceivedWifiPosition && elapsedSeconds > WifiTimeout)
                {
                    isDoingWifiPositioning = false;
                    OnBuildingIdentificationStatusChanged(null, new BuildingIdentificationEventArgs(false));
                }
                else
                {
                    Thread.Sleep(mWifiPositioningUpdateInterval);
                }
            }
		}

        private static void OnGetPositionComplete(IAsyncResult result)
        {
            // Get the radiomapContext back from the stored state.
            snifferContext = result.AsyncState as SnifferModel;
            try
            {
                WifiSnifferPositioningService.PositionEstimate newPos = snifferContext.EndExecute<WifiSnifferPositioningService.PositionEstimate>(result).FirstOrDefault();
                PreviousPositionEstimate = CurrentPositionEstimate;
                CurrentPositionEstimate = newPos;
                if (CurrentPositionEstimate != null)
                {
                    
                    //We notify about a new building in any of these two scenarios:
                    //1) This is the first estimate after starting positioning.
                    //2) The building id of the current estimate differs from the previous
                    bool arrivedAtNewBuilding = PreviousPositionEstimate != null && CurrentPositionEstimate != null && CurrentPositionEstimate.Building_ID != PreviousPositionEstimate.Building_ID;
                    if (!_hasReceivedWifiPosition || arrivedAtNewBuilding)
                    {
                        _hasReceivedWifiPosition = true;
                        OnBuildingIdentificationStatusChanged(null, new BuildingIdentificationEventArgs(true, CurrentPositionEstimate.Building_ID));
                    }
                                                          
                    OnPositionEstimateChanged(null, new PositionEstimateEventArgs(CurrentPositionEstimate));

                    if (AllowTracking)
                    {
                        AddToTrackedPositions(newPos);
                    }
                    
                }
            }
            catch (DataServiceQueryException) { }
        }

        public static void stopWifiPositioning()
        {
            isDoingWifiPositioning = false;
        }

        private static string trackingKey = "AllowTracking";
        //public static bool AllowTracking { get; set; }
        public static bool AllowTracking
        {
            get
            {                
                IsolatedStorageSettings settings = IsolatedStorageSettings.ApplicationSettings;

                if (settings.Contains(trackingKey))
                    return (bool)settings[trackingKey]; //may still be null
                else
                    return false;
            }
            set
            {
                IsolatedStorageSettings.ApplicationSettings[trackingKey] = value;
            }
        }

        private static List<TrackedPosition> bufferedTrackedPositions = new List<TrackedPosition>();
        private const int BUFFERED_POSITION_ESTIMATES_FLUSH_COUNT = 3;
        //Used to lock the bufferedTrackedPositions as we may be writing concurrently
        //(from wi-fi, gps, and we may be clearing the buffer)
        private static object bufferedTrackedPositionsLock = new Object();

        /// <summary>
        /// Creates a tracked position from a wifi sniffer position estimate and adds it to the tracked positions
        /// </summary>
        /// <param name="pos"></param>
        private static void AddToTrackedPositions(WifiSnifferPositioningService.PositionEstimate pos)
        {
            AddToTrackedPositions(CreateTrackedPosition(pos));                    
        }

        /// <summary>
        /// Creates a tracked position from a gps coordinate and adds it to the tracked positions
        /// </summary>
        /// <param name="pos"></param>
        public static void AddToTrackedPositions(System.Device.Location.GeoCoordinate pos)
        {
            AddToTrackedPositions(CreateTrackedPosition(pos));        
        }

        /// <summary>
        /// Add a position estimate to the buffer of tracked positions. 
        /// When the buffer is full, the data are flushed to the backend
        /// </summary>
        /// <param name="pos"></param>
        private static void AddToTrackedPositions(TrackedPosition pos)
        {
            // Wi-Fi and GPS can potentially be adding at the same time so we lock the buffer.
            lock (bufferedTrackedPositionsLock)
            {
                bufferedTrackedPositions.Add(pos);
            }
            if (IsTimeToSendTrackingData())
            {
                UploadTrackingDataAndFlushBuffer();
            }    
        }

        private static bool IsTimeToSendTrackingData()
        {
            return bufferedTrackedPositions.Count >= BUFFERED_POSITION_ESTIMATES_FLUSH_COUNT;
        }

        /// <summary>
        /// Both the sniffer backend and radiomap backend define a PositionEstimate entity. 
        /// Therefore, we convert WifiSniffer position estimates to radiomap position-estimates when 
        /// we save tracking data to the radio map backend (which houses the tracking data as well)
        /// </summary>
        /// <param name="?"></param>
        /// <returns></returns>
        private static TrackedPosition CreateTrackedPosition(WifiSnifferPositioningService.PositionEstimate org)
        {
            TrackedPosition res = new TrackedPosition();
            
            res.Accuracy = org.Accuracy;
            res.Altitude = org.Altitude;
            res.Bearing = org.Bearing;
            res.Building_ID = org.Building_ID;
            res.HasAccuracy = org.HasAccuracy;
            res.HasBearing = org.HasBearing;
            res.HasSpeed = org.HasSpeed;
            res.Latitude = org.Latitude;
            res.Longitude = org.Longitude;
            res.Provider = org.Provider;
            res.Speed = org.Speed;
            res.Time = DateTime.Now; //org.Time;
            res.VertexID = org.VertexID;
            res.ClientID = ClientId; //A guid

            return res;
        }

        private static TrackedPosition CreateTrackedPosition(System.Device.Location.GeoCoordinate org)
        {
            TrackedPosition res = new TrackedPosition();
            //NaN numbers cause an exception on MSSQL Server so they must be avoided.
            
            bool hasAccuracy = false;
            bool hasBearing = false;
            bool hasSpeed = false;
            if (!double.IsNaN(org.HorizontalAccuracy))
            {
                res.Accuracy = org.HorizontalAccuracy;
                hasAccuracy = true;
            }
            if (!double.IsNaN(org.Course))
            {
                res.Bearing = org.Course;
                hasBearing = true;
            }
            if (!double.IsNaN(org.Speed))
            {
                res.Speed = org.Speed;
                hasSpeed = true;
            }
            //res.Building_ID = -1;
           
            //Latitude, longitude, altitude are non-nullable (but should also never be NaN's)
            const double errorVal = -999;
            res.Latitude = !double.IsNaN(org.Latitude) ? org.Latitude : errorVal;
            res.Longitude = !double.IsNaN(org.Longitude) ? org.Longitude : errorVal;
            res.Altitude = !double.IsNaN(org.Altitude) ? org.Altitude : errorVal;
            
            res.Provider = "MS Location API (GPS)";
            res.Time = DateTime.Now;
            //res.VertexID = -1;
            res.HasAccuracy = hasAccuracy;
            res.HasBearing = hasBearing;
            res.HasSpeed = hasSpeed;
            res.ClientID = ClientId; //A guid

            return res;
        }

        private static void UploadTrackingDataAndFlushBuffer()
        {
            if (bufferedTrackedPositions == null)
                return;
            if (bufferedTrackedPositions.Count == 0)
                return;
                        
            //copy tracking data and clear buffer
            TrackedPosition[] tmp;
            lock (bufferedTrackedPositionsLock)
            {
                int numPositions = bufferedTrackedPositions.Count;
                tmp = new TrackedPosition[numPositions];
                bufferedTrackedPositions.CopyTo(tmp);
                bufferedTrackedPositions.Clear();
            }
            
            radiomapEntities context = new radiomapEntities(radiomapUri);
            foreach (TrackedPosition pos in tmp)
            {
                context.AddToTrackedPositions(pos);
            }
            context.BeginSaveChanges(SaveChangesOptions.Batch, OnPositionEstimatesSaved, context);
            
        }

        private static void OnPositionEstimatesSaved(IAsyncResult result)
        {
            bool errorOccured = false;

            radiomapEntities context = result.AsyncState as radiomapEntities;
            try
            {
                // Complete the save changes operation and display the response.
                DataServiceResponse response = context.EndSaveChanges(result);

                foreach (ChangeOperationResponse changeResponse in response)
                {
                    if (changeResponse.Error != null)
                        errorOccured = true;
                }
                if (!errorOccured)
                {
                    //Maybe raise success event?
                }
            }
            catch (Exception ex)
            {
                Exception exCopy = ex;    
            }
            
        }
        
		#endregion
	}
}
