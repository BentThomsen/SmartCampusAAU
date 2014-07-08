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

package com.smartcampus.indoormodel;

import java.io.Serializable;

public class AbsoluteLocation implements Serializable
{
	private static final long serialVersionUID = 1L;

    private double latitude, longitude, altitude;
    private int id;
    
    public AbsoluteLocation(double latitude, double longitude, double altitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null)
        	return false;
        if (!(o instanceof AbsoluteLocation))
        	return false;
        
        //todo: We account for double imprecision by multiplying
        AbsoluteLocation other = (AbsoluteLocation)o;
        return
        	this.latitude * 1E6 == other.latitude * 1E6 &&
        	this.longitude * 1E6 == other.longitude * 1E6 &&
        	this.altitude * 1E6 == other.altitude * 1E6;    	
    }
    
    public double getAltitude()
    {
        return altitude;
    }
    
    public int getId()
    {
    	return this.id;
    }
    
    public double getLatitude()
    {
        return latitude;
    }
    
    public double getLongitude()
    {
        return longitude; 
    }
    

    @Override
    public int hashCode()
    {
    	return
    		(int)(this.latitude * 1E6)  ^
    		(int)(this.longitude * 1E6) ^
    		(int)(this.altitude * 1E6);
    }
    
    public void setAltitude(double altitude)
    {
        this.altitude = altitude;
    }
    
    public void setId(int value)
    {
    	this.id = value;
    }
    
    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }
    
    public void setLongitude(double longitude)
    {
        this.longitude = longitude; 
    }
}

