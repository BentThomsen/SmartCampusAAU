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

package com.smartcampus.wifi;

import android.net.wifi.ScanResult;

/**
 * The MacInfo is a watered down version of Android's ScanResult class.
 * Used to carry additional info about an access point (mac address, i.e., BSSID)
 * and service set identifier (ssid). 
 * @author admin
 *
 */
public class MacInfo {
	
	private String mMac; //aka BSSID
	private String mSSID; //aka network name
	
	private static final String VALUE_NOT_SUPPLIED = "Value not supplied";
	
	public MacInfo()
	{
		initialize(VALUE_NOT_SUPPLIED, VALUE_NOT_SUPPLIED);
	}
	
	public MacInfo(ScanResult androidScanResult)
	{
		this.mMac = androidScanResult.BSSID;
		this.mSSID = androidScanResult.SSID;
	}
	
	/*
	public MacInfo(String mac, String ssid)
	{
		initialize(ssid, mac);
	}
	*/
	
	public String getBSSID()
	{
		return mMac;
	}
		
	public String getSSID()
	{
		return mSSID;
	}
	private void initialize(String ssid, String mac)
	{
		this.mSSID = ssid;
		this.mMac = mac;
		//ready for further properties if we so desire
	}
	
	public void setBSSID(String bssid)
	{
		this.mMac = bssid;
	}
	public void setSSID(String ssid)
	{
		this.mSSID = ssid;
	}
}
