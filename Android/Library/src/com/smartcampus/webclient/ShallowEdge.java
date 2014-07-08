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

import com.smartcampus.indoormodel.graph.Edge;
import com.smartcampus.indoormodel.graph.IGraph;
import com.smartcampus.indoormodel.graph.Vertex;
/**
 * This class is only used as a means to hold the properties as well as 
 * the endpoints' ids. 
 * This is used because vertices may not yet have been constructed
 * @author admin
 *
 */
public class ShallowEdge extends Edge {

	public static void addUndirectionalEdges(IGraph graph, Iterable<ShallowEdge> shallowEdges) {
		Vertex v1, v2;
		Edge e1, e2;
		for (ShallowEdge e : shallowEdges)
		{
			v1 = graph.getVertexById(e.getOriginId());
			v2 = graph.getVertexById(e.getDestinationId());
			
			//extra check - just in case
			//Although there shouldn't get any inconsistencies between vertices and edges 
			//as long as we don't add stuff manually in the db.
			if (v1 == null || v2 == null)
				continue;
			
			//make 2* undirectional edges
			e1 = graph.addDirectionalEdge(v1, v2);
			e2 = graph.addDirectionalEdge(v2, v1);

			//We just use the same id for both directional edges
			//as we don't care about directions
			//It probably makes updates and deletes more efficient 
			e1.setId(e.getId());
			e2.setId(e.getId());
			
			boolean isElevator = e.isElevator();
			e1.setElevator(isElevator);
			e2.setElevator(isElevator);
			if (isElevator)
			{
				graph.addElevatorVertex(v1);
				graph.addElevatorVertex(v2);
			}
			
			boolean isStairs = e.isStair();
			e1.setStair(isStairs);
			e2.setStair(isStairs);
			if (isStairs)
			{
				graph.addStaircaseVertex(v1);
				graph.addStaircaseVertex(v2);
			}
			
			e1.setDirectional(e.isDirectional());
			e2.setDirectional(e.isDirectional());
						
		}
	}
	
	private int mOriginId, mDestinationId;
	
	public ShallowEdge(int originId, int destinationId)
	{
		super(null, null, 0);
		this.mOriginId = originId;
		this.mDestinationId = destinationId;
	}
	public int getDestinationId()
	{
		return mDestinationId;
	}
	
	public int getOriginId()
	{
		return mOriginId;
	}
}
