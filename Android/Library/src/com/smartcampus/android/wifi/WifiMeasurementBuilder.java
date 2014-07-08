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

package com.smartcampus.android.wifi;

import java.util.Date;

import java.util.List;

import com.smartcampus.wifi.MacInfo;
import com.smartcampus.wifi.WifiMeasurement;
//import com.smartcampus.wifi.locationprovider.IWifiSniffer;

//import org.redpin.base.core.WiFiMeasurement;
//import org.redpin.base.core.measure.WiFiReading;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;

public class WifiMeasurementBuilder extends Service implements IWifiMeasurementBuilder {
		
	/**
	 * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     *
	 * @see Binder
	 */
	public class LocalBinder extends Binder {
		public WifiMeasurementBuilder getService() {
			return WifiMeasurementBuilder.this;
		}
	}
	//A new measurement is available
	public static final String NEW_MEASUREMENT = "com.stremaspin.android.wifi.NEW_MEASUREMENT";
	public static final String NEW_SCAN = "com.streamspin.android.NEW_SCAN";
	
	//The latest measurement
	WifiMeasurement mLastMeasurement;

	//Indicates whether we are currently measuring, i.e., in the process of creating a new Measurement
	private boolean isStopped = true;
	
	//used to scan and get signal strength information
	private WifiManager mWifi;
	//used to provide a responsive UI (can tell the user how many scans are currently taken)
	//remember, a measurement is created by aggregating information from a number of scan operations. 
	private int mScanNumber;
	
	private Runnable mPeriodicMeasurementTask = new Runnable() {
		   public void run() {
			   //TODO: Consider broadcasting a 'no wifi' message instead of just turning on wifi
			   //mWifi becomes null if a user turns off wifi.
			   if (mWifi == null)
				   initializeWifiManager();
			   
			   //enable wifi
			   if (!mWifi.isWifiEnabled())
				   if (mWifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING)
					   mWifi.setWifiEnabled(true);
			   
			   //create new measurement
			   mScanNumber = 0;
			   mLastMeasurement = new WifiMeasurement();
			   mLastMeasurement.setMeasTimeStart(new Date(System.currentTimeMillis()));
		       
			   //start running
			   while (!isStopped)
		       {
		    	   mWifi.startScan(); //result is received in mResultReceiver's onReceive() method.
		    	   try {
		    		   Thread.sleep(500); //NOTE: Changed from 1000 on 29/5/2013
		    	   }
		    	   catch (InterruptedException e) {
		    		   e.printStackTrace();
		    	   }
		       }
			   mLastMeasurement.setMeasTimeEnd(new Date(System.currentTimeMillis()));
			   
			   //broadcast new measurement available
		       Intent newMeasurement = new Intent(NEW_MEASUREMENT);
			   sendBroadcast(newMeasurement);
		   }
		};

	//
//	/**
//	 * {@link BroadcastReceiver} for retrieving scanning results and add it to a current measurement
//	 * {@link mLastMeasurement}.
//	 */
	private final BroadcastReceiver mResultReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			//Do not listen to broadcast when not initiated scan
			//When no network is available, broadcasts are sent every few seconds
			if(isStopped) {
				return;
			}
			
			List<ScanResult> results = mWifi.getScanResults();
			//WiFiMeasurement measurement = new WiFiMeasurement();

			for (ScanResult result : results) {
				mLastMeasurement.addValue(result.BSSID, result.level, new MacInfo(result));
			}
			
			mScanNumber++;
			Intent scanIntent = new Intent(NEW_SCAN);
			scanIntent.putExtra("ScanNumber", mScanNumber);
			sendBroadcast(scanIntent);			
		}
	};

	// This is the object that receives interactions from clients.
	//More specifically, this is what allow clients to start/stop measuring and 
	//retrieve the result
	private final LocalBinder mBinder = new LocalBinder();
	
	public boolean enableWifi() {
		//if (mWifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
		return mWifi.setWifiEnabled(true); //try to turn on		
	}	

	public WifiMeasurement getLastMeasurement() {
		return mLastMeasurement;
	}
	
	public int getScanNumber()
	{
		return mScanNumber;
	}
	
	public boolean isMeasuring()
	{
		return !isStopped;
	}
	
	public boolean isWifiEnabled() {
		return mWifi != null && mWifi.isWifiEnabled();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}	
	
	@Override
	public void onCreate() {
		initializeWifiManager();
	}	
	
	private void initializeWifiManager()
	{
		//register services from the WifiManager
		mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);	
		//receive results
		registerReceiver(mResultReceiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}
	
	/**
	 * Close threads and unregister services
	 */
	@Override
	public void onDestroy() {
		isStopped = true; //stop measuring
		if (mResultReceiver != null)
		{
			unregisterReceiver(mResultReceiver);
		}
		mWifi = null; //unregister
	}
	
	/**
	 * Start scanning immediately
	 */
	public void startMeasuring() {
		isStopped = false;
		mScanNumber = 1;
		new Thread(mPeriodicMeasurementTask).start();		
	}

	/**
	 * Stop scanning and broadcast new measurements.
	 * (We are not currently broadcasting anything)
	 */
	public void stopMeasuring() {
		isStopped = true;
	}	
}