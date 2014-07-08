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
   public class Edge
	{
		private int id;

		//*********** Properties **************//

		protected Vertex origin;
		protected Vertex destination;

		private int distance;
		private bool _isDirectional;
		private bool _isElevator;
		private bool _isStair;

		public Edge(Vertex origin, Vertex destination)
			:	this(origin, destination,
				(int)com.smartcampus.baselogic.DistanceMeasurements.CalculateMoveddistanceInMeters(
					origin.getLocation().getAbsoluteLocation().getLatitude(), 
					origin.getLocation().getAbsoluteLocation().getLongitude(),
					destination.getLocation().getAbsoluteLocation().getLatitude(),
					destination.getLocation().getAbsoluteLocation().getLongitude()))
		{
		}
	
		public Edge(Vertex origin, Vertex destination, int distance)
		{
			this.origin = origin;
			this.destination = destination;
			this.distance = distance;
		}
	
		public override bool Equals(Object other)
		{
			if (other == null)
				return false;
			if (!(other is Edge))
				return false;
		
			Edge otherEdge = (Edge)other;
			//Cf. the Vertex class for equality (compares absolute locations)
			//Note: We don't compare equality on distance (we just assume that it follows)
			return
				otherEdge.origin.Equals(this.origin) &&
				otherEdge.destination.Equals(this.destination);
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
	
		public override int GetHashCode()
		{
			//We use division to preserve the directional properties
			//That is, there is a difference between e(a, b) and e(b, a)
			return this.origin.GetHashCode() / this.destination.GetHashCode(); 
		}
		/**
		 * @return the isDirectional
		 */
		public bool isDirectional() {
			return _isDirectional;
		}
	
		/**
		 * @return the isElevator
		 */
		public bool isElevator() {
			return _isElevator;
		}
	
		/**
		 * @return the isStair
		 */
		public bool isStair() {
			return _isStair;
		}
	
		public Vertex Opposite(Vertex k)
		{
			if (k.Equals(origin))
				return destination;
			else if (k.Equals(destination))
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
		public void setDirectional(bool isDirectional) {
			this._isDirectional = isDirectional;
		}

		public void setDistance(int value)
		{
			this.distance = value;
		}

		/**
		 * @param isElevator the isElevator to set
		 */
		public void setElevator(bool isElevator) {
			this._isElevator = isElevator;
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
		public void setStair(bool isStair) {
			this._isStair = isStair;
			if (origin != null && destination != null)
			{
				origin.setIsStairEndpoint(isStair);
				destination.setIsStairEndpoint(isStair);
			}
		}	
	}
}
