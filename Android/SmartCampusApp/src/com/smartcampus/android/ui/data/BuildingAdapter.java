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

package com.smartcampus.android.ui.data;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartcampus.indoormodel.Building;

public class BuildingAdapter extends BaseAdapter {
	
	public final class BuildingListView extends LinearLayout 
	{
		private TextView mNameTextView;
		private TextView mAddressTextView;
		
		public BuildingListView(Context context, Building b) {
			super(context);
			
			this.setOrientation(LinearLayout.VERTICAL);
			LinearLayout.LayoutParams params = 
				new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
			
			mNameTextView = new TextView(context);
			mNameTextView.setText(b.getName());
			mNameTextView.setTextColor(Color.WHITE);
			
			mAddressTextView = new TextView(context);
			mAddressTextView.setText(b.getMaxAddress() + "; " + b.getPostalCode() + "; " + b.getCountry());
			mAddressTextView.setTextColor(Color.GRAY);
			
			this.addView(mNameTextView, params);
			this.addView(mAddressTextView, params);		
		}
	}
	//private static final String CLASSTAG = ReviewAdapter.class.getSimpleName();
	private final Context context;
	
	
	private List<Building> mBuildings;
	
	
	public BuildingAdapter(Context context, List<Building> buildings) {
	    this.context = context;   
	    this.mBuildings = buildings; 	    
	}
	
	@Override
	public int getCount() {
	    return mBuildings.size();
	}
	
	@Override
	public Building getItem(int position) {
	    return this.mBuildings.get(position);
	}
	
	@Override
	public long getItemId(int position) {
	    return position; //nothing special
	}   
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Building b = this.mBuildings.get(position);
	    return new BuildingListView(this.context, b);          
	}
}



