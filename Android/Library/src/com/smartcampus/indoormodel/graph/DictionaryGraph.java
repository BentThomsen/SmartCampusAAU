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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.smartcampus.baselogic.DistanceMeasurements;
import com.smartcampus.indoormodel.AbsoluteLocation;

public class DictionaryGraph implements IGraph {
	//******** Properties ******************//
	//data structure to look up vertex by id
	protected HashMap<Integer, Vertex> vertices = new HashMap<Integer, Vertex>();
    //data structure to get vertices by floor
	protected HashMap<Integer, List<Vertex>> verticesByFloor = new HashMap<Integer, List<Vertex>>();
	
	protected List<Edge> edges = new ArrayList<Edge>();
    
    protected List<Vertex> starcaseVertices = new ArrayList<Vertex>();
    protected List<Vertex> elevatorVertices = new ArrayList<Vertex>();

    @Override
	public Edge addDirectionalEdge(Edge e) {
		Vertex origin = e.getOrigin();
		Vertex destination = e.getDestination();
    	origin.addOutEdge(e); //destination
    	destination.addInEdge(e); //origin
    	
    	//Add, if missing vertices
    	if (!vertices.containsKey(origin.getId()))
    		vertices.put(origin.getId(), origin);
    	
    	if (!vertices.containsKey(destination.getId()))
    		vertices.put(destination.getId(), destination);
    	
    	if (!edges.contains(e))
    		edges.add(e);
    	
    	return e;
	}
    
    //We also add any missing vertices
    public Edge addDirectionalEdge(Vertex origin, Vertex destination)
    {
    	Edge e = new Edge(origin, destination);  
    	return addDirectionalEdge(e);
    }
    
    public boolean addElevatorVertex(Vertex v)
    {
    	return elevatorVertices.add(v);
    }
    
    public boolean addStaircaseVertex(Vertex v)
    {
    	return starcaseVertices.add(v);
    }
    
    @Override
	public Edge addUndirectionalEdge(Edge e) {
		Vertex origin = e.getOrigin();
		Vertex destination = e.getDestination();
    	origin.addOutEdge(e); 
    	destination.addOutEdge(e);
    	origin.addInEdge(e);
    	destination.addInEdge(e); 
    	
    	//Add, if missing vertices
    	if (!vertices.containsKey(origin.getId()))
    		vertices.put(origin.getId(), origin);
    	
    	if (!vertices.containsKey(destination.getId()))
    		vertices.put(destination.getId(), destination);
    	
    	if (!edges.contains(e))
    		edges.add(e);
    	
    	return e;
	}
    
    public Iterable<Edge> addUndirectionalEdges(Vertex v1, Vertex v2)
    {
    	Edge e1 = addDirectionalEdge(v1, v2);
    	Edge e2 = addDirectionalEdge(v2, v1);
    	ArrayList<Edge> result = new ArrayList<Edge>();
    	result.add(e1);
    	result.add(e2);
    	return result;
    }
    
    public boolean addVertex(Vertex v)
    {
    	if (vertices.containsKey(v.getId()))
    		return false;
    	    		
    	//Add vertex to <vertexId, vertex> structure
    	vertices.put(v.getId(), v);
    	//Add vertex to <floorNum, List<Vertex> structure
    	if (v.getLocation() != null && v.getLocation().getAbsoluteLocation() != null)
    	{
    		int floor = (int)v.getLocation().getAbsoluteLocation().getAltitude();
    		if (!verticesByFloor.containsKey(floor))
    		{
    			verticesByFloor.put(floor, new ArrayList<Vertex>());
    		}
    		verticesByFloor.get(floor).add(v);
    	}
    	return true;
    }
    
    public Iterable<Vertex> adjacentVertices(Vertex v)
    {
    	return v.adjacentVertices();
    }
    
    public boolean areAdjacent(Vertex v, Vertex w)
    {
    	for (Vertex cur : v.adjacentVertices())
    		if (cur.equals(w))
    			return true;
    	return false;
    }
    
    public boolean ContainsEdge(Edge e)
    {
    	return edges.contains(e);
    }
    
    public boolean ContainsVertex(Vertex v)
    {
    	return vertices.containsKey(v.getId());
    }
    
    public int degree(Vertex v)
    {
    	if (vertices.containsValue(v))
    		return v.degree();
    	else
    		return -1;
    }
    
