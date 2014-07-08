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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class Globals {
	
	//public static boolean IsFirstFix = true;
	
	public static final String URL_EXTRA = "url";
	
	//public static final String SMARTCAMPUS_SERVICE_ROOT_URI = "http://smartcampus.cloudapp.net/RadioMapService.svc/";
	
	public static AlertDialog createConnectionErrorDialog(Context context)
	{
		return createErrorDialog(context, "Connection Error", "Please check that you have connectivity and try again.");
	}
	
	public static AlertDialog createErrorDialog(Context context, String errorTitle, String errorMsg)
	{
		AlertDialog.Builder errorBuilder = new AlertDialog.Builder(context);
		errorBuilder.setTitle(errorTitle);
		errorBuilder.setMessage(errorMsg)
		   .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		       public void onClick(DialogInterface dialog, int id) {
		           dialog.dismiss();
		       }
		   }
		);
		return errorBuilder.create();
	}
	
	public static AlertDialog createWifiDialog(final Context context)
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Turn on Wifi?");
			builder.setMessage("Wi-Fi is required. Do you want to turn Wi-Fi on?");
			builder.setPositiveButton("Yes",
	            new DialogInterface.OnClickListener() {
	                @Override
	                public void onClick(final DialogInterface dialogInterface, final int i) {	                	
	                	context.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));	                	
	                }
	            });
			builder.setNegativeButton("No", null);
			
			return builder.create();
	}
	
	public static AlertDialog createGpsDialog(final Context context)
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Turn on Gps?");
			builder.setMessage("Gps is required. Do you want to turn Gps on?");
			//builder.setCancelable(false); //We allow cancellation by 'back'
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(
                        final DialogInterface dialogInterface,
                        final int i) {
                	try {
                		context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                	}
                	catch (Exception ex)
                	{
                		Globals.createErrorDialog(context, "Error: No Gps Provider found", "No Gps Provider was found.");
                	}
                }
            });
			builder.setNegativeButton("No", null);
			/*
			builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, final int id) {
                    dialog.cancel();
                }
            });
            */
			return builder.create();
	}
	
	//Used squarely for remote debugging. 
    public static AlertDialog createExceptionDialog(Context context, Exception ex)
    {
    	String msg = ex.getMessage(); 
		if (msg == null)
			msg = "<missing msg.>";
		
		String cause = null;
		Throwable c = ex.getCause();
		if (c != null)
			cause = ex.getCause().getMessage();
		if (cause == null)
			cause = "<missing cause>";
		
		String localizedMsg = ex.getLocalizedMessage();
		if (localizedMsg == null)
			localizedMsg = "<missing localized msg.>";
		
		StringBuffer stackTrace = new StringBuffer();    				
		StackTraceElement[] stacks = ex.getStackTrace();
		if (stacks != null)
		{
			for (StackTraceElement st : stacks)
				stackTrace.append(st.toString()).append("; \n");
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("MSG:\n").append(msg).append("\n");
		sb.append("CAUSE:\n").append(cause).append("\n");
		sb.append("LOCAL-MSG:\n").append(localizedMsg).append("\n");
		sb.append("STACK:\n").append(stackTrace.toString()).append("\n");    				
		final String finalError = sb.toString();
		
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
	    alertDialog.setTitle("Error");
	    alertDialog.setMessage(finalError);
	    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	return;
	        }
	    });
	    return alertDialog;
    }
}
