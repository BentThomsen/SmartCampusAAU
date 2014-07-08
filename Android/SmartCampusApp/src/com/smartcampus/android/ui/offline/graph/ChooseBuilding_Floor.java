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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.smartcampus.R;
import com.smartcampus.android.location.LocationService;
import com.smartcampus.android.ui.data.Building_FloorAdapter;
import com.smartcampus.indoormodel.Building;
import com.smartcampus.indoormodel.Building_Floor;

public class ChooseBuilding_Floor extends ListActivity {
	
	private class Building_FloorUploadedReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			updateFloorsListView();
		}
	}	
	private Building_FloorAdapter mBuilding_FloorAdapter;
	private ListView mListView;
	
	private Building mCurrentBuilding;
	
	private Building_FloorUploadedReceiver mReceiver;
	
	public void initializeBuilding_FloorList()
	{
		mListView = getListView();
		mListView.setItemsCanFocus(false);
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		//mListView.setAdapter(mBuilding_FloorAdapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view,
		        int position, long id)
		    {
		    	Building_Floor b = (Building_Floor)mListView.getItemAtPosition(position);
		    	EditBuilding_Floor.SELECTED_BUILDING_FLOOR = b;
		    	startActivity(new Intent(ChooseBuilding_Floor.this, EditBuilding_Floor.class));
		    }
		});	
		updateFloorsListView();
	};	
			
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_choose_building_floor, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch (item.getItemId())
    	{
    	case R.id.mi_create_new_building_floor:
    		startActivity(new Intent(ChooseBuilding_Floor.this, EditBuilding_Floor.class));
    		return true;           
    	}
    	return super.onMenuItemSelected(featureId, item);
    }    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.offline_choose_from_list);
		
		mCurrentBuilding = LocationService.CurrentBuilding;
		EditBuilding_Floor.SELECTED_BUILDING_FLOOR = null;
		
		initializeBuilding_FloorList();
		
		IntentFilter filter;
		filter = new IntentFilter(EditBuilding_Floor.BROADCAST_BUILDING_FLOOR_UPLOADED);
		mReceiver = new Building_FloorUploadedReceiver();
		registerReceiver(mReceiver, filter);
	}
	
	@Override
	protected void onDestroy()
	{
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
	
	private void updateFloorsListView()
	{
		Iterable<Building_Floor> floors;
		if (mCurrentBuilding != null)
		{
			floors = mCurrentBuilding.getFloors();
		}
		else
		{
			floors = new ArrayList<Building_Floor>(); //empty floors
		}
		mBuilding_FloorAdapter = new Building_FloorAdapter(this, floors);
		mListView.setAdapter(mBuilding_FloorAdapter);
	}
}

