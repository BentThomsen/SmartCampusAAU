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

package com.smartcampus.android.ui.offline.graph;

import java.util.ArrayList;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.smartcampus.R;
import com.smartcampus.android.location.LocationService;
import com.smartcampus.android.ui.Globals;
import com.smartcampus.android.ui.data.BuildingAdapter;
import com.smartcampus.baselogic.DistanceMeasurements;
import com.smartcampus.indoormodel.Building;

public class ChooseBuilding extends ListActivity {
	
	//WARNING: This can be used ONLY by trusted parties to gain access to the full list of buildings
	//This allows remote building manipulation
	public static boolean IS_BACKDOOR_ENABLED;
	
	private static int DISTANCE_THRESHOLD = 2500;
	private static Location currentGpsLocation;
	public static Location getCurrentGpsLocation()
	{
		return currentGpsLocation;
	}
	
	private ProgressDialog createDownloadProgressDialog()
	{
		ProgressDialog res = new ProgressDialog(this);
		res.setIndeterminate(true);
		res.setCancelable(true);
		String msg = "Determining your current GPS location in order to find nearby buildings...\n Please be patient as this may take a few minutes.";  
		res.setMessage(msg);
		return res;
	}
	
	private class BuildingUploadedReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			//Start fresh: Find location and all buildings within range
			clearAvailableBuildings();
			startGpsLocationManger();
		}
	}
	private ListView mListView;
	
	private BuildingAdapter mBuildingAdapter;
	private BuildingUploadedReceiver mReceiver;
	
	
	private ProgressDialog mProgressDialog;
	//Progress dialog that shows while determining a location.  
	private ProgressDialog mDetermineLocationProgressDialog;
	
	private LocationService mLocationService;
		
	private LocationListener mLocationListener;	
	
	private ServiceConnection mLocationServiceConnection = new ServiceConnection()
	{		
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mLocationService = ((LocationService.LocalBinder)service).getService();
			
			// Define a listener that responds to location updates
			mLocationListener = new LocationListener() {
			    public void onLocationChanged(Location location) {
			      // Called when a new location is found by the network location provider.
			      
			    }

			    public void onProviderDisabled(String provider) {}

			    public void onProviderEnabled(String provider) {}

			    public void onStatusChanged(String provider, int status, Bundle extras)
			    {
			    	if (provider.equalsIgnoreCase(LocationService.PROVIDER_NAME))
			    	{
			    		switch (status)
			    		{
			    		case LocationService.STATUS_RADIOMAP_READY:
			    		case LocationService.STATUS_RADIOMAP_DOWNLOADED:
							//Toast.makeText(ChooseBuilding.this, "Radio map downloaded.", Toast.LENGTH_SHORT).show();
							finish();
							break;
			    		case LocationService.STATUS_CONNECTION_PROBLEM:
			    			String connError = "Error connecting";
			    			if (extras != null && extras.getString("Msg") != null)
			    				connError = extras.getString("Msg");
			    			Globals.createErrorDialog(ChooseBuilding.this, "Error", connError).show();			    			
						}
			    		if (mProgressDialog != null)
			    			mProgressDialog.dismiss();
			    	}
			    }
			};
			mLocationService.addLocationListener(mLocationListener);		
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mLocationService = null;			
		}
	};
	
	public void initializeBuildingList()
	{		
		EditBuilding.SELECTED_BUILDING = null;
				
		mListView = getListView();
		mListView.setItemsCanFocus(false);
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view,
		        int position, long id)
		    {
		    	Building b = (Building)mListView.getItemAtPosition(position);
		    	if (mLocationService != null)
		    	{
		    		mLocationService.downloadRadioMapManually(b.getBuildingID());
		    		mProgressDialog = ProgressDialog.show(ChooseBuilding.this, "", "Downloading radio map. \n Please wait...", true);
		    		//Toast.makeText(ChooseBuilding.this, "Downloading radio map. Please wait...", Toast.LENGTH_LONG).show();		    		
		    		
		    	}		    		    			    
		    }
		});	
		clearAvailableBuildings();
	}	
	
	private void clearAvailableBuildings()
	{
		mBuildingAdapter = new BuildingAdapter(this, new ArrayList<Building>());
		mListView.setAdapter(mBuildingAdapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_choose_building, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch (item.getItemId())
    	{
    	case R.id.mi_refresh_available_buildings:
    		clearAvailableBuildings();
    		startGpsLocationManger();
    		return true;
    	case R.id.mi_create_new_building:
    		startActivity(new Intent(ChooseBuilding.this, EditBuilding.class));
    		return true;           
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.offline_choose_from_list);
		
		initializeBuildingList();
		
		if (IS_BACKDOOR_ENABLED) //WARNING: ONLY ENABLE FOR TRUSTED PARTIES
		{
			updateAvailableBuildings();
		}
		else
		{
			//the location manager will find all buildings within range
			startGpsLocationManger();
		}
		Intent bindIntent = new Intent(this, LocationService.class);
		bindService(bindIntent, mLocationServiceConnection, Context.BIND_AUTO_CREATE);
		
		IntentFilter filter;
		filter = new IntentFilter(EditBuilding.BROADCAST_BUILDING_UPLOADED);
		mReceiver = new BuildingUploadedReceiver();
		registerReceiver(mReceiver, filter);
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (mLocationListener != null && mLocationService != null)
			mLocationService.removeLocationListener(mLocationListener);
		if (mLocationService != null)
		{
			try
			{
				unbindService(mLocationServiceConnection);
			}
			catch (Exception ex)
			{
				
			}
		}		
	}
	
	/**
	 * Displays all buildings within range of the location 
	 * @param location
	 * @param range
	 */
	private void updateAvailableBuildings()
	{			
		//get ALL available buildings
		ArrayList<Building> buildings = new ArrayList<Building>();
		for (Building b : LocationService.getAvailableShallowBuildings())
		{
			//WARNING: ONLY FOR TRUSTED PARTIES
			if (IS_BACKDOOR_ENABLED)
			{
				buildings.add(b);
			}
			else
			{
				double distanceToBuilding = 
					DistanceMeasurements.CalculateMoveddistanceInMeters(
							currentGpsLocation.getLatitude(), currentGpsLocation.getLongitude(),
							b.getLatitude(), b.getLongitude()); 
				if (distanceToBuilding < DISTANCE_THRESHOLD) //HEED THE WARNING
				{
					buildings.add(b);
				}
			}
		}		
		mBuildingAdapter = new BuildingAdapter(ChooseBuilding.this, buildings);
		mListView.setAdapter(mBuildingAdapter);		
		if (buildings.size() == 0)
		{
			Toast.makeText(this, "No nearby buildings found", Toast.LENGTH_LONG);
		}
	}
	
	private void disableGpsProvider()
    {
		if (mDetermineLocationProgressDialog.isShowing())
			mDetermineLocationProgressDialog.dismiss();
		
    	if (mLocationManager != null && gpsLocationListener != null)
    		mLocationManager.removeUpdates(gpsLocationListener);
    }
	
	private LocationManager mLocationManager;  
	private final LocationListener gpsLocationListener = new LocationListener() {
	    public void onLocationChanged(Location location) {
	        currentGpsLocation = location;
	        disableGpsProvider(); //Just get one quick fix
	        updateAvailableBuildings();
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {}

	    public void onProviderEnabled(String provider) {}

	    public void onProviderDisabled(String provider) {}
	  };
	private void startGpsLocationManger()
	{
		// Acquire a reference to the system Location Manager
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);		

		// Register the listener with the Location Manager to receive location updates
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);
		if (mDetermineLocationProgressDialog == null)
			mDetermineLocationProgressDialog = createDownloadProgressDialog();
		mDetermineLocationProgressDialog.show();
	}	
}
