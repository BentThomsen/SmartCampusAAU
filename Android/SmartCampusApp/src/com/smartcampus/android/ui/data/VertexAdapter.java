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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import com.smartcampus.indoormodel.AggregateLocation;
import com.smartcampus.indoormodel.graph.Vertex;


public class VertexAdapter extends BaseAdapter {

    //private static final String CLASSTAG = ReviewAdapter.class.getSimpleName();
    private final Context context;
    private List<Vertex> vertices;
    
    public VertexAdapter(Context context, List<Vertex> vertices) {
        this.context = context;   
        this.vertices = vertices;      
        
    }
    
    public int getCount() {
        return vertices.size();
    }

    public Object getItem(int position) {
        return this.vertices.get(position);
    }

    public long getItemId(int position) {
        return position; //nothing special
    }

    public View getView(int position, View convertView, ViewGroup parent) {
    	TextView result = new TextView(this.context);
    	result.setTextSize(20);
    	
        AggregateLocation aggLoc = this.vertices.get(position).getLocation();
        String title;
        if (aggLoc.getSymbolicLocation() != null)
        	title = aggLoc.getSymbolicLocation().getTitle();
        else
        	title = aggLoc.getAbsoluteLocation().getLatitude() + ", " + aggLoc.getAbsoluteLocation().getLongitude();
        result.setText(title);
        
        return result;    	        
    }   
}
