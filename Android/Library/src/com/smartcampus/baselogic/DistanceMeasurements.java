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

package com.smartcampus.baselogic;

    /// <summary>
    /// Encapsulates functionality for calculating the distance between two points
    /// represented by latitude and longitude cordinates
    /// </summary>
    public class DistanceMeasurements
    {
        /// <summary>
        /// The radius of the earth
        /// </summary>
        static final double kEarthRadiusKms = 6376.5; //6371

        /**
         *  Calculates the distance, in kilometers, between the old and new location, each
         * represented by a latitude and a longitude
         */
        public static double CalculateMovedDistanceInKm(double oldLat, double oldLng, double newLat, double newLng) 
        {
            double dDistance = Double.MIN_VALUE;
            double dLat1InRad = oldLat * (Math.PI / 180);
            double dLong1InRad = oldLng * (Math.PI / 180);
            double dLat2InRad = newLat * (Math.PI / 180);
            double dLong2InRad = newLng * (Math.PI / 180);
            double dLongitude = dLong2InRad - dLong1InRad;
            double dLatitude = dLat2InRad - dLat1InRad;
            double a = Math.pow(Math.sin(dLatitude / 2), 2) + Math.cos(dLat1InRad) * Math.cos(dLat2InRad) * Math.pow(Math.sin(dLongitude / 2), 2);
            //Converts rectangular coordinates (x, y) to polar (r, theta). 
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            dDistance = kEarthRadiusKms * c;
            return dDistance;
        }        
        		        
        /**
        /// Calculates the distance, in meters, between the old and new location, each
        /// represented by a latitude and a longitude
        */
        public static double CalculateMoveddistanceInMeters(double oldLat, double oldLng, double newLat, double newLng)
        {
            return CalculateMovedDistanceInKm(oldLat, oldLng, newLat, newLng) * 1000;
        }
        
        /*
        //http://stackoverflow.com/questions/120283/working-with-latitude-longitude-values-in-java
       	public static double haversineDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        	    double earthRadius = 6371; 
        	    double dLat = Math.toRadians(lat2-lat1);
        	    double dLng = Math.toRadians(lng2-lng1);
        	    double sindLat = Math.sin(dLat / 2);
        	    double sindLng = Math.sin(dLng / 2);
        	    double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1) * Math.cos(lat2);
        	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        	    double dist = earthRadius * c;

        	    return dist;
        }
       	
       	public static double haversineDistanceMeters(double lat1, double lng1, double lat2, double lng2)
       	{
       		return haversineDistanceKm(lat1, lng2, lat2, lng2) * 1000;
       	}
       	*/
}