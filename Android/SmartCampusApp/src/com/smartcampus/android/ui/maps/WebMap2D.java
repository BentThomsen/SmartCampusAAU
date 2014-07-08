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

package com.smartcampus.android.ui.maps;

import java.util.ArrayList;
import java.util.List;

//import org.joda.time.DateTime;

import com.smartcampus.R;
import com.smartcampus.android.ui.data.Building_FloorAdapter;
import com.smartcampus.android.ui.data.VertexAdapter;
import com.smartcampus.indoormodel.AbsoluteLocation;
import com.smartcampus.indoormodel.Building;
import com.smartcampus.indoormodel.Building_Floor;
import com.smartcampus.indoormodel.graph.Edge;
import com.smartcampus.indoormodel.graph.IGraph;
import com.smartcampus.indoormodel.graph.Vertex;
import com.smartcampus.javascript.JSInterface;
import com.smartcampus.javascript.DeviceInterface;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;

public abstract class WebMap2D extends Activity {
	
		
	//callback for when the map has been loaded. 
	protected class MapReadyReceiver extends BroadcastReceiver {		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mCurrentBuilding != null) 
	    	{
				JSInterface.centerAt(webView, mCurrentBuilding.getLatitude(), mCurrentBuilding.getLongitude());
	    	}	    	
			WebMap2D.this.refreshUI();
		}
	}
	
	public enum ViewType {
		MAP, SATELLITE, STREET, TRAFFIC;
		
		/**
		 * @param index Index of desired infotype value
		 * @return Returns the type corresponding to the specified index.
		 * If no type is found, the default (NONE) is returned.  
		 */
	    public static ViewType getValue(int index)
	    {
	    	int i = 0;
	    	for (ViewType vt : ViewType.values())
	    	{
	    		if (i == index)
	    			return vt;
	    		i++;
	    	}
	    	return MAP;
	    }
	}	
	
	//Denotes the last known location estimate - regardless of provider
	protected static Location lastKnownLocation;
    private static final String DEFAULT_TILE_URL = "http://smartcampus.cs.aau.dk/tiles/sl300/no_tiles.php"; //TODO: Add folia maps url
                  
    protected WebView webView;
    //private Button button1;
    //private Button button2;
	/* Start of Map2D */
    protected static final String emptyTitle = "No Title";
    
	protected static final String emptyDescription = "No Description";
	protected static Building mCurrentBuilding;
	protected static IGraph mGraph;		
	protected static int mCurrentSelectedFloor = 0;
	protected static ViewType mCurrentViewType = ViewType.MAP;
	
	
	//TODO: FIGURE OUT IF WE CAN DO WITHOUT THE MAPVIEW
	//protected MapView mMapView;	
	
	//When pressed will display the current (i.e., estimated) floor (only visible in online mode)
	//protected ImageButton mCurrentFloorBtn;
    	
	protected static String mCurrentTileUrl;	
	
	public static final String IS_MAP_READY = "com.smartcampus.android.ui.maps.WebMap2D.IS_MAP_READY";
    
	//For efficient bitmap 
    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	        if (width > height) {
	            inSampleSize = Math.round((float)height / (float)reqHeight);
	        } else {
	            inSampleSize = Math.round((float)width / (float)reqWidth);
	        }
	    }
	    return inSampleSize;
	}
	
	//For efficient bitmap
	private static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}

	protected ImageButton mTrackPositionBtn;    
    
    /** SPLASH SECTION START **/
        
    protected Dialog mSplashDialog;
       
	//Receiver for when map tiles have been loaded (called from javascript - another thread)
    private MapReadyReceiver mMapReadyReceiver;
	
	//Indicates whether the floor changer dialog should contain a 'current' floor
	protected abstract boolean addCurrentFloorToFloorChangerDialog();
	
	//This method is called to show edges (IFF edges should be shown)
	protected void addEdgeOverlay(int floorNum)
	{
		List<Edge> edges = null;
		if (mGraph != null && showEdges())
		{
			//This could be changed to a route in the online phase (so akin to getVisibleVertices()
			edges = getVisibleEdges(floorNum); //mGraph.getEdges(floorNum);
		}
		JSInterface.showEdges(webView, edges, floorNum);
	}

	//NOTE: Consider refactoring
	//Change name to 'ShowVertices'
	//This method is called to show the vertices
	protected void addGraphOverlay(int floorNum)
    {
		List<Vertex> verts = null;
		if (mGraph != null)
		{
			verts = getVisibleVertices(mGraph.getVertices(floorNum));
		}
		JSInterface.showVertices(webView, verts, floorNum, isOnline());	   	
    }    
    
    protected void changeToFloor(int newFloor)
	{
		if (mGraph != null)
        {
			mCurrentSelectedFloor = newFloor;
	    	refreshUI();
        }
	}
    
    
    //Clear map overlays   
	protected void clearOverlays()
	{
		JSInterface.clearOverlays(webView);	 
	}
    
	//Concatenates building name and floor name, to give an overall indication of where a user is.
    protected String concatBuildingAndFloorName(int floorNum)
    {
    	if (mCurrentBuilding == null)
    		return " Unknown building ";
    	
		String building_name = mCurrentBuilding.getName();
		if (building_name == null)
			return "Unknown Building";
		else
		{
			Building_Floor floor = mCurrentBuilding.getFloorAtFloorNumber(floorNum);
			String floor_name;
			if (floor == null)
				floor_name = "Unknown floor";
			else
				floor_name = floor.getFloorName() != null 
					? floor.getFloorName()
					: "floor #" + floor.getFloorNumber();
			
			return building_name + " - " + floor_name;
		}
	}
	
	/**
     * Creates the 'Change floor' dialog which allows a user to select a different floor.
     * @return A dialog displaying available floors. 
     */
	protected AlertDialog createFloorChangerDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		//Add available floors
		ArrayList<Building_Floor> floors = new ArrayList<Building_Floor>();
		if (mCurrentBuilding == null || (!mCurrentBuilding.hasFloors()))
		{
			builder.setTitle("No available floors");
			floors = new ArrayList<Building_Floor>(); //empty list
		}
		else
		{
			builder.setTitle("Choose a new floor. \n (Current floor: " + getCurrentFloor() + ")");
			Iterable<Building_Floor> curFloors = mCurrentBuilding.getFloors();
			for (Building_Floor bf : curFloors)
			{
				floors.add(bf);
			}			
		}
		
		//Add floors to adapter
		final ArrayList<Building_Floor> finalFloors = floors; 
		builder.setAdapter(new Building_FloorAdapter(this, floors), new DialogInterface.OnClickListener() {
		    @Override
			public void onClick(DialogInterface dialog, int item) {		        
		    	changeToFloor(finalFloors.get(item).getFloorNumber());		    	
		    }
		});			
    	return builder.create();		
    }
	
	private LinearLayout createSplashLayout()
    {
    	try
    	{
    		LinearLayout lLayout = new LinearLayout(this);
	        lLayout.setOrientation(LinearLayout.VERTICAL);
	        LayoutParams layout = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	        lLayout.setLayoutParams(layout);
	        
	        Display display = getWindowManager().getDefaultDisplay();
	        int screenWidth = display.getWidth();
	        int screenHeight = display.getHeight();
	                        
	        ImageView imgView = new ImageView(this);
	        imgView.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.startskaerm_light, (int)(screenWidth * 1.3), (int)(screenHeight * 1.3)));
	        imgView.setScaleType(ScaleType.CENTER_CROP);
	        //imgView.setAdjustViewBounds(true); // set the ImageView bounds to match the Drawable's dimensions
	        imgView.setLayoutParams(layout); //.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	                
	        lLayout.addView(imgView);        
	        return lLayout;
    	}
    	catch (Exception ex)
    	{
    		return null;
    	}
    }	
	
	protected AlertDialog createViewDialog() 
	{
		//We present the user with different options for annotating the edge
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose view");
		String[] viewOptions = new String[4];
		viewOptions[0] = "Map view";
		viewOptions[1] = "Satellite view";
		viewOptions[2] = "Street view";
		viewOptions[3] = "Traffic view";
				
		builder.setItems(viewOptions, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	
		    	mCurrentViewType = ViewType.getValue(item);
		    	updateViewType();
		    }		    
		});
		return builder.create();
	}
	
	//Create the 'What's nearby' dialog
    public AlertDialog createWhatsNearbyDialog() {
    	List<Vertex> floorVerts = new ArrayList<Vertex>();
    	if (mGraph != null)
    	{
    		floorVerts = getVisibleVertices(
    				mGraph.getVertices(mCurrentSelectedFloor));
    	}
    	//HACK: This MIGHT happen (e.g., floor number is 0, but we don't have any building floors on 0)
    	//We return an empty list (no POIs)
    	//A better approach would be to disallow non-existing floor numbers altogether or at least diable the 'show nearby' button. 
    	if (floorVerts == null)
    		floorVerts = new ArrayList<Vertex>();
    	final List<Vertex> verticesOnCurrentFloorList = floorVerts;
    	    		
        AlertDialog.Builder whatsNearbyDialogBuilder = new AlertDialog.Builder(this);
        whatsNearbyDialogBuilder.setTitle("Nearby places of interest");
        
    	whatsNearbyDialogBuilder.setAdapter(new VertexAdapter(this, verticesOnCurrentFloorList), new android.content.DialogInterface.OnClickListener() {
			
			//@Override
			public void onClick(DialogInterface dialog, int which) {
    			Vertex selectedVertex = verticesOnCurrentFloorList.get(which);
    			
				AbsoluteLocation absLoc = selectedVertex.getLocation().getAbsoluteLocation();
				JSInterface.centerAt(webView, absLoc.getLatitude(), absLoc.getLongitude());	
				//We might insert a javascript call here that calls onTap in return in order to bring up the info-window
				subclassHook_WhatsNearbyDialog();
			}			
		});

    	return whatsNearbyDialogBuilder.create();
    }
	
    public Building getBuilding()
    {
    	return mCurrentBuilding;
    } 
       
    /**
	 * Returns the 'current floor'
	 * In the online phase, this means the floor of the estimated position.
	 * In the offline phase, this means the manually selected floor
	 * @return
	 */
	public abstract int getCurrentFloor();
    
    
    //The default behavior is to show all the graph's edges. 
	//However, in the online phase, we only want to show a route. 
	protected List<Edge> getVisibleEdges(int floorNum)
	{
		List<Edge> result = null;
		if (mGraph != null)
			result = mGraph.getEdges(floorNum);
		return result;
	}    
	
    //TODO: Change the API, so it looks more like getVisibleEdges()
	protected abstract List<Vertex> getVisibleVertices(List<Vertex> vertices);

    //used to tell javascript if we are in the online phase
	//default is false, override in online mode
	protected boolean isOnline() { return false; }

    protected void loadTiles() {
				
		//TODO:IFC change from using ifc to tile url
		//this is just for a quick test:
		//Note: The Json webclient CURRENTLY sets ifcUrl to "null" if it is null.
    	StringBuilder sbUrl = new StringBuilder();
        
        if (mCurrentBuilding != null && 
				mCurrentBuilding.getIfcUrl() != null &&
				mCurrentBuilding.getIfcUrl() != "null")
		{
        	sbUrl.append(mCurrentBuilding.getIfcUrl());
		}
		else
		{
			sbUrl.append(DEFAULT_TILE_URL);
		}
        //Avoid caching
        sbUrl.append("?t=").append(System.currentTimeMillis());
        
        String url = sbUrl.toString();
        webView.loadUrl(url);       
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);    	
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //This could be handled more gracefully than a type check
        //FOLIA specific - Show 'Folia Indoor Mapping' splashscreen
        /*
        if (this instanceof com.smartcampus.android.ui.maps.online.WebMap2DOnline)
        {
        	showSplashScreen();
        }
        */
        
        setContentView(R.layout.web_map);
        webView = (WebView)findViewById(R.id.web_map);
                
        setupWebView();
        loadTiles();
        
        IntentFilter mapReadyFilter;
        mapReadyFilter = new IntentFilter(IS_MAP_READY);
		mMapReadyReceiver =  createMapReadyReceiver(); //new MapReadyReceiver();
		registerReceiver(mMapReadyReceiver, mapReadyFilter);
        //refreshUI();
    }
	
    /**
     * All screens (but one) handle map load the same: 
     * A building has been loaded, now center at the building's coordinates. 
     * In the WebMap2DSelectBuildingLocation we don't yet have a building to center at
     * (that is what we are creating) so we handle it differently there.
     * @return
     */
    protected MapReadyReceiver createMapReadyReceiver()
    {
    	return new MapReadyReceiver();
    }
    
    /** SPLASH SECTION END **/
    
    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
    	
    	try
    	{
    		unregisterReceiver(mMapReadyReceiver);
    	}
    	catch (Exception ex) {}
    }
    @Override
    public void onRestart()
    {
    	super.onRestart();
    	    	
    	refreshUI();
    } 
    
	/**
	 * This method is called (from javascript) whenever the user taps a marker on the map. 
	 * @param floorNum The current floor 
	 * @param vertexId The id of the selected (tapped) vertex.
	 */
	public abstract void onTap(int floorNum, int vertexId); 	
	
	/*
	@Override
	public void onResume() {
		super.onResume();
				
		JavaScriptInterface jsi = JavaScriptInterface.getInstance();
    	jsi.setTarget(this);    	
	}
	*/		 
	
	/**
	 * This method refreshes the UI, i.e., updates tiles and title corresponding to a given floor
	 */
	protected void refreshUI()
    {	    	
    	if (mCurrentBuilding != null) 
    	{
    		//This MAY (theoretically) have a shallow building
    		if (mCurrentBuilding.getGraphModel() != null)
    		{
    			setTitle(concatBuildingAndFloorName(mCurrentSelectedFloor));
    	        this.updateOverlays(mCurrentSelectedFloor);
    		}
    	}
    }
	
	protected void removeSplashScreen() {
        if (mSplashDialog != null) {
            mSplashDialog.dismiss();
            mSplashDialog = null;
        }
    }
	
	public void setMapReady()
    {
		sendBroadcast(new Intent(IS_MAP_READY));		
    }
    /**
	 * This method is called (from javascript) whenever the user taps on the map - not a marker. 
	 * @param isOnline Indicates whether we are in the online phase
	 * @param floor the current floor
	 * @param lat the latitude of the tapped location
	 * @param lon the longitude of the tapped location
	 */
	public abstract void setSelectedLocation(boolean isOnline, int floor, double lat, double lon);
    
	//Replace a string level with an int level
	@SuppressLint("SetJavaScriptEnabled")
	private void setupWebView() {    	
    	
    	webView.getSettings().setJavaScriptEnabled(true);
    	//JSInterface
    	webView.addJavascriptInterface(new DeviceInterface(this), "DeviceInterface");
    	webView.clearCache(true);    	
    }	
	/**
	 * Indicates whether we desire to show the edges of the graph.
	 * @return The default value is true
	 */
	protected boolean showEdges()
	{
		return true; 
	}
	
	protected void showSplashScreen() {    
    	LinearLayout layout = createSplashLayout();
    	if (layout == null)
    		return;
    	
    	mSplashDialog = new Dialog(this, R.style.SplashScreen);        
        mSplashDialog.setContentView(layout);
        mSplashDialog.setCancelable(false);
        mSplashDialog.show();
         
        // Set Runnable to remove splash screen just in case
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
        	  removeSplashScreen();
          }
        }, 6000);
    }
	
	protected void subclassHook_WhatsNearbyDialog() {}
	
	protected void updateOverlays(int floorNum)
	{		
    	clearOverlays();
    	
    	//Show edges (if they should be shown):  
    	addEdgeOverlay(floorNum);
    	
    	//Show vertices:
    	addGraphOverlay(floorNum);    	
	     
	}
	
	private void updateViewType() {
		JSInterface.updateViewType(webView, mCurrentViewType);		
    }
}