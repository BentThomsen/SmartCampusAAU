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

package com.smartcampus.webclient;

import java.io.IOException;
import java.util.List;

import com.smartcampus.indoormodel.Building;
import com.smartcampus.indoormodel.Building_Floor;
import com.smartcampus.indoormodel.SymbolicLocation;
import com.smartcampus.indoormodel.graph.Edge;
import com.smartcampus.indoormodel.graph.Vertex;
import com.smartcampus.wifi.WifiMeasurement;

public interface IWebClient {
	//Add a new building and return the id of the new entry
	int addBuilding(Building b);
	
	int addBuilding_Floor(Building_Floor bf, Building b);	
	
	void addBuilding_Macs(List<String> newMacs, Building b);
	
	int addEdge(Edge input, Building b);
	
	int addMeasurement(Building building, Vertex vertex, WifiMeasurement input);
	
	//Add a new WifiMeasurement (associated with vertex v) to the server-side radio map and return the id of the new entry
	int addMeasurement(WifiMeasurement input, Vertex v);
		
	//Add a new SymbolicLocation (associated with vertex v) to the server-side radio map and return the id of the new entry
	int addSymbolicLocation(SymbolicLocation input, Vertex v);
		
	//Add a new vertex (associated with building b) to the server-side radio map and return the id of the new entry
	int addVertex(Vertex input, Building b);
	
	//Add an existing vertex to the VertexGraveYard - the dummy building where all deleted vertices end up
	void addVertexToGraveYard(int vertexId, int buildingId) throws Exception;
	
	void deleteEdge(int edgeID);
		
	void deleteEdge(int source_vertexID, int destination_vertexID);
	
	//Downloads a radio map (building with graph populated) from a given building id
	com.smartcampus.indoormodel.Building downloadRadioMap(int buildingId) throws IOException;
	
	//Determines a building id based on a list of mac addresses
	int getBuildingIdFromMacs(List<String> macs);

	Iterable<Building> getShallowBuildings() throws IOException;

	//Updates an existing building on the server-side radio map
	public void updateBuilding(Building b);

	void updateBuilding_Floor(Building_Floor input);
	
	void updateEdge(Edge input);

	//Updates an existing symboliclocation on the server-side radio map
	void updateSymbolicLocation(SymbolicLocation input);

	//Updates an existing vertex on the server-side radio map
	void updateVertex(com.smartcampus.indoormodel.graph.Vertex input);

	boolean UploadMeasurement(WifiMeasurement input, Vertex v);

}
