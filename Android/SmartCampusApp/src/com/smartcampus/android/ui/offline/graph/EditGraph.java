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
import com.smartcampus.android.ui.maps.offline.graph.edge.WebMap2DAddEdge;
import com.smartcampus.android.ui.maps.offline.graph.vertex.WebMap2DRemoveVertex;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class EditGraph extends Activity {
	
	private Button mEditLinksButton;
	//private Button mRemoveLink;
	private Button mRemoveNode;
	private Button mEditFloorsButton;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.offline_edit_graph);
                
        mEditLinksButton    = (Button) this.findViewById(R.id.edit_graph_edit_links_button);
        //mRemoveLink = (Button) this.findViewById(R.id.remove_link_button);
        mRemoveNode = (Button) this.findViewById(R.id.edit_graph_remove_node_button);
        mEditFloorsButton   = (Button) this.findViewById(R.id.edit_graph_edit_floors_button);
        
        setTitle("Graph Editing");
        
        mEditLinksButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(EditGraph.this, WebMap2DAddEdge.class));
				//startActivity(new Intent(EditGraph.this, AddEdge.class));				
			}        	
        });
        /*
        mRemoveLink.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(EditGraph.this, RemoveEdge.class));				
			}        	
        });
        */
        
        mRemoveNode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(EditGraph.this, WebMap2DRemoveVertex.class));				
			}        	
        });
        mEditFloorsButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		startActivity(new Intent(EditGraph.this, ChooseBuilding_Floor.class));
        	}
        });
        
	}
	
     
}
