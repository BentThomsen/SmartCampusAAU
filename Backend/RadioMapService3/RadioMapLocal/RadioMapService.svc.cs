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
DISCLAIMED. IN NO EVENT SHALL AAlBORG UNIVERSITY BE LIABLE FOR ANY
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

namespace RadioMapLocal
{
    public class RadioMapService : DataService< radiomapEntities >
    {
        // This method is called only once to initialize service-wide policies.
        public static void InitializeService(DataServiceConfiguration config)
        {
            //Allow everything on every entity:
            //config.SetEntitySetAccessRule("*", EntitySetRights.All);

            //Read and Create are the two allowed actions for most entities. 
            //However, we allow edges to be deleted and SymbolicLocations/ building floors to be modified
            //NOTE: I assume that subsequent rights will override the first defined right, i.e., the * option.
            config.SetEntitySetAccessRule("*", EntitySetRights.AllRead | EntitySetRights.WriteAppend);
            config.SetEntitySetAccessRule("SymbolicLocations", EntitySetRights.AllRead | EntitySetRights.WriteAppend | EntitySetRights.WriteMerge | EntitySetRights.WriteReplace);
            config.SetEntitySetAccessRule("Building_Floors", EntitySetRights.AllRead | EntitySetRights.WriteAppend | EntitySetRights.WriteMerge | EntitySetRights.WriteReplace);
            config.SetEntitySetAccessRule("Edges", EntitySetRights.AllRead | EntitySetRights.WriteAppend | EntitySetRights.WriteDelete);

            config.UseVerboseErrors = true;
            config.SetServiceOperationAccessRule("*", ServiceOperationRights.All);

            // Paging requires v2 of the OData protocol.
            config.DataServiceBehavior.MaxProtocolVersion = DataServiceProtocolVersion.V2;
        }              

        /**
         * Topic: Service operations.
         * cf: http://msdn.microsoft.com/en-us/library/cc668788.aspx
         */
        [WebGet]
        public IQueryable<Building> GetBuildingsWithName(string name)
        {
            //usage example: http://127.0.0.1:81/RadioMapService.svc/GetBuildingsWithName?name='Building'&$expand=Edges
            return CurrentDataSource.Buildings.Where(b => b.Building_Name.Contains(name));
        }

        /**
         * (Service operation: http://msdn.microsoft.com/en-us/library/cc668788.aspx )
         * This operation makes it possible to 'fake delete' a given vertex. 
         * 'Fake delete' means that the vertex is moved to the VertexGraveyard, and its edges are deleted.
         * cf: http://msdn.microsoft.com/en-us/library/cc668788.aspx
         */
        [WebGet]
        public void AddToVertexGraveyard(int buildingId, int vertexId)
        {
            //string errorMsg = "ERROR: Could not delete Vertex w. ID = " + vertexId + " from building w. ID = " + buildingId;
            //string succesMsg = "SUCCES: Deleted Vertex w. ID = " + vertexId + " from building w. ID = " + buildingId;

            //Check buildings exist
            Building graveyardBuilding = CurrentDataSource.Buildings.SingleOrDefault(b => b.ID == 18); //graveyard
            Building sourceBuilding = CurrentDataSource.Buildings.SingleOrDefault(b => b.ID == buildingId);
            if (graveyardBuilding == null || sourceBuilding == null)
                return; // errorMsg;

            //Check vertex exists
            Vertex targetVertex = CurrentDataSource.Vertices.SingleOrDefault(v => v.ID == vertexId && v.Building_ID == buildingId);
            if (targetVertex == null)
                return; // errorMsg;

            //delete the vertex's edges
            IQueryable<Edge> targetEdges = CurrentDataSource.Edges.Where(e => e.vertexOrigin == vertexId || e.vertexDestination == vertexId);
            foreach (Edge e in targetEdges)
            {
                CurrentDataSource.Edges.DeleteObject(e);
            }

            //move the vertex from the original building to the VertexGraveyard building (the one with ID = 18)
            graveyardBuilding.Vertices.Add(targetVertex);
            sourceBuilding.Vertices.Remove(targetVertex);
            CurrentDataSource.SaveChanges();
        }
                
        //IMPORTANT NOTE: Here we 'cheat' and don't download full histograms - instead we collapse them into avg values
        //to reduce the size of the file (original was 12 mb - see commented out method below)
        // http://127.0.0.1:82/RadioMapService.svc/RemoveNonCollectiveMeasurements?building_id=1&$expand=Vertices,Vertices/WifiMeasurements,Vertices/WifiMeasurements/Histograms
        [WebGet]
        public IQueryable<Building> RemoveNonCollectiveMeasurements(int building_id)
        {
            //IQueryable containing at most one entry
            IQueryable<Building> foundBuildings = CurrentDataSource.Buildings.Where(b => b.ID == building_id);
            if (foundBuildings != null)
            {
                foreach (Building building in foundBuildings)
                {
                    foreach (Vertex v in building.Vertices)
                    {
                        //Build a collective measurement or an empty measurement
                        WifiMeasurement collectiveWifi = BuildCollectiveWifiMeasurement(v);
                        WifiMeasurement lightWifi;
                        if (collectiveWifi == null)
                            lightWifi = new WifiMeasurement();
                        else
                            lightWifi = createLightWeightMeasurement(collectiveWifi);
                        
                        v.WifiMeasurements.Clear();
                        v.WifiMeasurements.Add(lightWifi);
                    }
                }
            }
            return foundBuildings;
        }

