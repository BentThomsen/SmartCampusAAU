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

package com.smartcampus.android.ui.maps.offline.graph.edge;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.smartcampus.R;
import com.smartcampus.android.ui.Globals;
import com.smartcampus.android.ui.maps.WebMap2D;
import com.smartcampus.indoormodel.AbsoluteLocation;
import com.smartcampus.indoormodel.SymbolicLocation;
import com.smartcampus.indoormodel.graph.Edge;
import com.smartcampus.indoormodel.graph.Vertex;
import com.smartcampus.javascript.JSInterface;
import com.smartcampus.webclient.IWebClient;
import com.smartcampus.webclient.OData4jWebClient;

public class WebMap2DAddEdge extends WebMap2D {
	
	//TODO: Update to new API
	@SuppressLint("NewApi")
	private class DeleteEdgeTask extends AsyncTask<Void, Void, Boolean>
    {
		private IWebClient mWebClient = new OData4jWebClient();
		private String errorMsg = "Ok";
				
		@Override
		protected Boolean doInBackground(Void... arg0) {
			boolean success = true;
			try
			{
				mWebClient.deleteEdge(mCurrentEdge.getId());
			}
			catch (Exception ex)
			{
				if (ex.getCause() != null)
					errorMsg = ex.getCause().getMessage();
				else
					errorMsg = ex.getMessage();
				success = false;
			}
			return success;  
		}
		
		protected void onPostExecute(Boolean success)
		{
			if (!success)
			{
				Globals.createErrorDialog(WebMap2DAddEdge.this, "Error", errorMsg).show();
			}
			else
			{
				mGraph.removeUndirectionalEdges(mCurrentEdge.getOrigin(), mCurrentEdge.getDestination());
				if (mCurrentEdge.isElevator())
				{
					mGraph.removeElevatorVertex(mCurrentEdge.getOrigin());
					mGraph.removeStaircaseVertex(mCurrentEdge.getDestination());
					//updateEndpoints(mCurrentEdge);					
				}
				if (mCurrentEdge.isStair())
				{
					mGraph.removeStaircaseVertex(mCurrentEdge.getOrigin());
					mGraph.removeStaircaseVertex(mCurrentEdge.getDestination());
				}
				
				//mEdgeOverlay.removeEdge(mCurrentEdge);
				Toast.makeText(WebMap2DAddEdge.this, "Link removed!", Toast.LENGTH_SHORT).show();
			}
			clearEndpoints();
			refreshUI();
			//resetUI();
		}		
    }	
	
	//callback for when an endpoint has been added/removed so title must change. 
	private class TitleChangedReceiver extends BroadcastReceiver {		
		@Override
		public void onReceive(Context context, Intent intent) {
			String title = intent.getStringExtra(WebMap2DAddEdge.INTENT_EXTRA_TITLE);
			if (title == null)
				title = " - ";
			WebMap2DAddEdge.this.setTitle(title);
		}
	}
	
	//TODO: Update to new API
	
	@SuppressLint("NewApi")
	private class UploadEdgeTask extends AsyncTask<Void, Void, Integer>
    {
		@SuppressLint("NewApi")
		private IWebClient mWebClient = new OData4jWebClient();
		private String downloadMsg = "Ok";
		
		@Override
		protected Integer doInBackground(Void... arg0) {
			int edge_id = -1;
			try
			{
				if (mIsUpdate)
				{
					mWebClient.updateEdge(mCurrentEdge);
					edge_id = mCurrentEdge.getId();
				}
				else
				{
					edge_id = mWebClient.addEdge(mCurrentEdge, mCurrentBuilding);
					mCurrentEdge.setId(edge_id);
				}
			}
			catch (Exception ex)
			{
				Throwable t = ex.getCause();
				if (t != null)
					downloadMsg = t.getMessage();
				else
					downloadMsg = ex.getMessage();
				return -1;
			}								
			
			return edge_id;  
		}
		
