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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

//import android.text.format.DateFormat;

import android.text.format.DateFormat;

import com.smartcampus.tracking.TrackedPosition;
import com.smartcampus.wifi.WifiMeasurement;

public class BatchUpdater {
		
	private static String batchPost(String categoryTerm, String collectionName, Iterable<String> entries) throws ClientProtocolException, IOException
	{    	
    	String successMsg = "ok";
    	final String batch = "batch_1";
    	final String changeSet = "changeset_77162fcd";
    	final String newLine = "\n";
    	//final String radioMapSvc = ConnectionInfo.SMARTCAMPUS_SERVICE_ROOT_URI;
    	
    	// Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost post = new HttpPost(ConnectionInfo.SMARTCAMPUS_SERVICE_ROOT_URI + "/$batch"); 
	    post.addHeader("content-type", "multipart/mixed;boundary=" + batch);
	    
	    //Create the batch post
        StringBuffer sb = new StringBuffer();
        //batch_1
        sb.append("--").append(batch).append(newLine);
        sb.append("Content-Type: multipart/mixed;boundary=" + changeSet).append(newLine).append(newLine);
                
        for (String content : entries)
        {
	        //changeset_1
	        sb.append("--").append(changeSet).append(newLine);
	        sb.append("Content-Type: application/http").append(newLine);
	        sb.append("Content-Transfer-Encoding: binary").append(newLine).append(newLine);
	        
	        sb.append("POST " + ConnectionInfo.SMARTCAMPUS_SERVICE_ROOT_URI + "/" + collectionName + " HTTP/1.1").append(newLine);
	        sb.append("accept: application/atom+xml").append(newLine);
	        sb.append("Content-Type: application/atom+xml;type=entry").append(newLine).append(newLine);
	        
	        //<AtomPub representation of entity 	        	        
	        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>").append(newLine);	        
	        sb.append("<entry xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\"").append(newLine);
	        sb.append("  xml:base=\"" + ConnectionInfo.SMARTCAMPUS_SERVICE_ROOT_URI + "\"").append(newLine);
	        sb.append("  xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\"").append(newLine);
	        sb.append("  xmlns=\"http://www.w3.org/2005/Atom\">").append(newLine);
	                
	        sb.append("  <title type=\"text\"></title>").append(newLine);    
			sb.append("  <updated>2011-11-27T15:59:56Z</updated>").append(newLine); //dummy date   
			sb.append("  <author>").append("<name/>").append("</author>").append(newLine);   
			sb.append("  <category term=\"radiomapModel." + categoryTerm + "\" scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\" />").append(newLine);
			sb.append("  <content type=\"application/xml\">").append(newLine);
			sb.append("    <m:properties>").append(newLine);
			
			sb.append(content);
						
			sb.append("    </m:properties>").append(newLine);
			sb.append("  </content>").append(newLine);
			sb.append("</entry>").append(newLine).append(newLine);		
        }
        
        //closing delimeters
		sb.append("--").append(changeSet).append("--").append(newLine);
        sb.append("--").append(batch).append("--");
        
        String entity = sb.toString();
        
        //try to post the data
        //May throw ClientProtocolException and IOException
    	StringEntity se = new StringEntity(entity); //UnsupportedEncodingException (subclass af ClientProtocolException?)
    
        post.setEntity(se);
        // Execute HTTP Post Request
        HttpResponse response = httpclient.execute(post); //ClientProtocolException, IOException
        
        //NOTE: WE used to return the response (but we didn't use it for anything upstream)
        //msg = writeResponse(response);	       
	    writeResponse(response);  
        
	    //We will always return "ok" - unless an exception occured
	    return successMsg; 
	}
	
	private static List<String> createBuilding_MacInfoEntries(List<String> macs, int buildingId)
    {		
		//We are just saving the macs currently
		ArrayList<String> result = new ArrayList<String>();
    	
    	final String newLine =  "\n";
    	
    	for (String mac : macs)
    	{
    		StringBuilder sb = new StringBuilder();
    		sb.append("      <d:Building_ID m:type=\"Edm.Int32\">").append(buildingId).append("</d:Building_ID>").append(newLine);
			sb.append("      <d:Mac>").append(mac).append("</d:Mac>").append(newLine);
			    		
    		result.add(sb.toString());
    	}  	
    	
    	return result;
    }
	
	private static List<String> createHistogramEntries(WifiMeasurement w, int wifiId)
    {
    	ArrayList<String> result = new ArrayList<String>();
    	
    	final String newLine =  "\n";
    	
    	Enumeration<String> macs = w.getHistograms().keys();
		while(macs.hasMoreElements()) {
			String mac = macs.nextElement();
			Hashtable<Integer, Integer> h = w.getHistograms().get(mac);
			
			Enumeration<Integer> fingerprintVal = h.keys();
			while(fingerprintVal.hasMoreElements()) {
				int value = fingerprintVal.nextElement();
				int count = h.get(value);
				
				StringBuilder sb = new StringBuilder();
		    	sb.append("      <d:value m:type=\"Edm.Int32\">").append(value).append("</d:value>").append(newLine);
				sb.append("      <d:count m:type=\"Edm.Int32\">").append(count).append("</d:count>").append(newLine);
				sb.append("      <d:WifiMeasurement_ID m:type=\"Edm.Int32\">").append(wifiId).append("</d:WifiMeasurement_ID>").append(newLine);
				sb.append("      <d:Mac>").append(mac).append("</d:Mac>").append(newLine);
				result.add(sb.toString());
			}
		}   	
    	
    	return result;
    }
	
