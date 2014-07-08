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

package com.smartcampus.javascript;

import java.util.List;

import android.location.Location;
import android.webkit.WebView;

import com.smartcampus.android.ui.maps.WebMap2D;
import com.smartcampus.indoormodel.AbsoluteLocation;
import com.smartcampus.indoormodel.SymbolicLocation;
import com.smartcampus.indoormodel.graph.Edge;
import com.smartcampus.indoormodel.graph.Vertex;

/**
 * This class encapsulates all calls that are made to javascript code. 
 * The purpose is simply to have all javascript calls collected in one place
 *  
 * @author admin
 *
 */
public class JSInterface {
	
	public static void centerAt(WebView target, double lat, double lon)
	{
		if (target == null)
			return;
		target.loadUrl("javascript:centerAt(" + lat + ", " + lon + ")");
	}
	
	/**
	 * This method is used (or rather CAN be used) for clearing the overlays (markers) on the map
	 * @param target
	 */
	public static void clearOverlays(WebView target)
	{
		if (target == null)
			return;
		target.loadUrl("javascript:clearOverlays()");		 
	}
	
	private static String createFoliaJsonEdge(Edge e)
	{
		AbsoluteLocation absLoc = e.getOrigin().getLocation().getAbsoluteLocation();
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		
		sb.append("endpoint1: { ");
		sb.append("   id: ").append(e.getOrigin().getId());
		sb.append(", lat: ").append(absLoc.getLatitude());
		sb.append(", lon: ").append(absLoc.getLongitude());		
		sb.append(" }, ");
		absLoc = e.getDestination().getLocation().getAbsoluteLocation();
		sb.append("endpoint2: { ");
		sb.append("   id: ").append(e.getDestination().getId());
		sb.append(", lat: ").append(absLoc.getLatitude());
		sb.append(", lon: ").append(absLoc.getLongitude());		
		sb.append(" } ");
		
		sb.append("}");
		
		return sb.toString();
	}

	private static String createFoliaJsonLocation(Vertex v)
	{
		if (v == null)
			return "null";
		
		AbsoluteLocation absLoc = v.getLocation().getAbsoluteLocation();
		SymbolicLocation symLoc = v.getLocation().getSymbolicLocation();
						
		//[
		// {id: , latitude: , longitude: , altitude: , title: , description: , url: , location_type: }
		// ]
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("id: ").append(v.getId());
		sb.append(", latitude: ").append(absLoc.getLatitude());
		sb.append(", longitude: ").append(absLoc.getLongitude());
		sb.append(", altitude: ").append(absLoc.getAltitude());
		String title = symLoc != null ? symLoc.getTitle() : "null";
		sb.append(", title: ").append("\"").append(title).append("\"");
		String description = symLoc != null ? symLoc.getDescription() : "null";
		sb.append(", description: ").append("\"").append(description).append("\"");
		String url = symLoc != null ? symLoc.getUrl() : "null";
		sb.append(", url: ").append("\"").append(url).append("\"");
		//sb.append(", location_type: ").append(symLoc != null ? symLoc.getType().toString() : "N/A");
		sb.append(", location_type: ").append(symLoc != null ? symLoc.getType().ordinal() : -1);
		sb.append(", isStairEndpoint: ").append(v.isStairEndpoint());
		sb.append(", isElevatorEndpoint: ").append(v.isElevatorEndpoint());
		boolean isEntrance = symLoc != null ? symLoc.isEntrance() : false;
		sb.append(", isEntrance: ").append(isEntrance);
		sb.append("}");
		
		return sb.toString();
	}
	
	/**
	 * This method deselected an endpoint in the add/remove webview
	 */
	public static void removeEndpoint(WebView target, int vertexId)
	{
		if (target == null)
			return;
		target.loadUrl("javascript:removeEndpoint(" + vertexId + ")");
	}
	
	public static void search(WebView target, String query)
	{
		if (target == null)
			return;
		
		target.loadUrl("javascript:search('" + query + "')");
	}
	
	/**
	 * This method is called when a user selects an endpoint in the add/remove webview
	 * @param vertexId
	 */
	public static void setEndpoint(WebView target, int vertexId)
	{
		if (target == null)
			return;
		target.loadUrl("javascript:setEndpoint(" + vertexId + ")");
	}
	
	/**
	 * Tells whether we are tracking the user's current position, i.e., if we want the map to center around it. 
	 * @param target
	 * @param doTrack
	 */
	public static void setIsTracking(WebView target, boolean doTrack)
	{
		if (target == null)
			return;
		String stringTrack = String.valueOf(doTrack);		                           
		target.loadUrl("javascript:setIsTracking(\"" + stringTrack + "\")");
	}
	
