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

package com.smartcampus.android.ui.maps.building;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.smartcampus.R;
import com.smartcampus.android.ui.Globals;
import com.smartcampus.android.ui.maps.WebMap2D;
import com.smartcampus.android.ui.offline.graph.ChooseBuilding;
import com.smartcampus.indoormodel.graph.Vertex;
import com.smartcampus.javascript.JSInterface;

public class WebMap2DSelectBuildingLocation extends WebMap2D {
	
	private static Location currentGpsLocation;
		
	//callback for when the map has been loaded. 
	protected class SelectMapReadyReceiver extends MapReadyReceiver {		
		@Override
		public void onReceive(Context context, Intent intent) {
			currentGpsLocation = ChooseBuilding.getCurrentGpsLocation();
			if (currentGpsLocation == null)
			{
				startGpsLocationManger();
			}
			else
	    	{
			    //center at and show location
		        JSInterface.centerAt(webView, currentGpsLocation.getLatitude(), currentGpsLocation.getLongitude());
		        setSelectedLocation(false, 0, currentGpsLocation.getLatitude(), currentGpsLocation.getLongitude());
		   }
		}
	}
	
	/**
	 * Create a receiver to handle the 'map ready' signal from javascript. 
	 */
	@Override
	protected MapReadyReceiver createMapReadyReceiver()
	{
		return new SelectMapReadyReceiver();
	}
	
	/**
	 * Do geocoding in the background to find a list of candidate addresses based on the selected coordinates
	 * @author admin
	 *
	 */
	//TODO: Update to new API
	@SuppressLint("NewApi") 
	private class GeocodingTask extends AsyncTask<Location, Void, List<Address>>
    {
		private String errorMsg = "Ok";
		@Override
		protected List<Address> doInBackground(Location... arg0) {
			Location loc = arg0[0];
			if (loc == null)
			{
				errorMsg = "No coordinates were specified";
				return null;
			}
			//do geocoding
			Geocoder gc = new Geocoder(WebMap2DSelectBuildingLocation.this, Locale.getDefault());
			try {
				//We are currently only getting the single best result (as the rest are useless)
				return gc.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);				
			}
			catch (IOException e)
			{
				if (e.getCause() != null)
					errorMsg = e.getCause().getLocalizedMessage();
				else
					errorMsg = e.getLocalizedMessage();
				return null;
			}					
		}
		
