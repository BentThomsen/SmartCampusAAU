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

public class ODataProperties {

	public static class AbsoluteLocation
	{
		public static final String ENTITY_NAME 	= "AbsoluteLocations";
		public static final String ID 		 	= "d:ID";
		public static final String VERTEX_ID	= "d:Vertex_ID";
		public static final String LATITUDE  	= "d:latitude";
		public static final String LONGITUDE 	= "d:longitude";
		public static final String ALTITUDE  	= "d:altitude";
	}

	public static class Building
	{
		public static final String ENTITY_NAME  = "Buildings";
		public static final String ID 			= "d:ID";
		public static final String NAME 		= "d:Building_Name";
		public static final String IFC_URL 		= "d:Ifc_Url";
		public static final String LATITUDE 	= "d:Lat";
		public static final String LONGITUDE 	= "d:Lon";
		public static final String COUNTRY 		= "d:Country";
		public static final String POSTAL_CODE 	= "d:Postal_Code";
		public static final String MAX_ADDRESS 	= "d:Max_Address";
		public static final String URL 			= "d:Url";
	}

	public static class BuildingFloors
	{
		public static final String ENTITY_NAME  = "Building_Floors";
		public static final String ID 			= "d:ID";
		public static final String FLOOR_NUMBER = "d:Number";
		public static final String FLOOR_NAME	= "d:Name";    		
	}

	public static class Edge
	{
		public static final String ENTITY_NAME 			= "Edges";
		public static final String ID 		   			= "d:ID";
		public static final String BUILDING_ID	 		= "d:Building_ID";
		public static final String IS_DIRECTIONAL 		= "d:directional";
		public static final String VERTEX_ORIGIN    	= "d:vertexOrigin";
		public static final String VERTEX_DESTINATION   = "d:vertexDestination";
		public static final String IS_STAIR             = "d:is_stair";
		public static final String IS_ELEVATOR          = "d:is_elevator";
	}

	public static class Histogram
	{
		public static final String ENTITY_NAME	   = "Histograms";
		public static final String ID 		       = "d:ID";
		public static final String MEASUREMENT_ID  = "d:WifiMeasurement_ID";
		public static final String MAC             = "d:Mac";
		public static final String VALUE           = "d:value";
		public static final String COUNT           = "d:count"; 			
	}

	public static class SymbolicLocation
	{
		public static final String ENTITY_NAME = "SymbolicLocations";
		public static final String ID 		   = "d:ID";
		public static final String VERTEX_ID   = "d:Vertex_ID";
		public static final String TITLE       = "d:title";
		public static final String DESCRIPTION = "d:description";
		public static final String URL         = "d:url";
		public static final String IS_ENTRANCE = "d:is_entrance";
		public static final String INFO_TYPE   = "d:info_type";
	}

	public static class Vertex
	{
		public static final String ENTITY_NAME = "Vertices";
		public static final String ID 		   = "d:ID";
		public static final String BUILDING_ID = "d:Building_ID";		
	}

	public static class WifiMeasurement
	{
		public static final String ENTITY_NAME 	   = "WifiMeasurements";
		public static final String ID 		       = "d:ID";
		public static final String VERTEX_ID       = "d:Vertex_ID";
		public static final String MEAS_TIME_START = "d:meas_time_start";
		public static final String MEAS_TIME_END   = "d:meas_time_end";			
	}

}
