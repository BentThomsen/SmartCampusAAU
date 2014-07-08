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

import android.widget.Toast;

import com.smartcampus.android.ui.maps.WebMap2D;
import com.smartcampus.android.ui.maps.offline.graph.edge.WebMap2DAddEdge;

/**
 * This classes exposes methods that can be called from javascript. 
 * More specifically, this class is used to call appropriate Android logic
 * whenever the user clicks on a marker or on the map of the webview.  
 * 
 * The events are simply forwarded to the appropriate target (online, offline, add/removeEdge)
 * 
 * @author admin
 *
 */
public class DeviceInterface {
	private WebMap2D mTarget;
		
	public DeviceInterface(WebMap2D target) {
		this.mTarget = target;
	}
	
	/**
	 * This method is introduced to allow javascript functions to show 
	 * an alert dialog (it is not possible to call javascript's alert)		
	 * @param msg
	 */
	public void debugAlert(String msg)
	{
		Toast toast = Toast.makeText(mTarget, msg, Toast.LENGTH_LONG);
        toast.show();
	}
	
	/**
	 * This method is called (from javascript) whenever the user taps a marker on the map.
	 * In the online phase we want to show directions to the location, the url of the location and what-not. 
	 * In the offline phase we want to either edit the location or attach a new measurement. 
	 * @param isOnline Indicates whether we are in the online phase
	 * @param floorNum The current floor 
	 * @param vertexId The id of the selected (tapped) vertex.
	 */
	public void onTap(boolean isOnline, int floorNum, int vertexId)
	{
		mTarget.onTap(floorNum, vertexId);
		//Toast.makeText(mTarget, "ID = " + vertexId + ", Floor = " + floorNum + ", isOnline = " + isOnline, Toast.LENGTH_LONG).show();    		
	}
	
	/**
	 * This method is called (from javascript) whenever the user taps on the map - not a marker. 
	 * @param isOnline Indicates whether we are in the online phase. We only handle this call in the offline phase as the tap determines
	 * the location of a new bound location.
	 * @param floor the current floor
	 * @param lat the latitude of the tapped location
	 * @param lon the longitude of the tapped location
	 */
	public void setSelectedLocation(boolean isOnline, int floor, double lat, double lon)
	{
		mTarget.setSelectedLocation(isOnline, floor, lat, lon);
		
		/*
		StringBuilder sb = new StringBuilder();
		sb.append("Is online = ").append(isOnline).append(", ");
		sb.append("Floor = ").append(floor).append(", ");
		sb.append(", Loc = (").append(lat).append(", ").append(lon).append(lon).append(")");
		String text = sb.toString();
		Toast.makeText(mTarget, text, Toast.LENGTH_LONG).show();
		*/
	}
	
	/**
	 * This method is called (from javascript) when the user wants to delete an edge. 
	 * (This is done by clicking on the edge
	 * @param originId
	 * @param destinationId
	 */
	public void removeLink(int originId, int destinationId)
	{
		if (!(mTarget instanceof WebMap2DAddEdge))
			return;
		
		((WebMap2DAddEdge)mTarget).createRemoveLinkConfirmationDialog(
				originId, destinationId).show();
	}	
	
	/**
	 * This method is called (from Javascript) when tiles are loaded,
	 * so we call our own setMapReady which centers at the building and updates overlays.
	 */
	public void setMapReady()
	{	
		mTarget.setMapReady();
		
		/*
		if (!(mTarget instanceof WebMap2DOnline))
			return;
		
		((WebMap2DOnline)mTarget).setMapReady();
		*/
		
	}
}
