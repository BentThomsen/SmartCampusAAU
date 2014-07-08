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

import android.content.ComponentName;      //SmartCampusAAU requires
import android.content.Context;            //SmartCampusAAU requires
import android.content.Intent;             //SmartCampusAAU requires
import android.content.ServiceConnection;  //SmartCampusAAU requires
import android.location.Location;          //SmartCampusAAU requires
import android.location.LocationListener;  //SmartCampus requires
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;                 //SmartCampusAAU requires
import android.util.Log;                   //Used for logging

//Required Smartcampus packages
import com.smartcampus.android.location.LocationService;

public class PositioningClient extends Activity {

//############ THIS REGION IS A TEMPLATE FOR USING THE SMARTCAMPUSAAU LIBRARY ############
	// Guide to using the SmartCampusAAULib library:
	// 1) Bind to LocationService, so you can call methods on the service.
	// 2) Create a ServiceConnection where you define a LocationListener.

    private boolean mIsLocationServiceBound = false;
	private LocationService mLocationService;
	protected static final String LOG_TAG = null;
	
	//This class holds all the callback methods related to positioning updates
	private ServiceConnection mLocationServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.v(LOG_TAG, "Connected to service!");

			// Set listener for location changes and enable indoor positioning
			mLocationService = ((LocationService.LocalBinder)service).getService();
			mLocationService.addLocationListener(mLocationListener);
			mLocationService.enableIndoorPositioning();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.v(LOG_TAG, "Disconnected from service!");

			mLocationService = null;			
		}
	};

	private LocationListener mLocationListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (provider.equalsIgnoreCase(LocationService.PROVIDER_NAME)) {
				switch (status) {
				case LocationService.STATUS_BUILDING_FOUND:
					Log.v(LOG_TAG, "Building Found");
					break;
				case LocationService.STATUS_BUILDING_NOT_FOUND:
					Log.w(LOG_TAG, "Building Not Found");
					break;
				case LocationService.STATUS_CONNECTION_PROBLEM:
					Log.w(LOG_TAG, "Connection Problem");
					break;
				case LocationService.STATUS_RADIOMAP_NOT_DOWNLOADED:
					Log.w(LOG_TAG, "Radio Map Not Downloaded");
					break;
				case LocationService.STATUS_RADIOMAP_DOWNLOADED:
					Log.v(LOG_TAG, "Radio Map Downloaded");
					// break; NOTE: The break is missing intentionally
				case LocationService.STATUS_RADIOMAP_READY:
					Log.v(LOG_TAG, "Radio Map Ready");
					mLocationService.startWifiPositioning();
					break;
				}
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onLocationChanged(Location location) {
			//location.getExtras().getInt("buildingId");
			//location.getExtras().getInt("vertexId");
			
			double lat = location.getLatitude();
			double lon = location.getLongitude();
			//altitude represents the floor of the indoor location
			double floor = location.getAltitude();
			StringBuilder stringLoc = new StringBuilder();
			stringLoc.append("New location: ");
			stringLoc.append("(").append(lat).append(", ").append(lon).append(", ").append(floor).append("");
			Log.v(LOG_TAG, stringLoc.toString());			
		}
	};

	private void disableWifiProvider() {
    	if (mLocationService != null && mLocationService.isDoingWifiPositioning()) {
    		mLocationService.stopWifiPositioning();
    	}
    }

	private void enableWifiProvider() {
		if (mLocationService == null) {
			//calls enableIndoorPositioning upon serviceConnected()
			bindLocationService();
		}
		else {
			//Let's go Joe
			mLocationService.enableIndoorPositioning();
		}
	}

	private void bindLocationService() {
		Log.v(LOG_TAG, "Binding to service...");
		Intent bindIntent = new Intent(this, LocationService.class);
		bindService(bindIntent, mLocationServiceConnection, Context.BIND_AUTO_CREATE);
		mIsLocationServiceBound = true;
	}

	private void unbindLocationService() {
		if (mIsLocationServiceBound) {
			Log.v(LOG_TAG, "Unbinding to service...");
			unbindService(mLocationServiceConnection);
			mIsLocationServiceBound = false;
		}
	}
	
//############ END_REGION - SMARTCAMPUSAAU LIBRARY ############	

