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

package com.smartcampus.webclient.snifferbackend;

public class InfrastructurePositionEstimate {
	
    private int id;
    private int buildingId;
    private int vertexId;
    private double latitude;
    private double longitude;
    private double altitude;
    private String provider;
    private long time;
    private double accuracy;
    private double speed;
    private double bearing;
    private boolean hasAccuracy;
    private boolean hasSpeed;
    private boolean hasBearing;
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param buildingId the buildingId to set
	 */
	public void setBuildingId(int buildingId) {
		this.buildingId = buildingId;
	}
	/**
	 * @return the buildingId
	 */
	public int getBuildingId() {
		return buildingId;
	}
	/**
	 * @param vertexId the vertexId to set
	 */
	public void setVertexId(int vertexId) {
		this.vertexId = vertexId;
	}
	/**
	 * @return the vertexId
	 */
	public int getVertexId() {
		return vertexId;
	}
	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}
	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}
	/**
	 * @param altitude the altitude to set
	 */
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
	/**
	 * @return the altitude
	 */
	public double getAltitude() {
		return altitude;
	}	
	/**
	 * @param provider the provider to set
	 */
	public void setProvider(String provider) {
		this.provider = provider;
	}
	/**
	 * @return the provider
	 */
	public String getProvider() {
		return provider;
	}
	/**
	 * @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;		
	}
	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}
	/**
	 * @param accuracy the accuracy to set
	 */
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}
	/**
	 * @return the accuracy
	 */
	public double getAccuracy() {
		return accuracy;
	}
	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	/**
	 * @return the speed
	 */
	public double getSpeed() {
		return speed;
	}
	/**
	 * @param bearing the bearing to set
	 */
	public void setBearing(double bearing) {
		this.bearing = bearing;
	}
	/**
	 * @return the bearing
	 */
	public double getBearing() {
		return bearing;
	}
	/**
	 * @param hasAccuracy the hasAccuracy to set
	 */
	public void setHasAccuracy(boolean hasAccuracy) {
		this.hasAccuracy = hasAccuracy;
	}
	/**
	 * @return the hasAccuracy
	 */
	public boolean hasAccuracy() {
		return hasAccuracy;
	}
	/**
	 * @param hasSpeed the hasSpeed to set
	 */
	public void setHasSpeed(boolean hasSpeed) {
		this.hasSpeed = hasSpeed;
	}
	/**
	 * @return the hasSpeed
	 */
	public boolean hasSpeed() {
		return hasSpeed;
	}
	/**
	 * @param hasBearing the hasBearing to set
	 */
	public void setHasBearing(boolean hasBearing) {
		this.hasBearing = hasBearing;
	}
	/**
	 * @return the hasBearing
	 */
	public boolean hasBearing() {
		return hasBearing;
	}
}
