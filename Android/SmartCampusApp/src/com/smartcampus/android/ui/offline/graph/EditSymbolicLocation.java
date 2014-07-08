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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.smartcampus.R;
import com.smartcampus.android.ui.Globals;
import com.smartcampus.android.ui.maps.offline.WebMap2DOffline;
import com.smartcampus.indoormodel.SymbolicLocation;
import com.smartcampus.indoormodel.SymbolicLocation.InfoType;
import com.smartcampus.indoormodel.graph.Vertex;
import com.smartcampus.webclient.IWebClient;
import com.smartcampus.webclient.OData4jWebClient;

/**
 * @author rhansen
 * Providies editing for a symbolic location 
 */
public class EditSymbolicLocation extends Activity {
	
	//Upload a symbolicLocation in the background (update or create)
	@SuppressLint("NewApi")
	private class UploadSymbolicLocationTask extends AsyncTask<SymbolicLocation, Void, Integer>
    {
		//NOTE: No id is specified
		//If we have a new symbolicLocation (global symbolic location is null), we get the id from the server
		//Otherwise, we set the id from the global vertex's symbolic location
		@Override
		protected Integer doInBackground(SymbolicLocation... arg0) {
			mWebClient = new OData4jWebClient();
			
			SymbolicLocation uploadSymLoc = arg0[0];
			SymbolicLocation currentSymLoc = WebMap2DOffline.SelectedOfflineVertex.getLocation().getSymbolicLocation();
			int id = -1;
			//1) upload new symbolic location 
			if (currentSymLoc == null) {
				try
				{
					id = mWebClient.addSymbolicLocation(uploadSymLoc, WebMap2DOffline.SelectedOfflineVertex);
					uploadSymLoc.setId(id);
					isNewSymbolicLocation = true;
				}
				catch (Exception ex)
				{
					if (ex.getCause() != null)
						downloadMsg = ex.getCause().getMessage();
					else
						downloadMsg = ex.getMessage();
					return errorVal;
				}
			}
			//2) or update existing
			else
			{		
				try
				{
					uploadSymLoc.setId(currentSymLoc.getId()); //We get the id from global vertex' symbolicLocation
					mWebClient.updateSymbolicLocation(uploadSymLoc);	
				}
				catch (Exception ex)
				{
					if (ex.getCause() != null)
						downloadMsg = ex.getCause().getMessage();
					else
						downloadMsg = ex.getMessage();
					return errorVal;
				}
			}
			//In any event, update the global selected vertex' symbolic location
			WebMap2DOffline.SelectedOfflineVertex.getLocation().setSymbolicLocation(uploadSymLoc);
			
			if (isNewSymbolicLocation) //NEW symbolic location
				id = WebMap2DOffline.SelectedOfflineVertex.getId();
			return id; //-1 if update 
		}
		
		//The id denotes which vertex was altered
		//If it was only an update, the id will be -1
		//else the id is the id of the vertex affected
		//We only broadcast in case of a NEW symbolic location (need to change icon to bound)
		protected void onPostExecute(Integer vertexId)
		{
			if (vertexId == errorVal)
			{
				Globals.createErrorDialog(EditSymbolicLocation.this, "Error", downloadMsg).show();
			}
			else
			{
				//Also, save updates (commented out 'new' requirement)
				//if (isNewSymbolicLocation)
				//{
					Intent intent = new Intent(BROADCAST_NEW_SYMBOLIC_LOCATION_UPLOADED);
					intent.putExtra(INTENT_EXTRA_IS_NEW_SYMBOLIC_LOCATION, true);
					intent.putExtra(INTENT_EXTRA_VERTEX_ID, vertexId);
					sendBroadcast(intent);					
				//}
				Toast.makeText(EditSymbolicLocation.this, "Changes saved", Toast.LENGTH_SHORT).show();
				finish();			
			}
		}		
    }
	//The client that is used to download a graph from the server with
	//cf. com.smartcampus.webclient
	private IWebClient mWebClient;
	private final int errorVal = Integer.MAX_VALUE;
	private String downloadMsg = "Ok";
	private boolean isNewSymbolicLocation;
	
