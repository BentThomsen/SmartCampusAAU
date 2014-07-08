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

namespace SmartCampusWebMap.Library.Webclient
{
    public class ConcreteWebClient : IWebClient
    {

        public int addBuilding(RadiomapBackend.Building b)
        {
            throw new NotImplementedException();
        }

        public int addBuilding_Floor(RadiomapBackend.Building_Floors bf, RadiomapBackend.Building b)
        {
            throw new NotImplementedException();
        }

        public void addBuilding_Macs(System.Collections.Generic.List<string> newMacs, RadiomapBackend.Building b)
        {
            throw new NotImplementedException();
        }

        public int addEdge(RadiomapBackend.Edge input, RadiomapBackend.Building b)
        {
            throw new NotImplementedException();
        }

        public void addMeasurement(RadiomapBackend.Building building, RadiomapBackend.Vertex v, string deviceMac, DateTime start, DateTime end)
        {
            throw new NotImplementedException();
        }

        public int addSymbolicLocation(RadiomapBackend.SymbolicLocation input, RadiomapBackend.Vertex v)
        {
            /*
            radiomapEntities radiomapContext = LocationService.radiomapContext;
            radiomapContext
            //String expandOptions = "Edges,Building_Floors,Vertices,Vertices/AbsoluteLocations,Vertices/SymbolicLocations";
            String expandOptions = "Building_Floors,Vertices,Vertices/AbsoluteLocations,Vertices/SymbolicLocations,Edges,Edges/Vertices,Edges/Vertices/AbsoluteLocations";
            var query = from b in radiomapContext.Buildings.Expand(expandOptions)
                        where b.ID == 16 //TODO: Hardcoded building id
                        select b;
            */            
            return 1;
        }

        public int addVertex(RadiomapBackend.Vertex input, RadiomapBackend.Building b)
        {
            throw new NotImplementedException();
        }

        public void addVertexToGraveYard(int vertexId, int buildingId)
        {
            throw new NotImplementedException();
        }

        public void deleteEdge(int edgeID)
        {
            throw new NotImplementedException();
        }

        public void deleteEdge(int source_vertexID, int destination_vertexID)
        {
            throw new NotImplementedException();
        }

        public RadiomapBackend.Building downloadRadioMap(int buildingId)
        {
            throw new NotImplementedException();
        }

        public int getBuildingIdFromMacs(System.Collections.Generic.List<string> macs)
        {
            throw new NotImplementedException();
        }

        public System.Collections.Generic.IEnumerable<RadiomapBackend.Building> getShallowBuildings()
        {
            throw new NotImplementedException();
        }

        public void updateBuilding(RadiomapBackend.Building b)
        {
            throw new NotImplementedException();
        }

        public void updateBuilding_Floor(RadiomapBackend.Building_Floors input)
        {
            throw new NotImplementedException();
        }

        public void updateEdge(RadiomapBackend.Edge input)
        {
            throw new NotImplementedException();
        }

        public void updateSymbolicLocation(RadiomapBackend.SymbolicLocation input)
        {
            throw new NotImplementedException();
        }

        public void updateVertex(RadiomapBackend.Vertex input)
        {
            throw new NotImplementedException();
        }
    }
}
