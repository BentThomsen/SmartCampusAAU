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
using System.Linq;
using System.Text;

namespace com.smartcampus.indoormodel.graph
{
	public class DictionaryGraph : IGraph {
	//******** Properties ******************//
	//data structure to look up vertex by id
	protected Dictionary<int, Vertex> vertices = new Dictionary<int, Vertex>();
	//data structure to get vertices by floor
	protected Dictionary<int, List<Vertex>> verticesByFloor = new Dictionary<int, List<Vertex>>();
	
	protected List<Edge> edges = new List<Edge>();
	
	protected List<Vertex> starcaseVertices = new List<Vertex>();
	protected List<Vertex> elevatorVertices = new List<Vertex>();

	public Edge addDirectionalEdge(Edge e) {
		Vertex origin = e.getOrigin();
		Vertex destination = e.getDestination();
		origin.addOutEdge(e); //destination
		destination.addInEdge(e); //origin
		
		//Add, if missing vertices
		if (!vertices.ContainsKey(origin.getId()))
			vertices.Add(origin.getId(), origin);
		
		if (!vertices.ContainsKey(destination.getId()))
			vertices.Add(destination.getId(), destination);
		
		if (!edges.Contains(e))
			edges.Add(e);
		
		return e;
	}
	
	//We also add any missing vertices
	public Edge addDirectionalEdge(Vertex origin, Vertex destination)
	{
		Edge e = new Edge(origin, destination);  
		return addDirectionalEdge(e);
	}
	
	public bool addElevatorVertex(Vertex v)
	{
		elevatorVertices.Add(v);
		return true;
	}
	
	public bool addStaircaseVertex(Vertex v)
	{
		starcaseVertices.Add(v);
		return true;
	}
	
	public Edge addUndirectionalEdge(Edge e) {
		Vertex origin = e.getOrigin();
		Vertex destination = e.getDestination();
		origin.addOutEdge(e); 
		destination.addOutEdge(e);
		origin.addInEdge(e);
		destination.addInEdge(e); 
		
		//Add, if missing vertices
		if (!vertices.ContainsKey(origin.getId()))
			vertices.Add(origin.getId(), origin);
		
		if (!vertices.ContainsKey(destination.getId()))
			vertices.Add(destination.getId(), destination);
		
		if (!edges.Contains(e))
			edges.Add(e);
		
		return e;
	}
	
	public IEnumerable<Edge> addUndirectionalEdges(Vertex v1, Vertex v2)
	{
		Edge e1 = addDirectionalEdge(v1, v2);
		Edge e2 = addDirectionalEdge(v2, v1);
		List<Edge> result = new List<Edge>();
		result.Add(e1);
		result.Add(e2);
		return result;
	}
	
	public bool addVertex(Vertex v)
	{
		if (vertices.ContainsKey(v.getId()))
			return false;
					
		//Add vertex to <vertexId, vertex> structure
		vertices.Add(v.getId(), v);
		//Add vertex to <floorNum, List<Vertex> structure
		if (v.getLocation() != null && v.getLocation().getAbsoluteLocation() != null)
		{
			int floor = (int)v.getLocation().getAbsoluteLocation().getAltitude();
			if (!verticesByFloor.ContainsKey(floor))
			{
				verticesByFloor.Add(floor, new List<Vertex>());
			}
			verticesByFloor[floor].Add(v);
		}
		return true;
	}
	
	public IEnumerable<Vertex> adjacentVertices(Vertex v)
	{
		return v.adjacentVertices();
	}
	
	public bool areAdjacent(Vertex v, Vertex w)
	{
		foreach (Vertex cur in v.adjacentVertices())
			if (cur.Equals(w))
				return true;
		return false;
	}
	
	public bool ContainsEdge(Edge e)
	{
		return edges.Contains(e);
	}
	
	public bool ContainsVertex(Vertex v)
	{
		return vertices.ContainsKey(v.getId());
	}
	
	public int degree(Vertex v)
	{
		if (vertices.ContainsValue(v))
			return v.degree();
		else
			return -1;
	}
	
	public IEnumerable<Vertex> destinations(Vertex v)
	{
		return v.destinations();
	}

	public Vertex[] endVertices(Edge e)
	{
		Vertex[] result = new Vertex[2];
		result[0] = e.getOrigin();
		result[1] = e.getDestination();
		return result;
	}
	
	public Vertex getClosestVertex(AbsoluteLocation userAbsLoc) {
		/*
		 * this may not be the best way of determining
		 * the vertex closest to the geopoint - a better solution
		 * could be to arrange the vertices in a different data structure
		 * (currently it is a list)
		 */
		
		int floor = (int)userAbsLoc.getAltitude();
		double dist, bestDist = double.MaxValue;
		Vertex closestVertex = null;
		foreach (Vertex v in this.getVertices(floor)) {
			AbsoluteLocation loc = v.getLocation().getAbsoluteLocation();
			dist = getDistance(loc.getLatitude() * 1E6, 
							   userAbsLoc.getLatitude() * 1E6, 
							   loc.getLongitude() * 1E6, 
							   userAbsLoc.getLongitude() * 1E6);
			
			if(dist < bestDist) {
				bestDist = dist;
				closestVertex = v;
			}
		}
		return closestVertex;
	}
	
