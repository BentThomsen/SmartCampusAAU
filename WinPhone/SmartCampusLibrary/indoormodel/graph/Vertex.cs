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
using com.smartcampus.indoormodel;
using com.smartcampus.wifi;

namespace com.smartcampus.indoormodel.graph
{
	public class Vertex
	{    
		private List<Edge> inEdges = new List<Edge>();
		private List<Edge> outEdges = new List<Edge>();
	
		//id (used for serialization on WinMobile - maybe not needed here)
		private int id;
	
		private AggregateLocation location = new AggregateLocation();
		//The fingerprints attached to the location
		private List<WifiMeasurement> fingerprints = new List<WifiMeasurement>();
		//Cf. "the graph paper" regarding the use of radius vertices
		private HashSet<Vertex> radiusVertices = new HashSet<Vertex>();
  
		private bool mIsStairEndpoint;
		private bool mIsElevatorEndpoint;
		
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
			this.inEdges = aVertex.getInEdges();
			this.outEdges = aVertex.getOutEdges();
			this.id = aVertex.getId();
			this.fingerprints = aVertex.getFingerPrints();
			this.radiusVertices = aVertex.getRadiusVertices();
		}
	
		public bool addFingerprint(WifiMeasurement value)
		{
			if (fingerprints == null)
				return false;
			if (value == null)
				return false;
		
			fingerprints.Add(value);
			return true;
		}
	
		//Adds an in-edge, and sets the destination of the edge to this vertex
		public bool addInEdge(Edge e)
		{
			if (e == null)
				return false;
		
            e.setDestination(this);
			if (e.isElevator())
				this.setIsElevatorEndpoint(true);
			if (e.isStair())
				this.setIsStairEndpoint(true);
		
			if (inEdges.Contains(e))
				return false;
			
            inEdges.Add(e);
            return true;
		}    
	
		public bool addInEdge(Vertex origin)
		{
			if (origin == null)
				return false;
		
			Edge e = new Edge(origin, this);
			return addInEdge(e);    	
		}
	
		//Adds an out edge, and sets the origin explicitly to this vertex
		public bool addOutEdge(Edge e)
		{
			if (e == null)
				return false;
		
			e.setOrigin(this);
			if (e.isElevator())
				this.setIsElevatorEndpoint(true);
			if (e.isStair())
				this.setIsStairEndpoint(true);
		
			if (outEdges.Contains(e))
				return false;
			
            outEdges.Add(e);
            return true;
		}
	
		//Out- and in-edges must be in sync
		public bool addOutEdge(Vertex destination)
		{
			if (destination == null)
				return false;
		
			Edge e = new Edge(this, destination);
			return addOutEdge(e);
		}
	
		public bool addRadiusVertex(Vertex value)
		{
			//a radius vertex must not be present in the adjacent vertices
			foreach (Vertex v in this.adjacentVertices())
				if (v.Equals(value))
					return false;
		
			return radiusVertices.Add(value);
		}
	 
		public List<Vertex> adjacentVertices()
		{
			List<Vertex> result = new List<Vertex>();
			result.AddRange(opposites(inEdges));
			result.AddRange(opposites(outEdges));
			return result;
		}
	
		public bool containsEdge(Edge value)
		{
			return inEdges.Contains(value) || outEdges.Contains(value);
		}
	
		public int degree()
		{
			return inEdges.Count + outEdges.Count;
		}
	
		/**
		 * @return All vertices that this vertex is pointing to.
		 */
		public List<Vertex> destinations()
		{
			return opposites(outEdges);
		}
	
		/// <summary>
		///We compare vertices by their location, cf. Location.equals()
		/// </summary>
		/// <param name="other"></param>
		/// <returns>True, if the two vertices reside at the same (unique) location</returns>
		public override bool Equals(Object other)
		{
			if (other == null)
				return false;
		
			if (!(other is Vertex))
				return false;
		
			AggregateLocation thisLocation = this.getLocation();
			AggregateLocation otherLocation = ((Vertex)other).getLocation();
			if (thisLocation == null || otherLocation == null)
				return false;
			return thisLocation.Equals(otherLocation);
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
			return fingerprints.Count;
		}    
	 
		public List<Edge> getOutEdges()
		{
			return outEdges;
		}
	
		public HashSet<Vertex> getRadiusVertices()
		{
			return radiusVertices;        
		}
	
		public override int GetHashCode()
		{
			return this.getLocation().GetHashCode();
		}    
	
		public List<Edge> incidentEdges()
		{
			List<Edge> result = new List<Edge>();
			result.AddRange(inEdges);
			result.AddRange(outEdges);
			return result;
		}
	
		//The remaining methods pertain to a vertex's position use. 
		//We don't bother to create a subclass as our vertex has a singular purpose in our application

		public int inDegree()
		{
			return inEdges.Count;
		}
		  
		public bool isElevatorEndpoint()
		{
			return this.mIsElevatorEndpoint;
		}
	
		public bool isStairEndpoint()
		{
			return mIsStairEndpoint;
		}
	
		//Helper method for origins(), destinations(), and adjacentVertices()
		private List<Vertex> opposites(IEnumerable<Edge> edges)
		{
			List<Vertex> result = new List<Vertex>();
			foreach (Edge e in edges)
				result.Add(e.Opposite(this));
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
			return outEdges.Count;
		}
	
		public bool removeFingerprint(WifiMeasurement value)
		{
			if (!fingerprints.Contains(value))
				return false;
			return fingerprints.Remove(value);
		}
	
		public bool removeInEdge(Vertex origin)
		{
			Edge e = new Edge(origin, this);
			if (!inEdges.Contains(e))
				return false;
			return inEdges.Remove(e);
		}    	 
	
		public bool removeOutEdge(Vertex destination)
		{
			Edge e = new Edge(this, destination);
			if (!outEdges.Contains(e))
				return false;
			return outEdges.Remove(e);
		}
	
		public bool RemoveRadiusVertex(Vertex value)
		{
			return radiusVertices.Remove(value);
		}
	
		public void setId(int value)
		{
			this.id = value;
		}
	
		public void setIsElevatorEndpoint(bool val)
		{
			this.mIsElevatorEndpoint = val;
		}
	
		public void setIsStairEndpoint(bool val)
		{
			this.mIsStairEndpoint = val;
		}
	}
}
