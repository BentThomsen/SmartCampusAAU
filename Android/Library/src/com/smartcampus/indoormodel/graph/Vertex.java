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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.smartcampus.indoormodel.AbsoluteLocation;
import com.smartcampus.indoormodel.AggregateLocation;
import com.smartcampus.wifi.WifiMeasurement;

public class Vertex
{    
    private ArrayList<Edge> inEdges = new ArrayList<Edge>();
    private ArrayList<Edge> outEdges = new ArrayList<Edge>();
    
    //id (used for serialization on WinMobile - maybe not needed here)
	private int id;
	
    private AggregateLocation location = new AggregateLocation();
    //The fingerprints attached to the location
    private ArrayList<WifiMeasurement> fingerprints = new ArrayList<WifiMeasurement>();
    //Cf. "the graph paper" regarding the use of radius vertices
    private Set<Vertex> radiusVertices = new HashSet<Vertex>();
  
    private boolean mIsStairEndpoint;
    private boolean mIsElevatorEndpoint;
        
    public static int NEXT_ID = 0;
    public Vertex()
    {
    	
    }
    
    public Vertex(int id, AbsoluteLocation absoluteLocation)
    {
    	this.id = id;
    	this.location = new AggregateLocation(absoluteLocation);
    }
    
    public Vertex(int id, AggregateLocation aggregateLocation)
    {
    	this.id = id;
    	this.location = aggregateLocation;
    }
    
    // Copy constructor
    public Vertex(Vertex aVertex) {
    	
    	double latitude = aVertex.getLocation().getAbsoluteLocation().getLatitude();
    	double longitude = aVertex.getLocation().getAbsoluteLocation().getLongitude();
    	double altitude = aVertex.getLocation().getAbsoluteLocation().getAltitude();
    	
    	AggregateLocation aggLoc = new AggregateLocation(new AbsoluteLocation(latitude, longitude, altitude));
    	this.location = aggLoc;
    	this.inEdges = (ArrayList<Edge>) aVertex.getInEdges();
    	this.outEdges = (ArrayList<Edge>) aVertex.getOutEdges();
    	this.id = aVertex.getId();
    	this.fingerprints = (ArrayList<WifiMeasurement>) aVertex.getFingerPrints();
    	this.radiusVertices = (Set<Vertex>) aVertex.getRadiusVertices();
    }
    
    public boolean addFingerprint(WifiMeasurement value)
    {
    	if (fingerprints == null)
    		return false;
    	if (value == null)
    		return false;
    	
    	return fingerprints.add(value);    	
    }
    
    //Adds an in-edge, and sets the destination of the edge to this vertex
    public boolean addInEdge(Edge e)
    {
    	if (e == null)
    		return false;
    	
    	e.setDestination(this);
    	if (e.isElevator())
    		this.setIsElevatorEndpoint(true);
    	if (e.isStair())
    		this.setIsStairEndpoint(true);
    	
    	if (inEdges.contains(e))
    		return false;
    	return inEdges.add(e);
    }    
    
    public boolean addInEdge(Vertex origin)
    {
    	if (origin == null)
    		return false;
    	
    	Edge e = new Edge(origin, this);
    	return addInEdge(e);    	
    }
    
    //Adds an out edge, and sets the origin explicitly to this vertex
    public boolean addOutEdge(Edge e)
    {
    	if (e == null)
    		return false;
    	
    	e.setOrigin(this);
    	if (e.isElevator())
    		this.setIsElevatorEndpoint(true);
    	if (e.isStair())
    		this.setIsStairEndpoint(true);
    	
    	if (outEdges.contains(e))
    		return false;
    	return outEdges.add(e);
    }
    
    //Out- and in-edges must be in sync
    public boolean addOutEdge(Vertex destination)
    {
    	if (destination == null)
    		return false;
    	
    	Edge e = new Edge(this, destination);
    	return addOutEdge(e);
    }
    
    public boolean addRadiusVertex(Vertex value)
    {
    	//a radius vertex must not be present in the adjacent vertices
    	for (Vertex v : this.adjacentVertices())
    		if (v.equals(value))
    			return false;
    	
       	return radiusVertices.add(value);
    }
     
