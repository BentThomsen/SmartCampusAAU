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
import com.smartcampus.android.ui.maps.online.WebMap2DOnline;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

public class SplashScreenActivity extends Activity {

	private static boolean IS_SHOWING;
	public static synchronized boolean isShowing()
	{
		return IS_SHOWING;
	}
	private static synchronized void setIsShowing(boolean isShowing)
	{
		IS_SHOWING = isShowing;
	}
	
	protected boolean dialogOpen = false;
	
	/*
	 * the following sets for how long the splash screen will be displayed in ms
	 */
	private static final int SPLASHTIME = 5000;
	
	//Initializes from xml - caused an inflateException with Folia's devices
	/*
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
				
		Thread splashTread = new Thread() {
	        @Override
	        public void run() {
	            try {
	                int waited = 0;
	                while((waited < SPLASHTIME)) {
	                    sleep(100);
	                    if(!dialogOpen) {
	                        waited += 100;
	                    }
	                }
	            } catch(InterruptedException e) {
	            } finally {
	                finish();
	                startActivity(new Intent(SplashScreenActivity.this, WebMap2DOnline.class));            
	                //startActivity(new Intent("com.smartcampus.android.ui.maps.online.WebMap2DOnline"));
	                stop();
	            }
	        }
	    };	    
	    splashTread.start();		
	};
	*/
			
	private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	        if (width > height) {
	            inSampleSize = Math.round((float)height / (float)reqHeight);
	        } else {
	            inSampleSize = Math.round((float)width / (float)reqWidth);
	        }
	    }
	    return inSampleSize;
	}
	
	private static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
	
	//Creating the UI programmatically:
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boolean showSplash = false;
		try
		{
			LinearLayout lLayout = new LinearLayout(this);
	        lLayout.setOrientation(LinearLayout.VERTICAL);
	        LayoutParams layout = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	        lLayout.setLayoutParams(layout);
	        
	        Display display = getWindowManager().getDefaultDisplay();
	        int screenWidth = display.getWidth();
	        int screenHeight = display.getHeight();
	                        
	        ImageView imgView = new ImageView(this);
	        imgView.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.startskaerm_light, (int)(screenWidth * 1.3), (int)(screenHeight * 1.3)));
	        imgView.setScaleType(ScaleType.CENTER_CROP);
	        //imgView.setAdjustViewBounds(true); // set the ImageView bounds to match the Drawable's dimensions
	        imgView.setLayoutParams(layout); //.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	                
	        lLayout.addView(imgView);        
	        
	        setContentView(lLayout);
		}
		catch (Exception ex)
		{
			showSplash = false;
		}
		if (!showSplash)
		{
			finish();
            startActivity(new Intent(SplashScreenActivity.this, WebMap2DOnline.class));                   
		}
		else
		{
			SplashScreenActivity.setIsShowing(true);
			Thread splashTread = new Thread() {
		        @Override
		        public void run() {
		            try {	            	
		                int waited = 0;
		                while((waited < SPLASHTIME)) {
		                    sleep(100);
		                    if(!dialogOpen) {
		                        waited += 100;
		                    }
		                }
		            } catch(InterruptedException e) {
		            } finally {
		                finish();
		                SplashScreenActivity.setIsShowing(false);
		                startActivity(new Intent(SplashScreenActivity.this, WebMap2DOnline.class));            
		                //startActivity(new Intent("com.smartcampus.android.ui.maps.online.WebMap2DOnline"));
		                stop();
		            }
		        }
		    };	    
		    splashTread.start();
		}
	};
}
