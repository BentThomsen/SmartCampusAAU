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

package com.smartcampus.android.ui;

import com.smartcampus.R;
import com.smartcampus.android.location.LocationService;

import android.app.Activity;
import android.content.SharedPreferences;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class SetTrackingActivity extends Activity {
	private static final String PREFS_NAME = "SmartCampusAAUPrefs";

	CheckBox allowTrackingCheckbox;
	TextView guidTextView;
	
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//TODO: Set proper content view
		setContentView(R.layout.set_tracking);	
		allowTrackingCheckbox = (CheckBox)findViewById(R.id.allowTrackingCheckbox);  
		guidTextView = (TextView)findViewById(R.id.guidTextView);
		final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    
		//Determine whether tracking is allowed
		final String key_isTrackingAllowedKey = "key_isTrackingAllowed";
		boolean isTrackingAllowed = settings.getBoolean(key_isTrackingAllowedKey, false);
	    LocationService.setTrackingAllowed(isTrackingAllowed);
		allowTrackingCheckbox.setChecked(isTrackingAllowed);
	    allowTrackingCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor editor = settings.edit();
		    	editor.putBoolean(key_isTrackingAllowedKey, isChecked);
		    	editor.commit();				
		    	LocationService.setTrackingAllowed(isChecked);
			}
		});
	    
	    //Display the anonymous id
	    final String key_clientId = "key_clientId";
	    String savedClientId = settings.getString(key_clientId, null);
	    if (savedClientId == null)
	    {
	    	SharedPreferences.Editor editor = settings.edit();
	    	//getClient() will generate a UUID if LocationsService's clientId is null or empty
	    	savedClientId = LocationService.getClientId();
	    	editor.putString(key_clientId, savedClientId);
	        editor.commit();
	    }
	    LocationService.setClientId(savedClientId);	
	    guidTextView.setText(savedClientId);	    
	};
}