    public List<Vertex> adjacentVertices()
    {
    	ArrayList<Vertex> result = new ArrayList<Vertex>();
    	result.addAll(opposites(inEdges));
    	result.addAll(opposites(outEdges));
    	return result;
    }
    
    public boolean containsEdge(Edge value)
    {
    	return inEdges.contains(value) || outEdges.contains(value);
    }
    
    public int degree()
    {
    	return inEdges.size() + outEdges.size();
    }
    
    /**
     * @return All vertices that this vertex is pointing to.
     */
    public List<Vertex> destinations()
    {
    	return opposites(outEdges);
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     * We compare vertices by their location, cf. Location.equals()
     */
    @Override
    public boolean equals(Object other)
    {
    	if (other == null)
    		return false;
    	
    	if (!(other instanceof Vertex))
    		return false;
    	
    	//NOTE: CHANGED FROM LOCATION CHECK TO ID CHECK
    	//Quicker
    	return this.getId() == ((Vertex)other).getId();
    	
    	/*
    	AggregateLocation thisLocation = this.getLocation();
    	AggregateLocation otherLocation = ((Vertex)other).getLocation();
    	if (thisLocation == null || otherLocation == null)
    		return false;
    	return thisLocation.equals(otherLocation);
    	*/
    }
    //Wifi measurements taken at this particular location
    public List<WifiMeasurement> getFingerPrints()
    {
        return fingerprints;
    }
    
    public int getId()
    {
    	return this.id;
    }        
    
    public List<Edge> getInEdges()
    {
    	return inEdges;
    }
    
    public AggregateLocation getLocation()
    {
    	return location;
    }
    
    public int getNumFingerprints()
    {
    	return fingerprints.size();
    }    
     
    public List<Edge> getOutEdges()
    {
    	return outEdges;
    }
    
    public Iterable<Vertex> getRadiusVertices()
    {
    	return radiusVertices;        
    }
    
    @Override
    public int hashCode()
    {
    	return this.getLocation().hashCode();
    }    
    
    public List<Edge> incidentEdges()
    {
    	ArrayList<Edge> result = new ArrayList<Edge>();
    	result.addAll(inEdges);
    	result.addAll(outEdges);
    	return result;
    }
    
    //The remaining methods pertain to a vertex's position use. 
    //We don't bother to create a subclass as our vertex has a singular purpose in our application

    public int inDegree()
    {
    	return inEdges.size();
    }
		  
    public boolean isElevatorEndpoint()
    {
    	return this.mIsElevatorEndpoint;
    }
    
    public boolean isStairEndpoint()
    {
    	return mIsStairEndpoint;
    }
    
    //Helper method for origins(), destinations(), and adjacentVertices()
    private ArrayList<Vertex> opposites(Iterable<Edge> edges)
    {
    	ArrayList<Vertex> result = new ArrayList<Vertex>();
    	for (Edge e : edges)
    		result.add(e.Opposite(this));
    	return result;
    }
    
    /**
     * @return All vertices that have an outgoing edge to this vertex
     */
    public List<Vertex> origins()
    {
    	return opposites(inEdges);
    }
    
    public int outDegree()
    {
    	return outEdges.size();
    }
    
    public boolean removeFingerprint(WifiMeasurement value)
    {
    	if (!fingerprints.contains(value))
    		return false;
    	return fingerprints.remove(value);
    }
    
    public boolean removeInEdge(Vertex origin)
    {
    	Edge e = new Edge(origin, this);
    	if (!inEdges.contains(e))
    		return false;
    	return inEdges.remove(e);
    }    	 
    
    public boolean removeOutEdge(Vertex destination)
    {
    	Edge e = new Edge(this, destination);
    	if (!outEdges.contains(e))
    		return false;
    	return outEdges.remove(e);
    }
    
    public boolean RemoveRadiusVertex(Vertex value)
    {
    	return radiusVertices.remove(value);
    }
    
    public void setId(int value)
    {
    	this.id = value;
    }
    
    public void setIsElevatorEndpoint(boolean val)
    {
    	this.mIsElevatorEndpoint = val;
    }
    
    public void setIsStairEndpoint(boolean val)
    {
    	this.mIsStairEndpoint = val;
    }
}