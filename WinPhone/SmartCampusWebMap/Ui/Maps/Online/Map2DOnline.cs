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

using System;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using Microsoft.Phone.Controls;
using SmartCampusWebMap.RadiomapBackend;
using SmartCampusWebMap.Javascript;
using SmartCampusWebMap.Ui.Maps;
using System.Linq;
using System.Windows.Controls.Primitives;
using System.Collections.Generic;
using SmartCampusWebMap.WifiSnifferPositioningService;
using SmartCampusWebMap.Library.BaseLogic;
using SmartCampusWebMap.Library.Location;
using System.Windows.Threading;
using System.Device.Location;
using System.Text;

namespace SmartCampusWebMap.Ui
{
    //This is the WebMap2DOnline class in Android
    public class Map2DOnline : Map2D
    {
        public event EventHandler OnProviderChange;

        #region Event to notify the UI about a new location estimate
        public class PositionEstimateArgs : EventArgs
        {
            public WifiSnifferPositioningService.PositionEstimate Location { get; private set; }
            public PositionEstimateArgs(WifiSnifferPositioningService.PositionEstimate loc)
            {
                this.Location = loc;
            }
        }
        public event EventHandler<PositionEstimateArgs> OnPositionEstimateChange;
        protected void OnPositionEstimateChanged(object sender, PositionEstimateArgs args)
        {
            if (OnPositionEstimateChange != null)
            {
                OnPositionEstimateChange(sender, args);
            }               
        }
        #endregion

        public enum ProviderStatus { USE_GPS, USE_WIFI, USE_NONE, AUTOMATIC }
        
        //Indicates the current provider (previously static)
        public ProviderStatus mProviderStatus { get; private set; }

        private int mCurrentEstimatedFloor;
        protected static WifiSnifferPositioningService.PositionEstimate lastKnownLocation;

        //Indicates whether we are currently tracking the position, i.curEdge., center around location estimate
        public event EventHandler OnIsTrackingChange;
        
        private bool _isTrackingPosition;
        public bool IsTrackingPosition
        { 
            get { return _isTrackingPosition; }
            set 
            {
                _isTrackingPosition = value;
                if (OnIsTrackingChange != null)
                    OnIsTrackingChange(null, null); //receiver asks for Status
            }
        }
        //last received wifi location estimate
        private WifiSnifferPositioningService.PositionEstimate lastKnownWifiLocation;
        //last location that was shown on a map
        private WifiSnifferPositioningService.PositionEstimate lastUpdatedLocation;
        //Specifies the required number of meters between two consecutive estimated locations before 
        //the most recent estimated location is shown
        private int updateThresholdInMeters = 0;
        
        public Map2DOnline()
            : base()
        {
            Initialize();
        }

        public Map2DOnline(WebBrowser browser)
            : base(browser)
        {
            Initialize();            
        }

        private void Initialize()
        {
            mProviderStatus = ProviderStatus.USE_NONE;
            IsTrackingPosition = false; //notifies the UI to display the proper currentLocationImage

            //Tell the location service to get moving and listen for when a building is downloaded
            LocationService.OnBuildingIdentificationChange += new EventHandler<LocationService.BuildingIdentificationEventArgs>(LocationService_OnBuildingIdentificationChange);
            LocationService.OnBuildingDownload += new EventHandler(LocationService_OnBuildingDownload);
            LocationService.OnPositionEstimateChange += new EventHandler<LocationService.PositionEstimateEventArgs>(LocationService_OnPositionEstimateChange);
            //TODO: REPLACE THIS CALL BY SOMETHING MORE REALISTIC
            //LocationService.initializeRadioMap();
        }

        #region Building identification
        public event EventHandler OnNoBuildingFound;
        void LocationService_OnBuildingIdentificationChange(object sender, LocationService.BuildingIdentificationEventArgs e)
        {
            if (e.BuildingFound)
            {
                LocationService.DownloadRadioMap(e.BuildingId);
            }
            else
            {
                if (OnNoBuildingFound != null)
                    OnNoBuildingFound(this, EventArgs.Empty);                
            }                
        }
        #endregion
        