//############ THIS REGION SHOWS HOW TO USE INFRASTRUCTURE-BASED POSITIONING ###########

	private boolean mIsInfrastructureServiceBound = false;
	private LocationService mInfrastructureService;
	
	//This class holds all the callback methods related to infrastructure based positioning updates
	private ServiceConnection mInfrastructureServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.v(LOG_TAG, "Connected to service!");

			mInfrastructureService = ((LocationService.LocalBinder)service).getService();
			mInfrastructureService.addLocationListener(mInfrastructureLocationListener);
			if (macAddress == null)
				macAddress = getMacAddress();
			mInfrastructureService.startWifiInfrastructurePositioning(macAddress, 2000);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.v(LOG_TAG, "Disconnected from service!");

			mInfrastructureService = null;			
		}
	};

	private LocationListener mInfrastructureLocationListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onLocationChanged(Location location) {
			double lat = location.getLatitude();
			double lon = location.getLongitude();
			//altitude represents the floor of the indoor location
			double floor = location.getAltitude();
			//The building id tells in which building the user is.
			//This can be used to determine when to display tiles for a new building (when the buildingId changes)
			int buildingId = location.getExtras().getInt("buildingId");
			
			StringBuilder stringLoc = new StringBuilder();
			stringLoc.append("New infrastructure based location: ");
			stringLoc.append("(").append(lat).append(", ").append(lon).append(", ").append(floor).append("");
			stringLoc.append(", building id = ").append(buildingId);			
			
			Log.v(LOG_TAG, stringLoc.toString());
			
		}
	};

	private void enableInfrastructurePositioning() {
		if (mInfrastructureService == null) {
			//calls enableIndoorPositioning upon serviceConnected()
			bindInfrastructureService(); 
		}
		else {
			//Let's go Joe
			if (macAddress == null)
				macAddress = getMacAddress();
			
			mInfrastructureService.startWifiInfrastructurePositioning(macAddress, 2000);
		}
	}
	
	private static String macAddress = "DUMMY_MAC_ADDRESS";
		
	//Get the client's mac address
	private String getMacAddress()
	{
		String mac = null;
		WifiManager wifiMan = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
					
		//Wi-Fi needs to be turned on in order to get mac address
		//If we turn it on, we turn it back off afterwards
		boolean prevWifiStatus = wifiMan.isWifiEnabled();
		boolean curWifiStatus = prevWifiStatus;
		if (prevWifiStatus == false)
		{
			curWifiStatus = wifiMan.setWifiEnabled(true);
		}
		mac = wifiMan.getConnectionInfo().getMacAddress();
		if (curWifiStatus != prevWifiStatus)
			wifiMan.setWifiEnabled(prevWifiStatus);
				
		return mac;		
	}

	private void bindInfrastructureService() {
		Log.v(LOG_TAG, "Binding to service...");
		Intent bindIntent = new Intent(this,  LocationService.class);
		bindService(bindIntent, mInfrastructureServiceConnection, Context.BIND_AUTO_CREATE);
		mIsInfrastructureServiceBound = true;
	}

	private void unbindInfrastructureService() {
		if (mIsInfrastructureServiceBound) {
			Log.v(LOG_TAG, "Unbinding to service...");
			unbindService(mInfrastructureServiceConnection);
			mIsInfrastructureServiceBound = false;
		}
	}	
	
	private boolean exampleUsage()
	{
		if (mInfrastructureService == null)
		{
			bindInfrastructureService();
			return false;
		}
		
		if (macAddress == null)
			macAddress = getMacAddress();
		
		//Using infrastructure based positioning is a three step process:
		//A listener is added once
		mInfrastructureService.addLocationListener(mInfrastructureLocationListener);
		//The service can be started and stopped at will.
		mInfrastructureService.startWifiInfrastructurePositioning(macAddress, 2000);
		//the update interval can be changed as such: mInfrastructureService.setWifiInfrastructurePositioningUpdateInterval(3000);
		mInfrastructureService.stopInfrastructureWifiPositioning();	
		
		return true;
	}
	
//############ END_REGION - INFRASTRUCTURE-BASED POSITIONING ###########
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //1. This is the approach for using the library
        //enableWifiProvider();
        
        //2. This is the approach for starting the SmartCampusAAU app
        //startActivity(new Intent(PositioningClient.this, SmartCampusAAU.class));
        
        //3. This is the approach for using the Sniffer API
        enableInfrastructurePositioning();
        //bindInfrastructureService();
        //exampleUsage();
    }
    
    @Override
	protected void onDestroy() {
		super.onDestroy();

		// Unbind the location service again
		unbindLocationService();
	}
    
    
}