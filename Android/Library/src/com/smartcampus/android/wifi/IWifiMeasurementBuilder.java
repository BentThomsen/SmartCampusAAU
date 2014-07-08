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

package com.smartcampus.android.wifi;

import com.smartcampus.wifi.WifiMeasurement;

/**
 * 
 * @author rhansen
 * Usage:
 * 0. Enable wifi.
 * 1. startMeasuring()
 * 2. stopMeasuring()
 * 3. getLastMeasurement()
 */
public interface IWifiMeasurementBuilder {
	/**
	 * Starts a scan.
	 */
	public void startMeasuring();	
	/**
	 * Stops an ongoing scan
	 */
	public void stopMeasuring();
	/**
	 * @return Is a measurement in progress
	 */
	public boolean isMeasuring();
	/**
	 * @return The last measurement. null if no measurement has been taken yet or since stopMeasuring().
	 * The scanning will be stopped (i.e., make sure you call stopMeasuring internally)
	 */
	public WifiMeasurement getLastMeasurement();
	/**
	 * Tries to enable wifi by turning on the wifi adapter.
	 * @return Reports on the success of trying to turn on the wifi adapter.
	 */
	public boolean enableWifi();
	/**
	 * @return Reports whether wifi is enabled.
	 */
	public boolean isWifiEnabled();
}