	private SymbolicLocation.InfoType mSelectedInfoType = InfoType.NONE;
	public static String BROADCAST_NEW_SYMBOLIC_LOCATION_UPLOADED = "com.smartcampus.android.ui.offline.graph.NEW_SYMBOLIC_LOCATION_UPLOADED";
	public static String INTENT_EXTRA_IS_NEW_SYMBOLIC_LOCATION = "IS_NEW_SYMBOLIC_LOCATION";
	public static final String INTENT_EXTRA_VERTEX_ID = "VERTEX_ID";
	
	public static final String INTENT_EXTRA_OVERLAY_ITEM_IDX = "OVERLAY_ITEM_IDX";
	private EditText mTitle;
	private EditText mDescription;
	private EditText mUrl;
	private CheckBox mIsEntrance;
	private Spinner mInfoType;
	private Button mSave;
	
	private Vertex selectedVertex;
	
	private void initializeInputBoxes()
	{
		if (selectedVertex != null)
		{
			SymbolicLocation symLoc = selectedVertex.getLocation().getSymbolicLocation();
			if (symLoc != null)
			{
				mTitle.setText(symLoc.getTitle());
				mDescription.setText(symLoc.getDescription());
				mUrl.setText(symLoc.getUrl());		
				mIsEntrance.setChecked(symLoc.isEntrance());
			}			
		}		
	}
	
	private void initializeSaveButton()
	{
		mSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
								
				String title = mTitle.getText().toString();
				String description = mDescription.getText().toString();
				String url = mUrl.getText().toString();
														
				//upload symbolicLocation in the background
				//(create or update - which is determined in UploadSymbolicLocationTask)
				SymbolicLocation tmpSymLoc = new SymbolicLocation(title, description, url);
				tmpSymLoc.setEntrance(mIsEntrance.isChecked());
				tmpSymLoc.setType(mSelectedInfoType);
				Toast.makeText(EditSymbolicLocation.this, "Uploading changes...", Toast.LENGTH_SHORT).show();
				new UploadSymbolicLocationTask().execute(tmpSymLoc);										
			}					
		});
	}
	
	private void initializeSpinner() {
		//Populate the spinner with 'pretty print' of available options
		SymbolicLocation.InfoType[] options = SymbolicLocation.InfoType.values();
		String[] strOptions = new String[options.length];
		for (int i = 0; i < options.length; i++)
		{
			strOptions[i] = InfoType.prettyPrint(options[i]);
		}
		
		//Set up the adapter and item listener
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, strOptions);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mInfoType.setAdapter(adapter);
	    mInfoType.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				mSelectedInfoType = InfoType.getValue(pos);
				//Toast.makeText(EditSymbolicLocation.this, InfoType.prettyPrint(InfoType.getValue(pos)), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}		
	    });

	    //Preselect if the current symLoc != null
	    if (selectedVertex != null)
	    {
	    	SymbolicLocation symLoc = selectedVertex.getLocation().getSymbolicLocation();
	    	if (symLoc != null)
	    	{
	    		mInfoType.setSelection(symLoc.getType().ordinal());
	    	}
		}
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.offline_edit_symbolic_location);
		
		mTitle 		 = (EditText) this.findViewById(R.id.symbolicLocation_title);
		mDescription = (EditText) this.findViewById(R.id.symbolicLocation_description);
		mUrl 		 = (EditText) this.findViewById(R.id.symbolicLocation_url);
		mSave 		 = (Button)   this.findViewById(R.id.symbolicLocation_save);
		mIsEntrance  = (CheckBox) this.findViewById(R.id.symbolicLocation_entrance_checkBox);
		mInfoType 	 = (Spinner)  this.findViewById(R.id.symbolicLocation_info_type_spinner);	
		
		selectedVertex = WebMap2DOffline.SelectedOfflineVertex; //rh alternative to above
		
		//INTENT_EXTRA_OVERLAY_ITEM_IDX
		initializeInputBoxes();
		initializeSpinner();
		initializeSaveButton();
	}
}