        private void LocationService_OnPositionEstimateChange(object sender, LocationService.PositionEstimateEventArgs e)
        {
            WifiSnifferPositioningService.PositionEstimate location = e.PositionEstimate;

            //TODO: Insert a check for whether we need to shift building
            //location.Building_ID != lastKnownWifiLocation
            //The rest is part of an else block

            mCurrentEstimatedFloor = (int)location.Altitude; //placed here because we check floor before updating location
			    	
			//First check if we are even interested in receiving position updates?
			if (!IsTrackingPosition)	
			    return;
			 
   	        //Then check whether we need to update floor
			//We change floor if this is the very first wifi location or the location is estimated
			//at a new floor
			bool doChangeFloor = false;
            //Beware: If the locationService changes semantics to broadcasting null estimates (in case of error)
            //then this null check will not be a valid indicator of a floor change
		    if (lastKnownWifiLocation == null) //No prior pos: We change floor			    		
		    {
		    	doChangeFloor = true;
		    }
		    else //Check for new floor
		    {
		    	int prevFloor = (int)lastKnownWifiLocation.Altitude;
			    if (prevFloor != mCurrentEstimatedFloor)
			    	doChangeFloor = true;		    		
		    }
            lastKnownWifiLocation = location;
					
		    if (doChangeFloor)
		    {
		    	mCurrentSelectedFloor = mCurrentEstimatedFloor;
		    	//Globals.ShowDialog(source, "Changing to new floor...", Globals.DURATION_SHORT);
		    	refreshUI();
		    }
		    		
		    //the threshold based check for whether to update the location is conducted in updateNewLocation()
		    //as it applies to gps and wi-fi alike           
		    updateNewLocation(location);
        }
                

        /// <summary>
        /// Called when an attempt has been made to download a building
        /// (This is akin to ServiceConnected). 
        /// </summary>
        /// <param name="sender">Always null</param>
        /// <param name="curEdge">Always null</param>
        private void LocationService_OnBuildingDownload(object sender, EventArgs e)
        {
            Building b = LocationService.CurrentBuilding;
            //case RADIOMAP_READY
            if (b != null)
            {
                mCurrentSelectedFloor = b.Building_Floors.Min(bf => bf.Number); //getInitialFloorNumber()
				mCurrentEstimatedFloor = mCurrentSelectedFloor; //until we get the first estimate
				
                UI_UpdateMap();
														
				if (mProviderStatus == ProviderStatus.USE_WIFI)
				{
					doWifiPositioning();	                    
				}                              
            }
        }

        public override void ScriptNotify(object sender, NotifyEventArgs e)
        {
            base.ScriptNotify(sender, e);
            
            string msg = e.Value;
            string[] msgParts = msg.Split('|');
            string method = msgParts[0];
            if (method == "onTap")
            {
                //"onTap|" + online + "|" + floor + "|" + vertexItem.id
                //get direction or open url
            }
        }

        public void setProvider(ProviderStatus newProviderStatus)
        {
            if (newProviderStatus != mProviderStatus) //we don't use 'mIsFirstFix' from Android
            {
                //update Status
                ProviderStatus oldStatus = mProviderStatus;
                mProviderStatus = newProviderStatus;
                
                //disable previous provider (we are currently only using one provider at a time)
                disableProvider(oldStatus);

                //enable new appropriate provider
                switch (mProviderStatus)
                {
                    case ProviderStatus.USE_GPS:
                        enableGpsProvider();
                        IsTrackingPosition = true;
                        break;
                    case ProviderStatus.USE_WIFI:
                        IsTrackingPosition = true;
                        enableWifiProvider();
                        break;
                    case ProviderStatus.USE_NONE:
                        IsTrackingPosition = false;
                        break;                    
                }
                //let UI update itself
                if (OnProviderChange != null)
                    OnProviderChange(this, EventArgs.Empty);
            }
        }        

        private void disableGpsProvider()
        {
            if (_gpsWatcher != null)
            {
                _gpsWatcher.PositionChanged -= _gpsWatcher_PositionChanged;
                _gpsWatcher = null;
            }
        }

        private void disableProvider(ProviderStatus oldStatus)
        {
            switch (oldStatus)
            {
                case ProviderStatus.USE_GPS:
                    disableGpsProvider();
                    break;
                case ProviderStatus.USE_WIFI:
                    disableWifiProvider();
                    break;
            }
        }
        private void disableWifiProvider()
        {
            LocationService.stopWifiPositioning();
        }

        //Consider refactoring, so we have one place for initializing wifi positioning
        private void doWifiPositioning()
        {
            Building b = LocationService.CurrentBuilding;
            if (b != null && mProviderStatus == ProviderStatus.USE_WIFI)
            {
                //LocationService.startWifiPositioning(this.mac);
                //register listener for location updates			
                
            }
        }

