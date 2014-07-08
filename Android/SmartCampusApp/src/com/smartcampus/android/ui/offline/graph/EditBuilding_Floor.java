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

import com.smartcampus.R;
import com.smartcampus.android.location.LocationService;
import com.smartcampus.android.ui.Globals;
import com.smartcampus.indoormodel.Building;
import com.smartcampus.indoormodel.Building_Floor;
import com.smartcampus.webclient.IWebClient;
import com.smartcampus.webclient.OData4jWebClient;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class EditBuilding_Floor extends Activity {
	
	@SuppressLint("NewApi")
	private class UploadBuilding_FloorTask extends AsyncTask<Building_Floor, Void, Integer>
    {
		private IWebClient mWebClient = new OData4jWebClient();
		private String downloadMsg = "Ok";
		private final int errorVal = Integer.MAX_VALUE;
		
		@Override
		protected Integer doInBackground(Building_Floor... arg0) {
			//do geocoding
			Building_Floor uploadBuildingFloor = arg0[0];
			int floor_id = errorVal;
			if (mIsNewFloor)
			{				
				try
				{
					floor_id = mWebClient.addBuilding_Floor(uploadBuildingFloor, mCurrentBuilding);
				}
				catch (Exception ex)
				{
					if (ex.getCause() != null)
						downloadMsg = ex.getCause().getMessage();
					else
						downloadMsg = ex.getMessage();
					return errorVal;
				}
				uploadBuildingFloor.setID(floor_id);	
				if (mCurrentBuilding != null)
					mCurrentBuilding.addFloor(uploadBuildingFloor);	
				//Floor has already been added to building - cf. building.AddFloor(...)	
			}
			//2) or update existing
			else
			{
				try
				{
					mWebClient.updateBuilding_Floor(uploadBuildingFloor);
					//signal success by assigning non-errorVal
					floor_id = uploadBuildingFloor.getFloorNumber();
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
			return floor_id;  
		}
		
		//Receives the id of the newly added/updated floor. 
		//arg is errorVal if an exception was encountered (probably I/O exception)
		protected void onPostExecute(Integer arg)
		{
			if (arg == errorVal)
			{
				Globals.createErrorDialog(EditBuilding_Floor.this, "Error", downloadMsg).show();
			}
			else
			{
				Toast.makeText(EditBuilding_Floor.this, "Changes saved", Toast.LENGTH_SHORT).show();
				if (mIsNewFloor)
				{
					Intent intent = new Intent(BROADCAST_BUILDING_FLOOR_UPLOADED);
					sendBroadcast(intent);
				}
				finish();
			}
		}		
    }
	public static Building_Floor SELECTED_BUILDING_FLOOR;
	
	public static final String BROADCAST_BUILDING_FLOOR_UPLOADED = "com.smartcampus.android.ui.offline.graph.EditBuilding_Floor.BROADCAST_BUILDING_FLOOR_UPLOADED";
	private EditText mFloorNumber;
	private EditText mFloorName;
	private Button   mSaveButton;
	private Building mCurrentBuilding;
		
	private boolean mIsNewFloor;

	private void initializeInputBoxes()
	{
		if (!mIsNewFloor)
		{
			String floorNum = Integer.toString(SELECTED_BUILDING_FLOOR.getFloorNumber());
			String floorName = SELECTED_BUILDING_FLOOR.getFloorName();
			mFloorNumber.setText(floorNum);
			mFloorName.setText(floorName);	
			
			//do not allow modification of floor num
			//to an existing floor.
			mFloorNumber.setEnabled(false); 
		}		
	}
	
	private void initializeSaveButton() {
		mSaveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String floorName = mFloorName.getText().toString();
				int floorNum;
				try
				{
					floorNum = Integer.parseInt(mFloorNumber.getText().toString());
				}
				catch (NumberFormatException ex)
				{
					AlertDialog.Builder ad = new Builder(EditBuilding_Floor.this);
					ad.setTitle("Invalid floor number");
					ad.setMessage("Please enter a valid floor number (as an integer)");
					ad.show();
					return;
				}
				
				//Apply changes locally
				mIsNewFloor = SELECTED_BUILDING_FLOOR == null;
				Building_Floor uploadFloor;
				if (mIsNewFloor)
				{
					if (mCurrentBuilding.hasFloorAt(floorNum))
					{
						Globals.createErrorDialog(
								EditBuilding_Floor.this,
								"Invalid floor number",
								"A floor with the specified floor number already exists"
						).show();
						return;
					}
					uploadFloor = new Building_Floor(floorNum, floorName);											
				}
				else
				{
					//the editText for floorNum is disabled
					//which prevents changing the floor num
					uploadFloor = SELECTED_BUILDING_FLOOR;					
				}
				uploadFloor.setFloorNumber(floorNum);
				uploadFloor.setFloorName(floorName);
				Toast.makeText(EditBuilding_Floor.this, "Uploading changes...", Toast.LENGTH_SHORT).show();
				new UploadBuilding_FloorTask().execute(uploadFloor);			
			}
		});		
	}	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.offline_edit_floor);
		
		mFloorNumber = (EditText)this.findViewById(R.id.floor_number_editText);
		mFloorName   = (EditText)this.findViewById(R.id.floor_name_editText);
		mSaveButton  = (Button)this.findViewById(R.id.save_new_floor_button);
		
		mCurrentBuilding = LocationService.CurrentBuilding;
		mIsNewFloor = SELECTED_BUILDING_FLOOR == null;
		initializeInputBoxes();
		initializeSaveButton();
	}
}
