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
using System.Collections.Generic;

namespace SmartCampusWebMap.Library.Webclient
{
    /// <summary>
    /// The web client may very well be superfluous on this platform
    /// </summary>
    public interface IWebClient
    {
        //Add a new building and return the id of the new entry
        int addBuilding(Building b);

        int addBuilding_Floor(Building_Floors bf, Building b);

        void addBuilding_Macs(List<String> newMacs, Building b);

        int addEdge(Edge input, Building b);

        //@Deprecated
        //int addMeasurement(Building building, Vertex vertex, WifiMeasurement input);
        //Add a new WifiMeasurement (associated with vertex v) to the server-side radio map and return the id of the new entry
        //int addMeasurement(WifiMeasurement input, Vertex v);

        /// <summary>
        /// The client sends a request to the server that it wishes to add a measurement at the vertex v. 
        /// The client relies on network-based localization where an infrastructure is in place to measure the signal
        /// strength from this device. 
        /// </summary>
        /// <param name="building"></param>
        /// <param name="deviceMac"></param>
        /// <param name="start"></param>
        /// <param name="end"></param>
        void addMeasurement(Building building, Vertex v, String deviceMac, DateTime start, DateTime end);

        //Add a new SymbolicLocation (associated with vertex v) to the server-side radio map and return the id of the new entry
        int addSymbolicLocation(SymbolicLocation input, Vertex v);

        //Add a new vertex (associated with building b) to the server-side radio map and return the id of the new entry
        int addVertex(Vertex input, Building b);

        //Add an existing vertex to the VertexGraveYard - the dummy building where all deleted vertices end up
        void addVertexToGraveYard(int vertexId, int buildingId);

        void deleteEdge(int edgeID);

        void deleteEdge(int source_vertexID, int destination_vertexID);

        //Downloads a radio map (building with graph populated) from a given building id
        Building downloadRadioMap(int buildingId);

        //Determines a building id based on a list of mac addresses
        int getBuildingIdFromMacs(List<String> macs);

        IEnumerable<Building> getShallowBuildings();

        //Updates an existing building on the server-side radio map
        void updateBuilding(Building b);

        void updateBuilding_Floor(Building_Floors input);

        void updateEdge(Edge input);

        //Updates an existing symboliclocation on the server-side radio map
        void updateSymbolicLocation(SymbolicLocation input);

        //Updates an existing vertex on the server-side radio map
        void updateVertex(Vertex input);
    }
}