	private double getDistance(double x1, double x2, double y1, double y2) {
		return Math.Sqrt(Math.Pow(x1 - x2, 2) + Math.Pow(y1 - y2, 2));
	}
	
	public List<Edge> getEdges()
	{
		return edges;
	}
	
	public List<Edge> getEdges(int floor)
	{
		List<Edge> result = new List<Edge>();
		foreach (Edge e in edges)
		if (e.getOrigin().getLocation().getAbsoluteLocation().getAltitude() == floor ||
			e.getDestination().getLocation().getAbsoluteLocation().getAltitude() == floor)
		{
			result.Add(e);
		}
		return result;
	}
	
	public List<Vertex> getElevatorVertices() {
		return elevatorVertices;
	}
	
	public List<Vertex> getElevatorVertices(int level) {
		List<Vertex> result = new List<Vertex>();
		foreach (Vertex v in elevatorVertices) {
			int vFloor = (int)(v.getLocation().getAbsoluteLocation().getAltitude());
			if (vFloor == level)
				result.Add(v);    		
		}
		return result;
	}
	
	public List<Vertex> getStaircaseVertices() {
		return starcaseVertices;
	}
	
	public List<Vertex> getStaircaseVertices(int level) {
		List<Vertex> result = new List<Vertex>();
		foreach (Vertex v in starcaseVertices) {
			int vFloor = (int)(v.getLocation().getAbsoluteLocation().getAltitude());
			if (vFloor == level)
				result.Add(v);
		}
		return result;
	}
	
	public Vertex getVertexById(int vertexId)
	{
		return vertices[vertexId];
	}

	public IEnumerable<Vertex> getVertices()
	{
        return vertices.Values;        
	}
			
	public List<Vertex> getVertices(int floor)
	{
		if (!verticesByFloor.ContainsKey(floor))
			return null;
		else
			return verticesByFloor[floor]; //may also be null	
	}   
	
	public IEnumerable<Edge> incidentEdges(Vertex v)
	{
		return v.incidentEdges();
	}
	
	public int inDegree(Vertex v)
	{
		return v.inDegree();
	}    
		
	public IEnumerable<Edge> inEdges(Vertex v)
	{
		return v.getInEdges();
	}
	
	// Methods dealing with directed edges //

	//Methods dealing with positioning 
	//We don't bother to create a subclass for this behavior as our graph is only used for one purpose in this application
	public void InsertRadiusVertices(Vertex v, int radius)
	{
		AbsoluteLocation sourceLocation = v.getLocation().getAbsoluteLocation(); 
		AbsoluteLocation targetLocation;
		double dist;
		foreach (Vertex w in vertices.Values)
		{
			if (v.Equals(w))
				continue;
			
			targetLocation = w.getLocation().getAbsoluteLocation(); 
			dist = com.smartcampus.baselogic.DistanceMeasurements.CalculateMoveddistanceInMeters(
					sourceLocation.getLatitude(), sourceLocation.getLongitude(),
					targetLocation.getLatitude(), targetLocation.getLongitude());
			
			if (dist <= radius)
				v.addRadiusVertex((Vertex)w);
		}
	}
	
	public int numEdges()
	{
		return edges.Count;
	}
	
	public int numVertices()
	{
		return vertices.Count;
	}
	
	public Vertex opposite(Edge e, Vertex v)
	{
		return e.Opposite(v);
	}
	
	/**
	 * @return All vertices with an edge going to v
	 */
	public IEnumerable<Vertex> origins(Vertex v)
	{
		return v.origins();
	}
	
	public int outDegree(Vertex v)
	{
		return v.outDegree();
	}        
	
	public IEnumerable<Edge> outEdges(Vertex v)
	{
		return v.getOutEdges();
	}
	
	public bool removeDirectionalEdge(Vertex origin, Vertex destination)
	{
		Edge e = new Edge(origin, destination);    
		//Removes edge if present
		bool sourceMod = 
			origin.removeOutEdge(destination) ||
			destination.removeInEdge(origin);
		return edges.Remove(e) || sourceMod;  
	}
	
	public bool removeElevatorVertex(Vertex v)
	{
		return elevatorVertices.Remove(v);
	}
	
	public bool removeStaircaseVertex(Vertex v)
	{
		return starcaseVertices.Remove(v);
	}
	
	public bool removeUndirectionalEdges(Vertex v1, Vertex v2)
	{
		bool mod1 = removeDirectionalEdge(v1, v2);
		bool mod2 = removeDirectionalEdge(v2, v1);
		return mod1 || mod2;
	}

	public bool removeVertex(Vertex v)
	{
		foreach (Edge e in v.incidentEdges())
			edges.Remove(e);
			
		//remove from <vertexId, vertex> structure
		vertices.Remove(v.getId());
		//remove from <floorNum, List<Vertex> structure
		bool hasLocation = v.getLocation() != null && v.getLocation().getAbsoluteLocation() != null;
		if (hasLocation)
		{
			int floor = (int)v.getLocation().getAbsoluteLocation().getAltitude();
			if (verticesByFloor.ContainsKey(floor))
			{
				verticesByFloor[floor].Remove(v);
			}
		}
		return true;
	}
}
}
