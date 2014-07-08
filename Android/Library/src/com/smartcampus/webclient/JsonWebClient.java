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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.smartcampus.indoormodel.AbsoluteLocation;
import com.smartcampus.indoormodel.Building;
import com.smartcampus.indoormodel.Building_Floor;
import com.smartcampus.indoormodel.SymbolicLocation;
import com.smartcampus.indoormodel.SymbolicLocation.InfoType;
import com.smartcampus.indoormodel.graph.Edge;
import com.smartcampus.indoormodel.graph.IGraph;
import com.smartcampus.indoormodel.graph.Vertex;
import com.smartcampus.wifi.Histogram;
import com.smartcampus.wifi.WifiMeasurement;

public class JsonWebClient implements IWebClient {

	@Override
	public int addBuilding(Building b) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int addBuilding_Floor(Building_Floor bf, Building b) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addBuilding_Macs(List<String> newMacs, Building b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int addEdge(Edge input, Building b) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int addMeasurement(Building building, Vertex vertex,
			WifiMeasurement input) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int addMeasurement(WifiMeasurement input, Vertex v) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int addSymbolicLocation(SymbolicLocation input, Vertex v) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int addVertex(Vertex input, Building b) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void addVertexToGraveYard(int vertexId, int buildingId) throws Exception {		
		String url = ConnectionInfo.SMARTCAMPUS_SERVICE_ROOT_URI + "AddToVertexGraveyard?buildingId=" + buildingId + "&vertexId=" + vertexId;
		//A succesful request returns 204 (void), so there is no json response to parse
		OpenHttpConnection(url);		
	}

	private ShallowEdge createShallowEdge(JSONObject curEdge) throws JSONException {
		int id = curEdge.getInt("ID");
		int v1_id = curEdge.getInt("vertexOrigin");
		int v2_id = curEdge.getInt("vertexDestination");
		//Account for nullable values
		boolean isDirectional = curEdge.isNull("directional") ? false : curEdge.getBoolean("directional");
		boolean isStair = curEdge.isNull("is_stair") 		  ? false : curEdge.getBoolean("is_stair");
		boolean isElevator = curEdge.isNull("is_elevator")    ? false : curEdge.getBoolean("is_elevator");		
		
		//Account for nullable values
		ShallowEdge newEdge = new ShallowEdge(v1_id, v2_id);
		newEdge.setId(id);
		newEdge.setDirectional(isDirectional);
		newEdge.setStair(isStair);
		newEdge.setElevator(isElevator);
		return newEdge;
	}

	private SymbolicLocation createSymbolicLocation(JSONObject curSymLoc)
			throws JSONException {
		SymbolicLocation newSymbolicLocation = new SymbolicLocation();
		newSymbolicLocation.setId(curSymLoc.getInt("ID"));
		newSymbolicLocation.setTitle(curSymLoc.getString("title"));
		newSymbolicLocation.setDescription(curSymLoc.getString("description"));
		newSymbolicLocation.setUrl(curSymLoc.getString("url"));
		if (!curSymLoc.isNull("is_entrance"))
			newSymbolicLocation.setEntrance(curSymLoc.getBoolean("is_entrance"));
		if (!curSymLoc.isNull("info_type"))
		{
			int infoVal = curSymLoc.getInt("info_type");
			newSymbolicLocation.setType(InfoType.getValue(infoVal));
		}
		else
		{
			newSymbolicLocation.setType(InfoType.NONE);
		}
		return newSymbolicLocation;
	}

	private WifiMeasurement createWifiMeasurement(JSONObject curMeas)
			throws JSONException {
		WifiMeasurement newWifiMeasurement = new WifiMeasurement();
		//"meas_time_start": null,
		//"meas_time_end": null,
		
		JSONArray histograms = curMeas.getJSONArray("Histograms");
		//No need to create histogram objects
		JSONObject curHistogram;
		for (int l = 0; l < histograms.length(); l++)
		{
			curHistogram = histograms.getJSONObject(l);
			int value = curHistogram.getInt("value");
			int count = curHistogram.getInt("count");
			String mac = curHistogram.getString("Mac");
			Histogram newHistogram = new Histogram(-1, mac, value, count);
			newWifiMeasurement.setHistogram(newHistogram);
		}
		return newWifiMeasurement;
	}

	@Override
	public void deleteEdge(int edgeID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteEdge(int source_vertexID, int destination_vertexID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Building downloadRadioMap(int buildingId)
    {
		try
    	{
			Building newBuilding = new Building();
			IGraph g = newBuilding.getGraphModel();
			List<ShallowEdge> shallowEdges = new ArrayList<ShallowEdge>();
			    	
	    	String url = ConnectionInfo.SMARTCAMPUS_SERVICE_ROOT_URI + "RemoveNonCollectiveMeasurements?building_id=" + buildingId + "&$expand=Edges,Vertices,Vertices/AbsoluteLocations,Vertices/SymbolicLocations,Vertices/WifiMeasurements,Vertices/WifiMeasurements/Histograms";
	    	String jsonResponse = OpenHttpConnection(url); 
    		JSONObject root = new JSONObject(jsonResponse);
	        //JSONArray buildings = root.getJSONArray("d");
	        
	        //The building is returned as an array - we will break after the first iteration.
	        JSONObject curBuilding;
	        curBuilding = root.getJSONArray("d").getJSONObject(0);
	        	          	
        	//Read in edges:
        	JSONArray edges = curBuilding.getJSONArray("Edges");
        	JSONObject curEdge;
        	for (int j = 0; j < edges.length(); j++)
        	{
        		//Create shallowEdge
        		curEdge = edges.getJSONObject(j);	
        		
        		ShallowEdge newEdge = createShallowEdge(curEdge);
        		
        		shallowEdges.add(newEdge);
        	}
        	
        	//Read in vertices
        	JSONArray vertices = curBuilding.getJSONArray("Vertices");
        	JSONObject curVertex;
        	for (int j = 0; j < vertices.length(); j++)
        	{
        		curVertex = vertices.getJSONObject(j);
        		
        		JSONObject absoluteLocation = curVertex.getJSONArray("AbsoluteLocations").getJSONObject(0);
        		double lat = absoluteLocation.getDouble("latitude");
        		double lon = absoluteLocation.getDouble("longitude");
        		double alt = absoluteLocation.getDouble("altitude");
        		
        		int id = curVertex.getInt("ID");
        		Vertex newVertex = new Vertex(id, new AbsoluteLocation(lat, lon, alt));	        		
        		
        		//Read in symbolic location
        		JSONArray symbolicLocations = curVertex.getJSONArray("SymbolicLocations");
        		JSONObject curSymLoc;
        		for (int x = 0; x < symbolicLocations.length(); x++) //0..1
        		{
        			curSymLoc = symbolicLocations.getJSONObject(x);    
        			
        			SymbolicLocation newSymbolicLocation = createSymbolicLocation(curSymLoc);  
        			
        			newVertex.getLocation().setSymbolicLocation(newSymbolicLocation);
        		}
        		
        		//Read in measurements:
        		JSONArray measurements = curVertex.getJSONArray("WifiMeasurements");
        		for (int k = 0; k < measurements.length(); k++)
        		{
        			JSONObject curMeas = measurements.getJSONObject(k);
        			
        			WifiMeasurement newWifiMeasurement = createWifiMeasurement(curMeas);
        			
        			newVertex.addFingerprint(newWifiMeasurement);
        			
        		}
        		
        		g.addVertex(newVertex);
        	}
        	
        	ShallowEdge.addUndirectionalEdges(g, shallowEdges);
        	return newBuilding;
    	}
    	catch (Exception ex) 
    	{
    		return null;
    	}    	
    }

	@Override
	public int getBuildingIdFromMacs(List<String> macs) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Iterable<Building> getShallowBuildings()
	   {
		    //Get shallow buildings in json format
		   	String jsonResponse;
		   	try
		   	{
		   		String url = ConnectionInfo.SMARTCAMPUS_SERVICE_ROOT_URI + "Buildings?$expand=Building_MacInfos,Building_Floors";
		   		
		   		jsonResponse = OpenHttpConnection(url);            
		   	}
		   	catch (Exception ex)
		   	{
		   		return null;
		   	}
		   	
		   	//Create the shallow buildings from the json response
		   	ArrayList<Building> res = new ArrayList<Building>();
		   	try
		   	{
		    	JSONObject root = new JSONObject(jsonResponse);
		    	//The root is 'd' for some reason
		    	JSONArray buildings = root.getJSONArray("d");
		        for (int i = 0; i < buildings.length(); i++)
		        {
		        	Building b = new Building();
		        	JSONObject curBuilding = buildings.getJSONObject(i);
		        	b.setBuildingID(curBuilding.getInt("ID"));
		        	b.setName(curBuilding.getString("Building_Name"));
		        	b.setLatitude(curBuilding.getDouble("Lat"));
		        	b.setLongitude(curBuilding.getDouble("Lon"));
		        	b.setIfcUrl(curBuilding.getString("Ifc_Url"));
		        	b.setMaxAddress(curBuilding.getString("Max_Address"));
		        	b.setPostalCode(curBuilding.getString("Postal_Code"));
		        	b.setCountry(curBuilding.getString("Country"));
		        	b.setUrl(curBuilding.getString("Url"));
		        			        	
		        	//read in building floors
		        	JSONArray floors = curBuilding.getJSONArray("Building_Floors");
		        	JSONObject curFloor;
		        	for (int j = 0; j < floors.length(); j++)
		        	{
		        		curFloor = floors.getJSONObject(j);
		        		
		        		Building_Floor bf = new Building_Floor();
			        	bf.setFloorNumber(curFloor.getInt("Number"));
		                bf.setFloorName(curFloor.getString("Name"));	
		                b.addFloor(bf);
		        	}	
		        	
		        	//read in macs (we don't care about the SSID currently)
		        	JSONArray macs = curBuilding.getJSONArray("Building_MacInfos");
		        	JSONObject curMac;
		        	List<String> permissableMacs = new ArrayList<String>();
		        	for (int j = 0; j < macs.length(); j++)
		        	{
		        		curMac = macs.getJSONObject(j);
		        		permissableMacs.add(curMac.getString("Mac"));
		        	}
		        	b.setPermissableAPs(permissableMacs);
		        	
		        	res.add(b);
		        }
		   	}
	       catch (JSONException ex)
	       {
	    	   return null;
	       }
	       return res;
	   }
   
//private String OpenHttpConnection(String urlString) throws Exception	
   public static String OpenHttpConnection(String urlString) throws Exception
 {
     InputStream in = null;
     int response = -1;
     StringBuilder builder = new StringBuilder();			
     String res = null;
     
     URL url = new URL(urlString); 
     URLConnection conn = url.openConnection();
              
     if (!(conn instanceof HttpURLConnection))                     
         throw new IOException("Not an HTTP connection");	     
     
     try{
         HttpURLConnection httpConn = (HttpURLConnection) conn;
         httpConn.setAllowUserInteraction(false);
         httpConn.setInstanceFollowRedirects(true);
         httpConn.setRequestMethod("GET");
         //NOTE: Setting the accept header to 'application/json' is a must!
         httpConn.setRequestProperty("accept", "application/json");
         httpConn.connect(); 

         response = httpConn.getResponseCode();                 
         if (response == HttpURLConnection.HTTP_OK) {
        	 in = httpConn.getInputStream();   
             
             BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			 String line;
             while ((line = reader.readLine()) != null) {
            	 builder.append(line);
             }
             res = builder.toString();
         }
     }
     catch (Exception ex)
     {
         throw ex;            
     }
     return res;     
 }
	
	@Override
	public void updateBuilding(Building b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateBuilding_Floor(Building_Floor input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateEdge(Edge input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateSymbolicLocation(SymbolicLocation input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateVertex(Vertex input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean UploadMeasurement(WifiMeasurement input, Vertex v) {
		// TODO Auto-generated method stub
		return false;
	}

}