		protected void onPostExecute(List<Address> foundAddresses)
		{
			//Remove the progress dialog
			if (mProgressDialog != null)
				mProgressDialog.dismiss();
			
			//Notify user about result
			//If some addresses were found, we let the user choose the most appropriate one
			if (foundAddresses == null)
			{
				Globals.createErrorDialog(WebMap2DSelectBuildingLocation.this, "Error", errorMsg).show();
			}
			else
			{
				if (foundAddresses.size() == 0) {
					Globals.createErrorDialog(WebMap2DSelectBuildingLocation.this, "", "No addresses found").show();
				}
				else
				{
					//createFoundAddressesDialog(foundAddresses).show();
					createFoundAddressDialog(foundAddresses.get(0)).show();
				}			
			}
		}		
    }
	
	public static String BROADCAST_ADDRESS_UPDATED = "com.smartcampus.android.ui.maps.building.BROADCAST_ADDRESS_UPDATED";
	private static Address selectedBuildingAddress;
	
	//Creates a readable format for an android address
	private static String getReadableAddress(Address address)
	{
		StringBuilder sb = new StringBuilder();
		//sb.append("lat: " + address.getLatitude() + ", lon: " + address.getLongitude()).append("\n");
		//if (address.getFeatureName() != null)
		//	sb.append(address.getFeatureName()).append("\n");
		for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
		{			
			sb.append(address.getAddressLine(i)).append("\n");			
		}
		if (address.getCountryName() != null)
			sb.append(address.getCountryName());
				
		return sb.toString();
	}
	
	
	public static Address getSelectedAddress()
	{
		return selectedBuildingAddress;
	}
	private Location selectedBuildingLocation;
			
	private ProgressDialog mProgressDialog;
	
	/**
	 * Displays the result of the geocoding process when we have a single result.
	 * @param addresses. The list of addresses found by the geocoder
	 * @return 
	 */
	private AlertDialog createFoundAddressDialog(Address foundAddress)
	{
		final Address finalFoundAddress = foundAddress;
		final String readableAddress = getReadableAddress(finalFoundAddress);
		
		AlertDialog.Builder confirmDialog = new AlertDialog.Builder(WebMap2DSelectBuildingLocation.this);
    	confirmDialog.setTitle("Use the address below?");
    	confirmDialog.setMessage(readableAddress);
    	confirmDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog, int id) {
	           selectedBuildingAddress = finalFoundAddress;
	           //Toast.makeText(SelectBuildingLocation.this, "Now using \n" + getReadableAddress(selectedBuildingAddress), Toast.LENGTH_LONG).show();
	           sendBroadcast(new Intent(WebMap2DSelectBuildingLocation.BROADCAST_ADDRESS_UPDATED));
	           finish();
	       }
    	});
    	confirmDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog, int id) {
	           dialog.dismiss();
	       }
    	}); 
    	return confirmDialog.create();		
	}	
	
	public void updateLocation(Location l)
	{
		selectedBuildingLocation = l;
		//this.setTitle("Lat: " + selectedBuildingLocation.getLatitude() + ", Lon: " + selectedBuildingLocation.getLongitude());
	}
	
	@Override
	protected boolean addCurrentFloorToFloorChangerDialog() {
		return false;
	}

	@Override
	public int getCurrentFloor() {
		return mCurrentSelectedFloor;
	}

	@Override
	protected List<Vertex> getVisibleVertices(List<Vertex> vertices) {
		return null;
	}

	@Override
	public void onTap(int floorNum, int vertexId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_select_building_location, menu);
				
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	
		switch (item.getItemId())
    	{
		case R.id.mi_get_current_building_location: 
			startGpsLocationManger();
			return true;
    	case R.id.mi_save_building_location:
    		if (selectedBuildingLocation == null)
			{
				Globals.createErrorDialog(WebMap2DSelectBuildingLocation.this, "No location specified", "Please click a location on the map first").show();
			}
			else
			{
				String title = "";
				String msg = "Finding address. Please wait...";
				mProgressDialog = ProgressDialog.show(WebMap2DSelectBuildingLocation.this, title, msg);
				new GeocodingTask().execute(selectedBuildingLocation);
			}
    		
            return true;
    	}
    	return super.onMenuItemSelected(featureId, item);
    	
    }
	
	@Override
	public void setSelectedLocation(boolean isOnline, int floor, double lat, double lon) {
		Location l = new Location("Selected Building Location");
    	l.setLatitude(lat);
    	l.setLongitude(lon);
		updateLocation(l);
		JSInterface.showSelectedLocation(webView, lat, lon);		
	}

	@Override
	public void onCreate(android.os.Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);  
	}
	
	//Progress dialog that shows while determining a location.  
	private ProgressDialog mDetermineLocationProgressDialog;

	private ProgressDialog createDownloadProgressDialog()
	{
		ProgressDialog res = new ProgressDialog(this);
		res.setIndeterminate(true);
		res.setCancelable(true);
		String msg = "Determining your current GPS location...\n Please be patient as this may take a few minutes.";  
		res.setMessage(msg);
		return res;
	}
	
	/**
	 * Note: Getting a gps fix is just duplicate code of the way to get a gps location in the 
	 * ChooseBuilding class.
	 */
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
	    	disableGpsProvider(); //Just get one quick fix
	        
	    	currentGpsLocation = location;
	        //center at and show location
	        JSInterface.centerAt(webView, currentGpsLocation.getLatitude(), currentGpsLocation.getLongitude());
	        setSelectedLocation(false, 0, currentGpsLocation.getLatitude(), currentGpsLocation.getLongitude());
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
