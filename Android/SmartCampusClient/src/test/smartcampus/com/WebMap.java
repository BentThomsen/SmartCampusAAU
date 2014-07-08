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

package test.smartcampus.com;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.webkit.WebView;

public class WebMap extends Activity {
	private static final String TILES_URL = "http://smartcampus.cs.aau.dk/tiles/sl300/aalborg.php";
	private WebView webView;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.web_map);
        webView = (WebView)findViewById(R.id.web_map);
                
        setupWebView();        
    }
	
	private void setupWebView() {    	
    	webView.getSettings().setJavaScriptEnabled(true);
    	//If you want to be able to call Android methods on this class add the following line
    	//webView.addJavascriptInterface(this, "AndroidInterface");  
    	
    	webView.loadUrl(TILES_URL);
    }	
	
	/**
	 * Shows a 'current location' marker on the map. 
	 * This method is usually called to display a new location estimate arrives.
	 * @param location. The location where the 'current location' marker is displayed.  
	 */
	private void updateNewLocation(Location location) {    	
    	if (location != null) {    		
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
    		
    		webView.loadUrl("javascript:updateNewLocation(" + jsonLoc.toString() + ")");
    	}    	
	}
	
	/**
	 * Centers the map around the specified coordinates
	 * @param lat
	 * @param lon
	 */
	private void centerAt(double lat, double lon) {
		webView.loadUrl("javascript:centerAt(" + lat + ", " + lon + ")");
	}
	
	/**
	 * Changes floor and corresponding tiles. 
	 * In most cases, the ground floor has number 0 and each subsequent floor add 1. 
	 * @param floor
	 */
	private void changeFloor(int floor) {
		webView.loadUrl("javascript:changeFloor( { level: " + floor + " })");
	}
	
	private void testUpdateNewLocationAndCenterAt() {	        
	    Location l = new Location("Dummy Location");
	    l.setLatitude(57.011803);
	    l.setLongitude(9.991251);
	    l.setAltitude(0);
	    updateNewLocation(l);
	    centerAt(l.getLatitude(), l.getLongitude());     
	}
}