		protected void onPostExecute(Integer arg)
		{
			if (arg == -1)
			{
				Globals.createErrorDialog(WebMap2DAddEdge.this, "Error", downloadMsg);
			}
			else
			{
				Toast.makeText(WebMap2DAddEdge.this, "Link added!", Toast.LENGTH_SHORT).show();
				if (!mIsUpdate)
				{
					//Add the edge to the graph
					Iterable<Edge> unEdges = mGraph.addUndirectionalEdges(mCurrentEdge.getOrigin(), mCurrentEdge.getDestination());
					for (Edge e : unEdges)
					{
						e.setId(mCurrentEdge.getId());
					}
					if (mCurrentEdge.isElevator())
					{
						mGraph.addElevatorVertex(mCurrentEdge.getOrigin());
						mGraph.addElevatorVertex(mCurrentEdge.getDestination());
					}
					if (mCurrentEdge.isStair())
					{
						mGraph.addStaircaseVertex(mCurrentEdge.getOrigin());
						mGraph.addStaircaseVertex(mCurrentEdge.getDestination());
					}

					//Update the edge overlay
					//mEdgeOverlay.addEdge(mCurrentEdge);
				}				
			}
			clearEndpoints();
			refreshUI();
			//resetUI();
		}		
    }
	
	public static final String BROADCAST_BUILDING_EDGE_UPLOADED = "com.smartcampus.android.ui.offline.graph.edge.MapAddEdge.BROADCAST_BUILDING_EDGE_UPLOADED";
	
	//Indicates whether the title has changed (this occurs when a marker has been tapped
    //(in javascript code - from another thread - that's why we are using a callback)
    public static final String HAS_TITLE_CHANGED = "com.smartcampus.android.ui.offline.maps.Map2DOffline.HAS_TITLE_CHANGED";
    public static final String INTENT_EXTRA_TITLE = "INTENT_EXTRA_TITLE";
    	
	private List<Vertex> mEndPoints = new ArrayList<Vertex>();
	
	//private List<Drawable> mEndPointDrawables = new ArrayList<Drawable>();
	
	//Receiver for title change (changed from javascript - another thread)
    private TitleChangedReceiver mTitleChangedReceiver;

	private static Edge mCurrentEdge;

	//private EdgeOverlay mEdgeOverlay;
	
	private boolean mIsUpdate;

	@Override
	protected boolean addCurrentFloorToFloorChangerDialog() {
		return false;
	}	
	
	public boolean addEndpoint(Vertex v)
	{
		if (!hasTwoEndpoints())
		{
			mEndPoints.add(v);
			JSInterface.setEndpoint(webView, v.getId());
			return true;
		}
		return false;
	}
	
