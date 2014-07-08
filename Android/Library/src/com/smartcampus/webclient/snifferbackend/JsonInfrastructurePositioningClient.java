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

package com.smartcampus.webclient.snifferbackend;

import org.json.JSONObject;

import com.smartcampus.webclient.ConnectionInfo;
import com.smartcampus.webclient.JsonWebClient;

public class JsonInfrastructurePositioningClient implements IInfrastructurePositioningService {
		
	@Override
	public boolean startMeasuringAtBoundLocation(String clientMac, int buildingId, int vertexId)
    {		    	
    	//String url = ConnectionInfo.SMARTCAMPUS_SNIFFER_SERVICE_URI + "StartMeasuringAtBoundLocation?clientMac=" + clientMac;
    	StringBuilder sb = new StringBuilder();
    	sb.append(ConnectionInfo.SMARTCAMPUS_SNIFFER_SERVICE_URI);
    	sb.append("StartMeasuringAtBoundLocation");	    	
    	sb.append("?");
    	sb.append("clientMac='").append(clientMac).append("'");
    	sb.append("&buildingId=").append(buildingId);
    	sb.append("&vertexId=").append(vertexId);
    	String url = sb.toString();
    	
    	String jsonResponse = null;
    	try
    	{
    		jsonResponse = JsonWebClient.OpenHttpConnection(url); 
    	}
    	catch (Exception ex)
    	{
    		return false;
    	}
    	
    	//TODO: Inspect jsonResponse
		return jsonResponse != null;	           	
    }

	@Override
	public boolean startMeasuringAtUnboundLocation(String clientMac,
			int buildingId, double lat, double lon, int alt) {
		
		final int E6 = 1000000; //due to potential problem with service using '.'
		int latE6 = (int)(lat * E6);
		int lonE6 = (int)(lon * E6);
		StringBuilder sb = new StringBuilder();
    	sb.append(ConnectionInfo.SMARTCAMPUS_SNIFFER_SERVICE_URI);
    	sb.append("StartMeasuringAtUnboundLocation");	    	
    	sb.append("?");
    	sb.append("clientMac='").append(clientMac).append("'");
    	sb.append("&buildingId=").append(buildingId);
    	sb.append("&latE6=").append(latE6);
    	sb.append("&lonE6=").append(lonE6);
    	sb.append("&alt=").append(alt);
    	String url = sb.toString();
    	
    	String jsonResponse = null;
    	try
    	{
    		jsonResponse = JsonWebClient.OpenHttpConnection(url); 
    	}
    	catch (Exception ex)
    	{
    		return false;
    	}
    	
    	//TODO: Inspect jsonResponse
		return jsonResponse != null;	      
	}

	@Override
	public boolean stopMeasuring(String clientMac) {
		
		StringBuilder sb = new StringBuilder();
    	sb.append(ConnectionInfo.SMARTCAMPUS_SNIFFER_SERVICE_URI);
    	sb.append("StopMeasuring");
    	sb.append("?");
    	sb.append("clientMac='").append(clientMac).append("'");
    	String url = sb.toString();
    	
    	String jsonResponse = null;
    	try
    	{
    		jsonResponse = JsonWebClient.OpenHttpConnection(url); 
    	}
    	catch (Exception ex)
    	{
    		return false;
    	}
    	
    	//TODO: Inspect jsonResponse
		return jsonResponse != null;	
	}

	@Override
	public boolean startWifiPositioning(String clientMac) {
		StringBuilder sb = new StringBuilder();
    	sb.append(ConnectionInfo.SMARTCAMPUS_SNIFFER_SERVICE_URI);
    	sb.append("StartWifiPositioning");
    	sb.append("?");
    	sb.append("clientMac='").append(clientMac).append("'");
    	String url = sb.toString();
    	
    	String jsonResponse = null;
    	try
    	{
    		jsonResponse = JsonWebClient.OpenHttpConnection(url); 
    	}
    	catch (Exception ex)
    	{
    		return false;
    	}
    	
    	//TODO: Inspect jsonResponse
		return jsonResponse != null;
	}

	@Override
	public boolean stopWifiPositioning(String clientMac) {
		StringBuilder sb = new StringBuilder();
    	sb.append(ConnectionInfo.SMARTCAMPUS_SNIFFER_SERVICE_URI);
    	sb.append("StopWifiPositioning");
    	sb.append("?");
    	sb.append("clientMac='").append(clientMac).append("'");
    	String url = sb.toString();
    	
    	String jsonResponse = null;
    	try
    	{
    		jsonResponse = JsonWebClient.OpenHttpConnection(url); 
    	}
    	catch (Exception ex)
    	{
    		return false;
    	}
    	
    	//TODO: Inspect jsonResponse
		return jsonResponse != null;
	}

