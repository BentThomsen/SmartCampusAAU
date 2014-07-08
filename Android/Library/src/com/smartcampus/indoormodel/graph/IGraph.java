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

package com.smartcampus.indoormodel.graph;

import java.util.Collection;
import java.util.List;

import com.smartcampus.indoormodel.AbsoluteLocation;

public interface IGraph {
	
    Edge addDirectionalEdge(Edge e);
    
    //We also add any missing vertices
    public Edge addDirectionalEdge(Vertex origin, Vertex destination);
    
    public boolean addElevatorVertex(Vertex v);
    
    public boolean addStaircaseVertex(Vertex v);
    
    Edge addUndirectionalEdge(Edge e);
    
    public Iterable<Edge> addUndirectionalEdges(Vertex v1, Vertex v2);
    
    public boolean addVertex(Vertex v);
    
    public Iterable<Vertex> adjacentVertices(Vertex v);
    
    public boolean areAdjacent(Vertex v, Vertex w);
    
    public boolean ContainsEdge(Edge e);
    
    public boolean ContainsVertex(Vertex v);
    
    public int degree(Vertex v);
    
    public Iterable<Vertex> destinations(Vertex v);
    
    public Vertex[] endVertices(Edge e);
    
    public Vertex getClosestVertex(AbsoluteLocation userAbsLoc);
    
    public List<Edge> getEdges();
    
    public List<Edge> getEdges(int floor);
    
    public List<Vertex> getElevatorVertices();
    
    public List<Vertex> getElevatorVertices(int level);
    
    public List<Vertex> getStaircaseVertices();
    
    public List<Vertex> getStaircaseVertices(int level);
    
    public Vertex getVertexById(int vertexId);
    
    public Collection<Vertex> getVertices();
    
    public List<Vertex> getVertices(int floor);
    
    public Iterable<Edge> incidentEdges(Vertex v);
    
    public int inDegree(Vertex v);
    
    // Methods dealing with directed edges //

    public Iterable<Edge> inEdges(Vertex v);
    
    //Methods dealing with positioning 
    //We don't bother to create a subclass for this behavior as our graph is only used for one purpose in this application
    public void InsertRadiusVertices(Vertex v, int radius);
    
    public int numEdges();
    
    public int numVertices();
    
    public Vertex opposite(Edge e, Vertex v);
    
    /**
     * @return All vertices with an edge going to v
     */
    public Iterable<Vertex> origins(Vertex v);        
    
    public int outDegree(Vertex v);
    
    public Iterable<Edge> outEdges(Vertex v);
    
    public boolean removeDirectionalEdge(Vertex origin, Vertex destination);
    
    public boolean removeElevatorVertex(Vertex v);
    
    public boolean removeStaircaseVertex(Vertex v);

	public boolean removeUndirectionalEdges(Vertex v1, Vertex v2);

	public boolean removeVertex(Vertex v);
}