	private void addLink() {
		Vertex source = mEndPoints.get(0);
		Vertex destination = mEndPoints.get(1);
		//Establish whether this is an existing edge (update)
		//or a new one (create)
		mCurrentEdge = null;
		mIsUpdate = false;
		//Search for existing link in source's out-edges
		List<Edge> inEdges = source.getOutEdges();					
		for (Edge e : inEdges)
		{
			if (e.Opposite(source).getId() == destination.getId())
			{
				mCurrentEdge = e;
				mIsUpdate = true;
				break;
			}
		}
		if (!mIsUpdate)
		{
			//Search for existing link in source's in-edges
			List<Edge> outEdges = source.getOutEdges();					
			for (Edge e : outEdges)
			{
				if (e.Opposite(source).getId() == destination.getId())
				{
					mCurrentEdge = e;
					mIsUpdate = true;
					break;
				}
			}			
		}
		if (!mIsUpdate)
		{
			//No existing link found
			mCurrentEdge = new Edge(source, destination);
		}
		
		//We present the user with different options for annotating the edge
		AlertDialog.Builder builder = new AlertDialog.Builder(WebMap2DAddEdge.this);
		builder.setTitle("Specify link property");
		final String[] linkProperty = { "None", "Elevator", "Stairs" };

		builder.setItems(linkProperty, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        switch (item)
		        {
		        case 0: //NONE - don't do anything
		        	break; 
		        case 1: //Elevator
		        	mCurrentEdge.setElevator(true);
		        	break;
		        case 2: //Stairs
		        	mCurrentEdge.setStair(true);
		        	break;
		        }		        
		        //Save the edge with the specified property
		        Toast.makeText(WebMap2DAddEdge.this, "Uploading changes...", Toast.LENGTH_SHORT).show();
				new UploadEdgeTask().execute();
		    }		
		});
		AlertDialog alert = builder.create();
		alert.show();			
	}
	
	//determines whether the endpoints are connected
	private boolean areEndpointsConnected()
	{
		if (mEndPoints.size() != 2)
			return false;
		else
		{
			Vertex v1 = mEndPoints.get(0);
			Vertex v2 = mEndPoints.get(1);
			
			//we search manually to avoid potential equals() discrepancies
			//NOTE: equals() should now check on id
			//is v2 in v1's neighbors?
			List<Vertex> v1Neighbors = v1.adjacentVertices();
			for (Vertex v : v1Neighbors)
			{
				if (v.getId() == v2.getId())
					return true;
			}
			//is v1 in v2's neighbors?
			List<Vertex> v2Neighbors = v2.adjacentVertices();
			for (Vertex v : v2Neighbors)
			{
				if (v.getId() == v1.getId())
					return true;
			}
			//if we get here, v1 and v2 are not neighbors
			return false;
		}
	}
	
	private void clearEndpoints()
	{
		for (int i = 0; i < mEndPoints.size(); i++)
		{
			JSInterface.removeEndpoint(webView, mEndPoints.get(i).getId());			
		}
		mEndPoints.clear();
	}
	
	/**
	 * Prompts the user about deleting the link between the two vertices specified by the given ids.
	 * If yes, the link - if it exists - is deleted.
	 * @param v1Id Id of first endpoint
	 * @param v2Id Id of second endpoint
	 * @return An alertdialog used to confirm if the user wants to delete the specified link.
	 */
	public AlertDialog createRemoveLinkConfirmationDialog(int v1Id, int v2Id)
	{
		final int originId = v1Id, destinationId = v2Id;
		//Prompt the user
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Remove Link?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               removeLink(originId, destinationId); 		        	   
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               dialog.cancel();
		           }
		       });
		return builder.create();
	}
		
	//Write vertex's absLoc like so: (lat; lon; floor)
	private String getCoordinatesTitle(Vertex v)
	{
		AbsoluteLocation absLoc = v.getLocation().getAbsoluteLocation();
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(absLoc.getLatitude());
		sb.append("; ");
		sb.append(absLoc.getLongitude());
		sb.append("; ");
		sb.append((int)(absLoc.getAltitude()));
		sb.append(")");
		return sb.toString();		
	}
	
	@Override
	public int getCurrentFloor() {
		return mCurrentSelectedFloor;
	}
				
	public String getEndpointsTitle()
	{
		String title = "? - ?";
		if (mEndPoints.size() == 1)
		{
			Vertex v1 = mEndPoints.get(0);
			SymbolicLocation s1 = v1.getLocation().getSymbolicLocation();
			String end1 = (s1 != null && s1.getTitle() != null)
						   	? s1.getTitle()
						   	: getCoordinatesTitle(v1);
			title = end1 + " - ?";
		}
		else if (mEndPoints.size() == 2)
		{
			Vertex v1 = mEndPoints.get(0);
			SymbolicLocation s1 = v1.getLocation().getSymbolicLocation();
			String end1 = (s1 != null && s1.getTitle() != null)
							? s1.getTitle() 
							: getCoordinatesTitle(v1);
			
			Vertex v2 = mEndPoints.get(1);
			SymbolicLocation s2 = v2.getLocation().getSymbolicLocation();
			String end2 = (s2 != null && s2.getTitle() != null)
							? s2.getTitle()
							: getCoordinatesTitle(v2);
			title = end1 + " - " + end2;
		}
		return title;		
	}
		
	@Override
	protected List<Vertex> getVisibleVertices(List<Vertex> vertices) {
		//show everything, so just pass through
		return vertices;
	}
	
	public boolean hasTwoEndpoints()
	{
		return mEndPoints.size() == 2;
	}	
	
	public boolean isEndpoint(Vertex v)
	{
		for (Vertex cur : mEndPoints)
		{			
			if (cur.getId() == v.getId())
				return true;
		}
		return false;
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);    	
    }
	
	@Override
	public void onCreate(android.os.Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        
        IntentFilter titleChangedFilter;
		titleChangedFilter = new IntentFilter(WebMap2DAddEdge.HAS_TITLE_CHANGED);
		mTitleChangedReceiver = new TitleChangedReceiver();
		registerReceiver(mTitleChangedReceiver, titleChangedFilter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map2d_edit_edge_menu, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch (item.getItemId())
    	{
    	case R.id.edit_edge_add_link:
    		addLink();
    		return true;
    	case R.id.edit_edge_remove_link:
    		removeLink();
    		break;
    	case R.id.edit_edge_floor_changer:
    		AlertDialog alert = createFloorChangerDialog();
        	alert.show();
            return true;
    	case R.id.edit_edge_what_is_here:
    		AlertDialog nearbyPlacesDialog = createWhatsNearbyDialog();
        	nearbyPlacesDialog.show();
            return true;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{		
		MenuItem addLink = menu.findItem(R.id.edit_edge_add_link);
		MenuItem removeLink = menu.findItem(R.id.edit_edge_remove_link);
		
		addLink.setEnabled(hasTwoEndpoints());
	    removeLink.setEnabled(hasTwoEndpoints() && areEndpointsConnected());
	    
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public void onTap(int floorNum, int vertexId) {
		if (mGraph == null)
			return;
		final Vertex v = mGraph.getVertexById(vertexId);
		if (v == null)
			return;
		
		//We have two options:
		//We can add an endpoint or we can remove an existing endpoint		
		final String[] items = new String[1];
		final boolean isEndpoint = isEndpoint(v);
		if (isEndpoint)
		{
			items[0] = "Remove endpoint";
		}
		else
		{
			items[0] = "Add endpoint";
		}
				
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose an action");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        if (isEndpoint)
		        {
		        	//newItem.setMarker(mae.getOriginalMarker(newItem.getVertex()));
		        	removeEndpoint(v);		        	
		        }
		        else
		        {
		        	//A link has to be between two endpoints
		        	if (hasTwoEndpoints())
		        	{
		        		Globals.createErrorDialog(WebMap2DAddEdge.this, "Too many endpoints", "Pleasure remove another endpoint first").show();
		        	}
		        	else
	        		{
		        		addEndpoint(v);
		        		//Add destination marker		        		
	        		}
		        }	
		        Intent titleIntent = new Intent(HAS_TITLE_CHANGED);
		    	titleIntent.putExtra(WebMap2DAddEdge.INTENT_EXTRA_TITLE, getEndpointsTitle());
		    	sendBroadcast(titleIntent);
		        //mae.setTitle(mae.getEndpointsTitle());		        
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();		
		
	}
	
	public boolean removeEndpoint(Vertex v)
	{
		int removeIdx = -1;
		//find vertex
		for (int i = 0; i < mEndPoints.size(); i++)
		{
			if (v.getId() == mEndPoints.get(i).getId())
			{
				removeIdx = i;
				break;
			}
		}
		//remove vertex
		if (removeIdx != -1)
		{
			mEndPoints.remove(removeIdx);
			JSInterface.removeEndpoint(webView, v.getId());
			return true;
		}
		return false;
	}
	
	private void removeLink()
	{
		Vertex source = mEndPoints.get(0);
		Vertex destination = mEndPoints.get(1);
		
		removeLink(source, destination);		
	}
		
	/**
	 * This method makes it possible to delete a link by responding to a javascript click event
	 * on an edge. The user is prompted if he wants to remove the link.
	 * @param originId
	 * @param destinationId
	 */
	public void removeLink(int originId, int destinationId)
	{
		//Toast.makeText(this, "Delete link between " + originId + " and " + destinationId, Toast.LENGTH_SHORT).show();
		
		if (mGraph == null)
			return;
		
		removeLink(
			mGraph.getVertexById(originId),
			mGraph.getVertexById(destinationId));				
	}
	
	private void removeLink(Vertex source, Vertex destination)
	{
		if (source == null || destination == null)
			return;
		
		List<Edge> total = source.getOutEdges();					
		for (Edge e : total)
		{
			if (e.Opposite(source).getId() == destination.getId())
			{
				mCurrentEdge = e;
				Toast.makeText(WebMap2DAddEdge.this, "Uploading changes...", Toast.LENGTH_SHORT).show();				
				new DeleteEdgeTask().execute();
				break;
			}
		}
	}
	
	@Override
	protected void refreshUI()
	{
		//clearEndpoints();
		super.refreshUI();
		setTitle(getEndpointsTitle());
				
		for (int i = 0; i < mEndPoints.size(); i++)
		{
			//redraw on floor-change (which prompts the refreshUI()
			JSInterface.setEndpoint(webView, mEndPoints.get(i).getId());			
		}		
	}
	
	/*
	private void resetUI() {
		clearEndpoints();
        setTitle(getEndpointsTitle());
        //mGraphOverlay = new HashMap<Integer, Map2DGraphOverlay>();
		updateOverlays(mCurrentSelectedFloor);        
	}
	*/
	
	@Override
	public void setSelectedLocation(boolean isOnline, int floor, double lat,
			double lon) {		
		return;
	}

	/*
	@Override
	protected void updateOverlays(int floorNum) {
		this.clearOverlays();
    	//Show edges
		//TODO:JAVASCRIPT

    	//Show vertices
       	addGraphOverlay(floorNum);          	
	}	
	*/
	
}