    public Iterable<Vertex> destinations(Vertex v)
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
    	double dist, bestDist = Double.MAX_VALUE;
    	Vertex closestVertex = null;
    	for(Vertex v : this.getVertices(floor)) {
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
    	return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
    
    public List<Edge> getEdges()
    {
    	return edges;
    }
    
    public List<Edge> getEdges(int floor)
    {
    	ArrayList<Edge> result = new ArrayList<Edge>();
    	for (Edge e : edges)
		if (e.getOrigin().getLocation().getAbsoluteLocation().getAltitude() == floor ||
			e.getDestination().getLocation().getAbsoluteLocation().getAltitude() == floor)
		{
			result.add(e);
		}
    	return result;
    }
    
    public List<Vertex> getElevatorVertices() {
    	return elevatorVertices;
    }
    
    public List<Vertex> getElevatorVertices(int level) {
    	LinkedList<Vertex> result = new LinkedList<Vertex>();
    	for(Vertex v : elevatorVertices) {
    		int vFloor = (int)(v.getLocation().getAbsoluteLocation().getAltitude());
    		if (vFloor == level)
    			result.add(v);    		
    	}
    	return result;
    }
    
    public List<Vertex> getStaircaseVertices() {
    	return starcaseVertices;
    }
    
    public List<Vertex> getStaircaseVertices(int level) {
    	LinkedList<Vertex> result = new LinkedList<Vertex>();
    	for(Vertex v : starcaseVertices) {
    		int vFloor = (int)(v.getLocation().getAbsoluteLocation().getAltitude());
    		if (vFloor == level)
    			result.add(v);
    	}
    	return result;
    }
    
    public Vertex getVertexById(int vertexId)
    {
    	return vertices.get(vertexId);
    }

    public Collection<Vertex> getVertices()
    {
    	return vertices.values();        
    }
            
    public List<Vertex> getVertices(int floor)
    {
    	if (!verticesByFloor.containsKey(floor))
    		return null;
    	else
    		return verticesByFloor.get(floor); //may also be null    	
    }   
    
    public Iterable<Edge> incidentEdges(Vertex v)
    {
    	return v.incidentEdges();
    }
    
    public int inDegree(Vertex v)
    {
    	return v.inDegree();
    }    
        
    public Iterable<Edge> inEdges(Vertex v)
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
		for (Vertex w : vertices.values())
        {
        	if (v.equals(w))
        		continue;
        	
        	targetLocation = w.getLocation().getAbsoluteLocation(); 
            dist = DistanceMeasurements.CalculateMoveddistanceInMeters(
            		sourceLocation.getLatitude(), sourceLocation.getLongitude(),
            		targetLocation.getLatitude(), targetLocation.getLongitude());
            
            if (dist <= radius)
                v.addRadiusVertex((Vertex)w);
        }
    }
    
    public int numEdges()
    {
    	return edges.size();
    }
    
    public int numVertices()
    {
    	return vertices.size();
    }
    
    public Vertex opposite(Edge e, Vertex v)
    {
    	return e.Opposite(v);
    }
    
    /**
     * @return All vertices with an edge going to v
     */
    public Iterable<Vertex> origins(Vertex v)
    {
    	return v.origins();
    }
    
    public int outDegree(Vertex v)
    {
    	return v.outDegree();
    }        
    
    public Iterable<Edge> outEdges(Vertex v)
    {
    	return v.getOutEdges();
    }
    
    public boolean removeDirectionalEdge(Vertex origin, Vertex destination)
    {
    	Edge e = new Edge(origin, destination);    
    	//Removes edge if present
    	boolean sourceMod = 
    		origin.removeOutEdge(destination) ||
    		destination.removeInEdge(origin);
    	return edges.remove(e) || sourceMod;  
    }
    
    public boolean removeElevatorVertex(Vertex v)
    {
    	return elevatorVertices.remove(v);
    }
    
    public boolean removeStaircaseVertex(Vertex v)
    {
    	return starcaseVertices.remove(v);
    }
    
    public boolean removeUndirectionalEdges(Vertex v1, Vertex v2)
    {
    	boolean mod1 = removeDirectionalEdge(v1, v2);
    	boolean mod2 = removeDirectionalEdge(v2, v1);
    	return mod1 || mod2;
    }

	public boolean removeVertex(Vertex v)
    {
    	for (Edge e : v.incidentEdges())
    		edges.remove(e);
    		
    	//remove from <vertexId, vertex> structure
    	vertices.remove(v.getId());
    	//remove from <floorNum, List<Vertex> structure
    	boolean hasLocation = v.getLocation() != null && v.getLocation().getAbsoluteLocation() != null;
    	if (hasLocation)
    	{
    		int floor = (int)v.getLocation().getAbsoluteLocation().getAltitude();
    		if (verticesByFloor.containsKey(floor))
    		{
    			verticesByFloor.get(floor).remove(v);
    		}
    	}
    	return true;
    }
}
