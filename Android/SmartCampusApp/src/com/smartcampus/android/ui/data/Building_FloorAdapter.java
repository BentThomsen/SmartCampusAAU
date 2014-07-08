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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartcampus.indoormodel.Building_Floor;

public class Building_FloorAdapter extends BaseAdapter {

    public final class Building_FloorListView extends LinearLayout 
	{
		private TextView mFloorNumberTextView;
		private TextView mFloorNameTextView;
		
		public Building_FloorListView(Context context, Building_Floor b) {
			super(context);
			
			this.setOrientation(LinearLayout.VERTICAL);
			LinearLayout.LayoutParams params = 
				new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
			
			mFloorNumberTextView = new TextView(context);
			mFloorNumberTextView.setText("Floor " + b.getFloorNumber());
			mFloorNumberTextView.setTextColor(Color.WHITE);
			
			mFloorNameTextView = new TextView(context);
			mFloorNameTextView.setText(b.getFloorName());
			mFloorNameTextView.setTextColor(Color.GRAY);
			
			this.addView(mFloorNumberTextView, params);
			this.addView(mFloorNameTextView, params);		
		}
	}
    //private static final String CLASSTAG = ReviewAdapter.class.getSimpleName();
    private final Context context;
    
    private List<Building_Floor> mFloors;
    
    public Building_FloorAdapter(Context context, Iterable<Building_Floor> floors) {
        this.context = context;   
        mFloors = new ArrayList<Building_Floor>();
        for (Building_Floor floor : floors)
        	mFloors.add(floor);     
        
    }

    public int getCount() {
        return mFloors.size();
    }

    public Object getItem(int position) {
        return this.mFloors.get(position);
    }

    public long getItemId(int position) {
        return position; //nothing special
    } 
    
    public View getView(int position, View convertView, ViewGroup parent) {
    	//Building_Floor b = this.mFloors.get(position);
    	//return new Building_FloorListView(this.context, b);   
    	
	    TextView result = new TextView(this.context);
	    result.setTextSize(20);
    	Building_Floor b = mFloors.get(position);
		String text = "Floor " + b.getFloorNumber();
		if (b.getFloorName() != null)
			text += " (" + b.getFloorName() + ")";

    	result.setText(text);
        
        return result;   
                
    }
}