	private static List<String> createTrackedPositionEntries(TrackedPosition[] trackedPositions)
    {
    	ArrayList<String> result = new ArrayList<String>();
    	
    	//note: Is the newline even required??
    	final String newLine =  "\n";
    	
    	TrackedPosition cur;
    	for (int i = 0; i < trackedPositions.length; i++)
    	{
    		cur = trackedPositions[i];
			StringBuilder sb = new StringBuilder();
			sb.append("<d:ID m:type=\"Edm.Int32\">")			.append(cur.getId())		.append("</d:ID>").append(newLine);
			//NOTE: Nulls might have to be handled specially
	    	sb.append("<d:Building_ID  m:type=\"Edm.Int32\">")	.append(cur.getBuildingId()).append("</d:Building_ID>").append(newLine);	
	    	sb.append("<d:VertexID  m:type=\"Edm.Int32\">")		.append(cur.getVertexId())	.append("</d:VertexID>").append(newLine);		
	    	sb.append("<d:Latitude m:type=\"Edm.Double\">")		.append(cur.getLatitude())	.append("</d:Latitude>").append(newLine);			
			sb.append("<d:Longitude m:type=\"Edm.Double\">")	.append(cur.getLongitude())	.append("</d:Longitude>").append(newLine);	
			sb.append("<d:Altitude m:type=\"Edm.Double\">")		.append(cur.getAltitude())	.append("</d:Altitude>").append(newLine);	
			sb.append("<d:Provider>")							.append(cur.getProvider())	.append("</d:Provider>").append(newLine);		
			CharSequence timeStr = DateFormat.format("yyyy-MM-ddTkk:mm:ss.00", cur.getTime());
			sb.append("<d:Time m:type=\"Edm.DateTime\">")		.append(timeStr)			.append("</d:Time>").append(newLine);			
			sb.append("<d:Accuracy m:type=\"Edm.Double\">")		.append(cur.getAccuracy())	.append("</d:Accuracy>").append(newLine);  
			sb.append("<d:Speed  m:type=\"Edm.Double\">")		.append(cur.getSpeed())		.append("</d:Speed>").append(newLine);		
			sb.append("<d:Bearing  m:type=\"Edm.Double\">")		.append(cur.getBearing())	.append("</d:Bearing>").append(newLine);			
			sb.append("<d:HasAccuracy m:type=\"Edm.Boolean\">")	.append(cur.isHasAccuracy()).append("</d:HasAccuracy>").append(newLine);			
			sb.append("<d:HasSpeed m:type=\"Edm.Boolean\">")	.append(cur.isHasSpeed())	.append("</d:HasSpeed>").append(newLine);			
			sb.append("<d:HasBearing m:type=\"Edm.Boolean\">")	.append(cur.isHasBearing())	.append("</d:HasBearing>").append(newLine);			
			sb.append("<d:ClientID>")							.append(cur.getClientId()) 	.append("</d:ClientID>").append(newLine); 
			     
			result.add(sb.toString());			
		}    	
    	return result;
    }
	
	//we currently only save the macs
	//this method may also be dubbed savePermissableAPs
	public static String updateBuilding_MacInfos(List<String> macs, int buildingId)
	{
		String msg = null;
		//WARNING: 
		//
		// After moving from Azure to CS-IT server I received (or at least noticed) the following error:
		// "Error processing request stream. The type name 'radiomapModel.Building_MacInfo' is not valid."
		// Effect: Macs were not added to Building_MacInfos
		// SOLUTION:
		// I have changed categoryTerm 'Building_MacInfo' to 'Building_MacInfos' (plural) 
		// It now works, but beware if/when changing server again. 
		//
		String categoryTerm = "Building_MacInfos";
		String collectionName = "Building_MacInfos";		
		List<String> entries = createBuilding_MacInfoEntries(macs, buildingId);
		if (entries.size() > 0)
		{
			try {
				msg = batchPost(categoryTerm, collectionName, entries);
			} catch (ClientProtocolException e) {
				msg = e.getMessage();
				//e.printStackTrace();
			} catch (IOException e) {
				msg = e.getMessage();
				//e.printStackTrace();
			}
		}
		return msg;
	}
		
	public static String updateHistograms(WifiMeasurement w, int wifiMeasId)
	{
		String msg = null;
		String categoryTerm = "Histogram";
        String collectionName = "Histograms";
        List<String> entries = createHistogramEntries(w, wifiMeasId);
        if (entries.size() > 0)
        {
        	try {
				msg = batchPost(categoryTerm, collectionName, entries);
			} catch (ClientProtocolException e) {
				msg = e.getMessage();
				//e.printStackTrace();
			} catch (IOException e) {
				msg = e.getMessage();
				//e.printStackTrace();
			}        	
        }
        return msg;
	}
	
	public static String updateTrackedPositions(TrackedPosition[] trackedPositions)
	{
		String msg = null;
		String categoryTerm = "TrackedPosition";
        String collectionName = "TrackedPositions";
        List<String> entries = createTrackedPositionEntries(trackedPositions);
        if (entries.size() > 0)
        {
        	try {
				msg = batchPost(categoryTerm, collectionName, entries);
			} catch (ClientProtocolException e) {
				msg = e.getMessage();
				//e.printStackTrace();
			} catch (IOException e) {
				msg = e.getMessage();
				//e.printStackTrace();
			}        	
        }
        return msg;
	}
	
	private static String writeResponse(HttpResponse response) throws IOException {
		IOException ioException = null;
		
		String msg = null;
		BufferedReader in = null;
		try
		{			
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			msg = sb.toString();
		}
		catch (IOException ex)
		{
			ioException = ex;
		}
		finally
	    {
	    	if (in != null)
	    	{
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    	if (ioException != null)
	    	{
	    		throw ioException;
	    	}
	    }    
		return msg;
	}
}
