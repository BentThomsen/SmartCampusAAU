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

import com.smartcampus.baselogic.DistanceMeasurements;

public class Edge
{
    private int id;

    //*********** Properties **************//

    protected Vertex origin;
    protected Vertex destination;

    private int distance;
    private boolean isDirectional;
    private boolean isElevator;
    private boolean isStair;

    public Edge(Vertex origin, Vertex destination)
    {
    	this(origin, destination,
    		(int)DistanceMeasurements.CalculateMoveddistanceInMeters(
    			origin.getLocation().getAbsoluteLocation().getLatitude(), 
    			origin.getLocation().getAbsoluteLocation().getLongitude(),
    			destination.getLocation().getAbsoluteLocation().getLatitude(),
    			destination.getLocation().getAbsoluteLocation().getLongitude()));
    }
    
    public Edge(Vertex origin, Vertex destination, int distance)
    {
    	this.origin = origin;
    	this.destination = destination;
    	this.distance = distance;
    }
    
    @Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (!(other instanceof Edge))
			return false;
		
		Edge otherEdge = (Edge)other;
		//Cf. the Vertex class for equality (compares absolute locations)
		//Note: We don't compare equality on distance (we just assume that it follows)
		return
			otherEdge.origin.equals(this.origin) &&
			otherEdge.destination.equals(this.destination);
	}
    
    public Vertex getDestination()
    {
    	return destination;
    }
    
    //distance in meters
    public int getDistance()
    {
        return distance;
    }
    
    public int getId()
    {
    	return id;
    }
    public Vertex getOrigin()
    {
    	return origin;
    }
    
    @Override
	public int hashCode()
	{
		//We use division to preserve the directional properties
		//That is, there is a difference between e(a, b) and e(b, a)
		return this.origin.hashCode() / this.destination.hashCode(); 
	}
    /**
	 * @return the isDirectional
	 */
	public boolean isDirectional() {
		return isDirectional;
	}
    
    /**
	 * @return the isElevator
	 */
	public boolean isElevator() {
		return isElevator;
	}
    
    /**
	 * @return the isStair
	 */
	public boolean isStair() {
		return isStair;
	}
    
    public Vertex Opposite(Vertex k)
    {
    	if (k.equals(origin))
    		return destination;
    	else if (k.equals(destination))
    		return origin;
    	else
    		return null;
    }
	
	public void setDestination(Vertex destination) {
    	this.destination = destination;
    }

	/**
	 * @param isDirectional the isDirectional to set
	 */
	public void setDirectional(boolean isDirectional) {
		this.isDirectional = isDirectional;
	}

	public void setDistance(int value)
    {
    	this.distance = value;
    }

	/**
	 * @param isElevator the isElevator to set
	 */
	public void setElevator(boolean isElevator) {
		this.isElevator = isElevator;
		if (origin != null && destination != null)
		{
			origin.setIsElevatorEndpoint(isElevator);
			destination.setIsElevatorEndpoint(isElevator);
		}
	}

	public void setId(int value)
    {
    	this.id = value;
    }

	public void setOrigin(Vertex origin) {
    	this.origin = origin;
    }

	/**
	 * @param isStair the isStair to set
	 */
	public void setStair(boolean isStair) {
		this.isStair = isStair;
		if (origin != null && destination != null)
		{
			origin.setIsStairEndpoint(isStair);
			destination.setIsStairEndpoint(isStair);
		}
	}	
}