        //Here we build a lightweight histogram by taking the (mac, (value,count)) to find the avg value
        //Result: Histogram with (mac, (avg-value, count=1))
        private WifiMeasurement createLightWeightMeasurement(WifiMeasurement orgMeas)
        {
            if (orgMeas == null)
                return null;

            WifiMeasurement newMeas = new WifiMeasurement();
            newMeas.ID = orgMeas.ID;
            newMeas.Vertex_ID = orgMeas.Vertex_ID;
            newMeas.meas_time_start = orgMeas.meas_time_start;
            newMeas.meas_time_end = orgMeas.meas_time_end;

            Dictionary<String, int> macTotalValue = new Dictionary<string, int>();
            Dictionary<String, int> macTotalCount = new Dictionary<string, int>();
            foreach (Histogram h in orgMeas.Histograms)
            {
                if (!macTotalValue.ContainsKey(h.Mac))
                {
                    macTotalValue.Add(h.Mac, 0);
                    macTotalCount.Add(h.Mac, 0);
                }
                macTotalValue[h.Mac] += Math.Abs(h.value) * h.count;
                macTotalCount[h.Mac] += Math.Abs(h.count);
            }
            foreach (String mac in macTotalValue.Keys)
            {
                Histogram newHist = new Histogram();
                newHist.Mac = mac;
                newHist.value = -(macTotalValue[mac] / macTotalCount[mac]);
                newHist.count = 1;
                newMeas.Histograms.Add(newHist);
            }
            return newMeas;            
        }

        private WifiMeasurement BuildCollectiveWifiMeasurement(Vertex source)
        {
            if (source == null)
                return null;
            if (source.WifiMeasurements.Count < 1)
                return null;

            WifiMeasurement collectiveWifi = new WifiMeasurement();
            collectiveWifi.Is_Collective = true;

            //1. Build collective measurement based on existing measurements for the source vertex.
            foreach (WifiMeasurement wifi in source.WifiMeasurements)
            {
                //ignore collective measurements (and cancel previous - but don't delete yet)
                if (wifi.Is_Collective == true) //HACK, until we get a better approach (requires schema change and cascading delete, wifi-meas id must be part of prim key of hist)
                {
                    wifi.Is_Collective = false;
                    wifi.Was_Collective = true;
                    continue;
                }
                if (wifi.Was_Collective == true)
                {
                    continue;
                }
                Histogram curHist;
                foreach (Histogram hist in wifi.Histograms)
                {
                    curHist = collectiveWifi.Histograms.FirstOrDefault(h => h.Mac == hist.Mac && h.value == hist.value);
                    if (curHist == null) //insert new histogram (ap, val)
                    {
                        curHist = new Histogram();
                        curHist.Mac = hist.Mac;
                        curHist.value = hist.value;
                        curHist.count = 0;
                        collectiveWifi.Histograms.Add(curHist);
                    }
                    curHist.count += hist.count; //update count for (ap, val)
                }
            }
            return collectiveWifi;
        }
        
        private void updateCollectiveWifiMeasurement(Vertex source)
        {
            if (source == null)
                return;
            if (source.WifiMeasurements.Count < 1)
                return;

            WifiMeasurement collectiveWifi = new WifiMeasurement();
            collectiveWifi.Is_Collective = true;

            //1. Build collective measurement based on existing measurements for the source vertex.
            foreach (WifiMeasurement wifi in source.WifiMeasurements)
            {
                //ignore collective measurements (and cancel previous - but don't delete yet)
                if (wifi.Is_Collective == true) //HACK, until we get a better approach (requires schema change and cascading delete, wifi-meas id must be part of prim key of hist)
                {
                    wifi.Is_Collective = false;
                    wifi.Was_Collective = true;
                    continue;
                }
                if (wifi.Was_Collective == true)
                {
                    continue;
                }
                Histogram curHist;
                foreach (Histogram hist in wifi.Histograms)
                {
                    curHist = collectiveWifi.Histograms.FirstOrDefault(h => h.Mac == hist.Mac && h.value == hist.value);
                    if (curHist == null) //insert new histogram (ap, val)
                    {
                        curHist = new Histogram();
                        curHist.Mac = hist.Mac;
                        curHist.value = hist.value;
                        curHist.count = 0;
                        collectiveWifi.Histograms.Add(curHist);
                    }
                    curHist.count += hist.count; //update count for (ap, val)
                }
            }
            source.WifiMeasurements.Add(collectiveWifi);

            this.CurrentDataSource.SaveChanges();

        }

    }
}