	@Override
	public boolean saveMeasurement(String clientMac) {
		StringBuilder sb = new StringBuilder();
    	sb.append(ConnectionInfo.SMARTCAMPUS_SNIFFER_SERVICE_URI);
    	sb.append("SaveMeasurement");
    	sb.append("?");
    	sb.append("clientMac='").append(clientMac).append("'");
    	String url = sb.toString();
    	
    	String jsonResponse = null;
    	try
    	{
    		jsonResponse = JsonWebClient.OpenHttpConnection(url); 
    	}
    	catch (Exception ex)
    	{
    		return false;
    	}
    	
    	//TODO: Inspect jsonResponse
		return jsonResponse != null;
	}
	
	@Override
	public InfrastructurePositionEstimate getPosition(String clientMac)
    {
		if (clientMac == null)
			throw new IllegalArgumentException("clientMac must not be null");
		
		InfrastructurePositionEstimate res = new InfrastructurePositionEstimate();
		
		//Build the url
		StringBuilder sb = new StringBuilder();
    	sb.append(ConnectionInfo.SMARTCAMPUS_SNIFFER_SERVICE_URI);
    	sb.append("GetPosition");
    	sb.append("?");
    	sb.append("clientMac='").append(clientMac).append("'");
    	String url = sb.toString();
    	String jsonResponse;
	    
    	//send the request
    	try
	    {
	    	jsonResponse = JsonWebClient.OpenHttpConnection(url); 
	    }
    	catch (Exception ex) 
    	{
    		return null;
    	}  	
		
    	//parse the response
    	try
    	{
	    	JSONObject root = new JSONObject(jsonResponse);
	    	JSONObject obj = root.getJSONObject("d");
	    	res.setId(obj.getInt("ID"));
	    	res.setBuildingId(obj.getInt("Building_ID"));
	    	res.setVertexId(obj.getInt("VertexID"));
	    	res.setLatitude(obj.getDouble("Latitude"));
	    	res.setLongitude(obj.getDouble("Longitude"));
	    	res.setAltitude(obj.getDouble("Altitude"));
	    	res.setProvider(obj.getString("Provider"));
	    	String strDate = obj.getString("Time").substring(6, 19);
	    	res.setTime(Long.parseLong(strDate));
	    	res.setAccuracy(obj.getDouble("Accuracy"));
	    	res.setSpeed(obj.getDouble("Speed"));
	    	res.setBearing(obj.getDouble("Bearing"));
	    	res.setHasAccuracy(obj.getBoolean("HasAccuracy"));
	    	res.setHasSpeed(obj.getBoolean("HasSpeed"));
	    	res.setHasBearing(obj.getBoolean("HasBearing"));
    	}
    	catch (org.json.JSONException ex)
    	{
    		return null;
    	}		
	        
	    return res;  	
    	  	
    }

	@Override
	public InfrastructurePositionEstimate testGetRandomPosition(String clientMac) {
		if (clientMac == null)
			throw new IllegalArgumentException("clientMac must not be null");
		
		InfrastructurePositionEstimate res = new InfrastructurePositionEstimate();
		    	
		StringBuilder sb = new StringBuilder();
    	sb.append(ConnectionInfo.SMARTCAMPUS_SNIFFER_SERVICE_URI);
    	sb.append("TestGetRandomPosition");
    	sb.append("?");
    	int randomBuildingId = 16;
    	sb.append("buildingId=").append(randomBuildingId);
    	String url = sb.toString();
    	String jsonResponse;
    	try
    	{
    		jsonResponse = JsonWebClient.OpenHttpConnection(url); 
    	}
    	catch (Exception ex)
    	{
    		return null;
    	}
    	try
    	{
	    	JSONObject root = new JSONObject(jsonResponse);
	    	JSONObject obj = root.getJSONObject("d");
	    	res.setId(obj.getInt("ID"));
	    	res.setBuildingId(obj.getInt("Building_ID"));
	    	res.setVertexId(obj.getInt("VertexID"));
	    	res.setLatitude(obj.getDouble("Latitude"));
	    	res.setLongitude(obj.getDouble("Longitude"));
	    	res.setAltitude(obj.getDouble("Altitude"));
	    	res.setProvider(obj.getString("Provider"));
	    	String strDate = obj.getString("Time").substring(6, 19);
	    	res.setTime(Long.parseLong(strDate));
	    	res.setAccuracy(obj.getDouble("Accuracy"));
	    	res.setSpeed(obj.getDouble("Speed"));
	    	res.setBearing(obj.getDouble("Bearing"));
	    	res.setHasAccuracy(obj.getBoolean("HasAccuracy"));
	    	res.setHasSpeed(obj.getBoolean("HasSpeed"));
	    	res.setHasBearing(obj.getBoolean("HasBearing"));
    	}
    	catch (org.json.JSONException ex)
    	{
    		return null;
    	}		
        
        return res;  	
    	  	
	}
}