	/**
	 * Tells what the current provider is. If the provider is none - we can remove the estimate-circle.
	 * @param target
	 * @param status
	 */
	public static void setProviderStatus(WebView target, int status)
	{
		if (target == null)
			return;
		target.loadUrl("javascript:setProviderStatus('" + status + "')");
	}
	
	/**
	 * This method sends the vertices that needs to be shown on a map.
	 * 
	 * NOTE: The floorNum and isOnline parameters may very well be redundant
	 */
	public static void showEdges(WebView target, List<Edge> edges, int floorNum)
	{
		if (target == null)
			return;
		
		String result;
		if (edges == null)
			result = "[]"; //this is the default json if no edges are available
		else
		{
			StringBuilder foliaEdges = new StringBuilder();
			foliaEdges.append("[");
			for (int i = 0; i < edges.size(); i++)
			{			
				foliaEdges.append(createFoliaJsonEdge(edges.get(i)));
				if (i < edges.size() - 1)
					foliaEdges.append(", ");
			}
			foliaEdges.append("]");
			result = foliaEdges.toString();

		}		
		//Load the vertices 
		target.loadUrl("javascript:showEdges(" + floorNum + ", " + result + ")");
	}
	
	public static void showSelectedLocation(WebView target, double lat, double lon)
	{
		if (target == null)
			return;
		
		target.loadUrl("javascript:showSelectedLocation(" + lat + ", " + lon + ")");
	}
	
	/**
	 * This method sends the vertices that needs to be shown on a map.
	 * 
	 * NOTE: The floorNum and isOnline parameters may very well be redundant
	 */
	public static void showVertices(WebView target, List<Vertex> vertices, int floorNum, boolean isOnline)
	{
		if (target == null)
			return;
		
		String result;
		if (vertices == null)
			result = "[]"; //this is the default json if no vertices are available
		else
		{
			StringBuilder foliaLocations = new StringBuilder();
			foliaLocations.append("[");
			for (int i = 0; i < vertices.size(); i++)
			{			
				foliaLocations.append(createFoliaJsonLocation(vertices.get(i)));
				if (i < vertices.size() - 1)
					foliaLocations.append(", ");
			}
			foliaLocations.append("]");
			result = foliaLocations.toString();

		}		
		//Load the vertices 
		target.loadUrl("javascript:addGraphOverlay(" + floorNum + ", " + isOnline  + ", " + result + ")");					   	
	}	
	
	/**
	 * This method is called when we have a new location estimate. 
	 * @param target The webview where the javascript function is called
	 * @param location The estimated location
	 */
	public static void updateNewLocation(WebView target, Location location)
	{
		if (target == null || location == null)
			return;
		
		//Create json location and send it to javascript
		StringBuilder jsonLoc = new StringBuilder();
		jsonLoc.append("{");
		jsonLoc.append("hasAltitude: ").append(location.hasAltitude()).append(", ");
		jsonLoc.append("hasAccuracy: ").append(location.hasAccuracy()).append(", ");
		jsonLoc.append("hasBearing: ").append(location.hasBearing()).append(", ");
		jsonLoc.append("hasSpeed: ").append(location.hasSpeed()).append(", ");
		jsonLoc.append("accuracy: ").append(location.getAccuracy()).append(", ");
		jsonLoc.append("bearing: ").append(location.getBearing()).append(", ");
		jsonLoc.append("speed: ").append(location.getSpeed()).append(", ");
		jsonLoc.append("latitude: ").append(location.getLatitude()).append(", ");
		jsonLoc.append("longitude: ").append(location.getLongitude()).append(", ");
		jsonLoc.append("altitude: ").append(location.getAltitude()).append(", ");
		jsonLoc.append("time: ").append(location.getTime());
		jsonLoc.append("}");
		target.loadUrl("javascript:updateNewLocation(" + jsonLoc.toString() + ")");
	}

	public static void updateSelectedLocation(WebView target, int xPixels, int yPixels)
	{
		if (target == null)
			return;
		
		target.loadUrl("javascript:updateSelectedLocation(" + xPixels + ", " + yPixels + ")");
	}
	
	
	public static void updateViewType(WebView target, WebMap2D.ViewType viewType)
	{
		if (target == null)
			return;
				
		target.loadUrl("javascript:setViewType('" + viewType.toString() + "')");
	}
}
