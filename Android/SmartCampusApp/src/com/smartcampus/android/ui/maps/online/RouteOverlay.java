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

package com.smartcampus.android.ui.maps.online;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.smartcampus.indoormodel.AbsoluteLocation;
import com.smartcampus.indoormodel.graph.Edge;
import com.smartcampus.indoormodel.graph.Vertex;

public class RouteOverlay {

	//private LinkedList<AbsoluteLocation> route;
	private HashMap<Integer, List<Edge>> edges;	
	
	//NOTE: This is crap performance-wise!!!
	public RouteOverlay(LinkedList<AbsoluteLocation> route) {
		//this.route = route;
		edges = new HashMap<Integer, List<Edge>>();
		Vertex origin, destination;
		for (int i = 0; i < route.size() - 1; i++)
		{
			int curFloor = (int) route.get(i).getAltitude();
			if (!edges.containsKey(curFloor))
			{
				edges.put(curFloor, new ArrayList<Edge>());				
			}
			
			origin = new Vertex(-2, route.get(i));
			destination = new Vertex(-1, route.get(i + 1));
			
			edges.get(curFloor).add(new Edge(origin, destination));
		}
	}	
	
	public List<Edge> getEdges(int floor)
	{
		return edges.get(floor);
	}
	
	
}