        GeoCoordinateWatcher _gpsWatcher;
        //Yeah, we just wrap the original event
        public event EventHandler<GeoPositionChangedEventArgs<GeoCoordinate>> OnGpsPositionChange;
        private bool enableGpsProvider()
        {            
            //Specify high accuracy in order to use GPS
            _gpsWatcher = new GeoCoordinateWatcher(); // (GeoPositionAccuracy.High);
            _gpsWatcher.MovementThreshold = 5;

            _gpsWatcher.PositionChanged +=new EventHandler<GeoPositionChangedEventArgs<GeoCoordinate>>(_gpsWatcher_PositionChanged);
            _gpsWatcher.Start();
            return true;
        }

        private void _gpsWatcher_PositionChanged(object sender, GeoPositionChangedEventArgs<GeoCoordinate> e)
        {
            //We just wrap the original event
            //Reason: We preserve the enableGpsProvider() and disableGpsProvider() methods from the 
            //Android version
            if (OnGpsPositionChange != null)
            {
                OnGpsPositionChange(this, e);
            }

            if (LocationService.AllowTracking)
            {
                LocationService.AddToTrackedPositions(e.Position.Location);
            }

        }

        private void enableWifiProvider()
        {            
            LocationService.startWifiPositioning();
        }

        private static bool displayRadiomapDownloadNotifications()
        {
            return true; //Always show
            //return !SplashScreenActivity.isShowing();
        }        

        public override int getCurrentFloor()
        {
            return mCurrentEstimatedFloor;
        }

        protected override IEnumerable<Vertex> getVisibleVertices(int floorNum)
        {
            Building b = LocationService.CurrentBuilding;
            if (b == null || b.Vertices == null)
                return null;

            return 
                from v in b.Vertices
                let symLoc = v.SymbolicLocations.FirstOrDefault()
                let absLoc = v.AbsoluteLocations.First()
                where symLoc != null && (int)absLoc.altitude == floorNum
                select v;

            //Only show locations that have a symbolic location
            //return b.Vertices.Where(v => v.SymbolicLocations.FirstOrDefault() != null);
        }

        public override void onTap(int floorNum, int vertexId) {
    	    const int GET_DIRECTION = 0;
		    const int OPEN_URL = 1;
		    const String GET_DIRECTION_TEXT = "Get Directions";
		    const String OPEN_URL_TEXT = "Open URL";
		
            //HACK: Replace with graph
		    Vertex v = LocationService.CurrentBuilding.Vertices.FirstOrDefault(v1 => v1.ID == vertexId);	
		    SymbolicLocation symLoc = v.SymbolicLocations.FirstOrDefault();
		    //if (symLoc == null)				
		
		    String[] items;
		    if (symLoc == null || (!isLocationUrlWellFormed(symLoc.url)))
		    {
			    items = new String[1]; //URL is not applicable
			    items[GET_DIRECTION] = GET_DIRECTION_TEXT;
		    }
		    else { //url is well-formed
			    items = new String[2];
			    items[GET_DIRECTION] = GET_DIRECTION_TEXT;
			    items[OPEN_URL] = OPEN_URL_TEXT;
		    }
		
            //TODO: Create Dialog, i.curEdge., new Page in WP 7
		    //Two choices: Get directions or browse symbolic location's url            
        }    

        private bool isLocationUrlWellFormed(String url) {
		    return url != null && (url.StartsWith("http://") || url.StartsWith("www."));
	    }

        private void updateNewLocation(WifiSnifferPositioningService.PositionEstimate location)
        {
            double distToPrev = -1; //placed here so I can see its value throughoutu the mehthod (for debugging)

            if (location != null)
            {
                bool showNewLocation = true;
                if (lastUpdatedLocation != null)
                {
                    //ekstra tjek - paa threshold    		
                    double oldLat = lastUpdatedLocation.Latitude;
                    double oldLng = lastUpdatedLocation.Longitude;
                    double newLat = location.Latitude;
                    double newLng = location.Longitude;
                    distToPrev = DistanceMeasurements.CalculateMoveddistanceInMeters(oldLat, oldLng, newLat, newLng);

                    if (distToPrev <=  this.updateThresholdInMeters)
                    {
                        showNewLocation = false;
                    }
                }
                if (showNewLocation)
                {
                    UI_UpdateNewLocation(location);
                    lastUpdatedLocation = location; //NOTE: MAYBE important to COPY as done here. 
                }
                lastKnownLocation = location;
            }
        }

        private void UI_UpdateNewLocation(WifiSnifferPositioningService.PositionEstimate location)
        {
            if (uiMode == UiMode.GOOGLE_MAPS)
            {
                JSInterface.updateNewLocation(browser, location);                    
            }
            else if (uiMode == UiMode.BING_MAPS)
            {
                OnPositionEstimateChanged(this, new PositionEstimateArgs(location));
            }
        }
    }
}
