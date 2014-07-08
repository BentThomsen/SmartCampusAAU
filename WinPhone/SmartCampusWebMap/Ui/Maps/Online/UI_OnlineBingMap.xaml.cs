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
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using Microsoft.Phone.Controls;
using Microsoft.Phone.Controls.Maps;
using System.Device.Location;
using SmartCampusWebMap.RadiomapBackend;
using SmartCampusWebMap.Library.Location;
using System.Windows.Media.Imaging;
using Microsoft.Phone.Shell;
using SmartCampusWebMap.Ui.Dialogs;
using Microsoft.Phone.Controls.Maps.Platform;
using SmartCampusWebMap.Ui.Maps;
using System.Text;

namespace SmartCampusWebMap.Ui
{
    public partial class UI_OnlineBingMap : PhoneApplicationPage
    {
        private static readonly string noBuildingErrorMsg = "No building present.\n Please enable the Wi-Fi provider in order to identify an appropriate building";

        private MapLayer mapLayerVertices;
        private MapLayer mapLayerEdges;
        private MapLayer mapLayerCurrentLocation;
        Image currentLocationImage;

        public UI_OnlineBingMap()
        {
            InitializeComponent();
            ApplicationTitle.Text = Globals.AppName;

            InitializeTrackingImages();
            //NOTE: Redundans omkring application bar buttons og handlinger, f.eks. changeFloor
            InitializeMenu();
            InitializeMap();
            InitializeWifiProgressIndicator();

            mWebMapCommon = new Map2DOnline();
            mWebMapCommon.OnTitleChange += (source, titleArgs) => this.ApplicationTitle.Text = titleArgs.Title;
            mWebMapCommon.OnCenterChange += (sender, coordinateArgs) => graphMap.Center = new GeoCoordinate(coordinateArgs.Latitude, coordinateArgs.Longitude);
            mWebMapCommon.OnVisibleVerticesChange +=
                (sender, visibleVerticesArgs) =>
                {
                    IEnumerable<Vertex> vertices = visibleVerticesArgs.VisibleVertices;
                    if (vertices == null)
                        return;

                    foreach (Vertex v in vertices)
                    {
                        AddVertexPin(v);
                    }
                };            
            mWebMapCommon.OnClearOverlayChange += (sender, args) => { ClearOverlays(); };
            mWebMapCommon.OnIsTrackingChange +=
                (source, irrelevantArgs) =>
                {
                    if (mWebMapCommon.IsTrackingPosition)
                    {
                        trackImg.Source = imgTrackingOn;
                    }
                    else
                    {
                        trackImg.Source = imgTrackingOff;
                    }
                };

            mWebMapCommon.OnGpsPositionChange += (object sender, GeoPositionChangedEventArgs<GeoCoordinate> args) =>
                {
                    UI_UpdateCurrentPosition(args.Position.Location.Latitude, args.Position.Location.Longitude);                    
                };
            mWebMapCommon.OnPositionEstimateChange +=
                (sender, positionEstimateArgs) =>
                {
                    //Update the UI (invoked from a background thread - hence Dispatch.Invoke
                    Dispatcher.BeginInvoke(
                        () =>
                        {
                            UI_UpdateCurrentPosition(positionEstimateArgs.Location.Latitude, positionEstimateArgs.Location.Longitude);
                            
                        }
                    );
                };
            mWebMapCommon.OnNoBuildingFound += (sender, args) =>
                {
                    StringBuilder sb = new StringBuilder();
                    sb.Append("No building found. \n");
                    sb.Append("This may be caused by three things: \n");
                    sb.Append("1) A connection problem. Please check that you have connectivity\n");
                    sb.Append("2) An invalid Mac address. Please check the menu item 'set mac address'\n");
                    sb.Append("3) No Wi-Fi sniffer infrastructure is present. In this case, please contact the appropriate building administrator");
                    Dispatcher.BeginInvoke(() =>
                        {
                            MessageBox.Show(sb.ToString());
                            wifiProgressIndicator.IsVisible = false;
                            mWebMapCommon.setProvider(Map2DOnline.ProviderStatus.USE_NONE);
                        });
                };
            mWebMapCommon.OnProviderChange += (sender, args) =>
                {
                    switch (mWebMapCommon.mProviderStatus)
                    {
                        case Map2DOnline.ProviderStatus.USE_GPS:
                            menuProvider.IconUri = new Uri(Globals.IconUri_ProviderGps, UriKind.Relative);
                            break;
                        case Map2DOnline.ProviderStatus.USE_WIFI:
                            wifiProgressIndicator.IsVisible = true;    
                            menuProvider.IconUri = new Uri(Globals.IconUri_ProviderWifi, UriKind.Relative);
                            break;
                        case Map2DOnline.ProviderStatus.USE_NONE:
                            menuProvider.IconUri = new Uri(Globals.IconUri_ProviderNone, UriKind.Relative);
                            break;
                    }
                };
            mWebMapCommon.setProvider(Map2DOnline.ProviderStatus.USE_NONE);
        }

        private void UI_UpdateCurrentPosition(double lat, double lon)
        {
            //If the progress indicator is showing (we have not yet gotten a wifi fix) we remove it.
            wifiProgressIndicator.IsVisible = false;
    
            mapLayerCurrentLocation.Children.Clear();
            //apparently, it is necessary to create a new object in order to get rid of the old parent (which is readonly)
            currentLocationImage = new Image();
            currentLocationImage.Source = new System.Windows.Media.Imaging.BitmapImage(new Uri(Globals.IconUri_MyLocation, UriKind.Relative));
            currentLocationImage.Stretch = System.Windows.Media.Stretch.None;        

            GeoCoordinate geo = new GeoCoordinate(lat, lon);
            mapLayerCurrentLocation.AddChild(currentLocationImage, geo, PositionOrigin.Center);
            graphMap.Center = geo; 
        }

        private void ClearOverlays()
        {            
            //Note: This may not be the optimal way to clear items in a layer
            if (mapLayerEdges != null)
            {
                graphMap.Children.Remove(mapLayerEdges);
            }
            if (mapLayerVertices != null)
            {
                graphMap.Children.Remove(mapLayerVertices);
            }
            if (mapLayerCurrentLocation != null)
            {
                graphMap.Children.Remove(mapLayerCurrentLocation);
            }
            mapLayerEdges = new MapLayer();
            mapLayerVertices = new MapLayer();
            mapLayerCurrentLocation = new MapLayer();
            graphMap.Children.Add(mapLayerEdges);
            graphMap.Children.Add(mapLayerVertices);
            graphMap.Children.Add(mapLayerCurrentLocation);                 
        }

        void InitializeMap()
        {
            graphMap.ZoomLevel = 15;
            
            currentLocationImage = new Image();
            currentLocationImage.Source = new System.Windows.Media.Imaging.BitmapImage(new Uri(Globals.IconUri_MyLocation, UriKind.Relative));
            currentLocationImage.Stretch = System.Windows.Media.Stretch.None;            
            
            ClearOverlays();
        }

        void InitializeBuilding()
        {
            //LocationService.OnBuildingDownload -= LocationService_OnBuildingDownload;
            Building b = LocationService.CurrentBuilding;
            if (b == null)
                return;

            //Map initialization
            graphMap.Center = new GeoCoordinate((double)b.Lat, (double)b.Lon);
            graphMap.ZoomLevel = 15;
            mapLayerVertices = new MapLayer();
            graphMap.Children.Add(mapLayerVertices);
            
            //vertexMap.MouseLeftButtonUp += new MouseButtonEventHandler(vertexMap_MouseLeftButtonUp);

            AbsoluteLocation absLoc;
            foreach (Vertex v in b.Vertices)
            {
                absLoc = v.AbsoluteLocations.First();
                AddVertexPin(v);
            }
        }

        private void AddVertexPin(Vertex v)
        {
            //Pushpin pushpin = new Pushpin();
            //pushpin.Style = (Style)(Application.Current.Resources["PushpinStyle"]);
            Image pushpin = MarkerConfig.CreateImage(v);
            //pushpin.Tap += new EventHandler<GestureEventArgs>(pushpin_Tap);
            pushpin.Tap += (object sender, GestureEventArgs args) =>
                {
                    SymbolicLocation symLoc = v.SymbolicLocations.FirstOrDefault();
                    if (symLoc != null)
                    {
                        string title = symLoc.title ?? "";
                        string description = symLoc.description ?? "";
                        string url = symLoc.url ?? "";
                        MessageBox.Show(string.Format("{0}\n{1}\n{2}", title, description, url));
                    }
                };

            AbsoluteLocation pos = v.AbsoluteLocations.First();
            mapLayerVertices.AddChild(pushpin, new GeoCoordinate((double)pos.latitude, (double)pos.longitude), PositionOrigin.BottomCenter);
        }        

        private void AddEdgeLine(double originLat, double originLon, double destinationLat, double destinationLon)
        {
            MapPolyline edge = new MapPolyline();
            edge.Stroke = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Colors.Red);
            edge.StrokeThickness = 5;
            edge.Opacity = 0.7;
            edge.Locations = new LocationCollection()
            {
                new GeoCoordinate(originLat, originLon),
                new GeoCoordinate(destinationLat, destinationLon)
            };
            mapLayerEdges.Children.Add(edge);
        }         
        

        /******************************************** Common UI section ***********************************/
        #region Common UI (same using Google maps
        
        //displays upload progress
        ProgressIndicator wifiProgressIndicator;
        private void InitializeWifiProgressIndicator()
        {
            wifiProgressIndicator = new ProgressIndicator()
            {
                IsVisible = false,
                IsIndeterminate = true,
                Text = "Determining indoor Wi-Fi location ..."
            };
            SystemTray.SetProgressIndicator(this, wifiProgressIndicator);
        }

        private static BitmapImage imgTrackingOn, imgTrackingOff;

        private void InitializeTrackingImages()
        {
            //Med billeder som 'ressource'
            ///SmartCampusAAU;component/Images/drawable-hdpi/tracking_off.png
            const string resourcePrefix = "/SmartCampusAAU;component";
            imgTrackingOn = new BitmapImage(new Uri(resourcePrefix + Globals.IconUri_TrackingOn, UriKind.RelativeOrAbsolute));
            imgTrackingOff = new BitmapImage(new Uri(resourcePrefix + Globals.IconUri_TrackingOff, UriKind.RelativeOrAbsolute));
            //Med billeder som 'content'
            /* Billeder som content:
            imgTrackingOn = new BitmapImage(new Uri(Globals.IconUri_TrackingOn, UriKind.RelativeOrAbsolute));
            imgTrackingOff = new BitmapImage(new Uri(Globals.IconUri_TrackingOff, UriKind.RelativeOrAbsolute));
            */
        }

        private Map2DOnline mWebMapCommon;

        //Application bar buttons
        private ApplicationBarIconButton menuProvider, menuFloorChanger, menuNearbyPOI, menuRoute;
        //Application bar menu items
        ApplicationBarMenuItem menuAbout, menuOffline, menuSaveMac, menuSetTracking; // menuFindNearest, menuSearch, 
        private void InitializeMenu()
        {
            ApplicationBar = new ApplicationBar();

            //General idea for most: Open "dialog" (separate page) where selection is made
            //Result of selection is broadcasted as an event which is the captured in this page. 
            //The four buttons
            menuProvider = new ApplicationBarIconButton();
            menuProvider.IconUri = new Uri(Globals.IconUri_ProviderNone, UriKind.Relative);
            menuProvider.Text = "Provider Off";
            menuProvider.Click += new EventHandler(menuProvider_Click);
            ApplicationBar.Buttons.Add(menuProvider);
            SelectProvider.OnProviderChange += new EventHandler<SelectProvider.ProviderEventArgs>(SelectProvider_OnProviderChange);

            menuFloorChanger = new ApplicationBarIconButton();
            menuFloorChanger.IconUri = new Uri(Globals.IconUri_ChangeFloor, UriKind.Relative);
            menuFloorChanger.Text = "Floors";
            menuFloorChanger.Click += new EventHandler(menuFloorChanger_Click);
            ApplicationBar.Buttons.Add(menuFloorChanger);
            SelectBuildingFloor.OnBuildingFloorChange += new EventHandler<SelectBuildingFloor.BuildingFloorEventArgs>(SelectBuildingFloor_OnBuildingFloorChange);

            menuNearbyPOI = new ApplicationBarIconButton();
            menuNearbyPOI.IconUri = new Uri(Globals.IconUri_NearbyPOI, UriKind.Relative);
            menuNearbyPOI.Text = "Nearby POI";
            menuNearbyPOI.Click += new EventHandler(menuNearbyPOI_Click);
            ApplicationBar.Buttons.Add(menuNearbyPOI);
            ShowNearbyPoi.OnPoiSelectionChange += new EventHandler<ShowNearbyPoi.PoiEventArgs>(ShowNearbyPoi_OnPoiSelected);

            menuRoute = new ApplicationBarIconButton();
            menuRoute.IconUri = new Uri(Globals.IconUri_Route, UriKind.Relative);
            menuRoute.Text = "Clear Route";
            menuRoute.Click += new EventHandler(menuRoute_Click);
            menuRoute.IsEnabled = false;
            ApplicationBar.Buttons.Add(menuRoute);
            
            //The remaining menu items
            //menuFindNearest = new ApplicationBarMenuItem();
            //menuFindNearest.Text = "Find Nearest...";
            //pic = ic_menu_find_nearest
            //ApplicationBar.MenuItems.Add(menuFindNearest);
            //menuFindNearest.Click += new EventHandler(menuFindNearest_Click);

            //menuSearch = new ApplicationBarMenuItem();
            //menuSearch.Text = "Search";
            //pic = ic_menu_search
            //ApplicationBar.MenuItems.Add(menuSearch);
            //menuSearch.Click += new EventHandler(menuSearch_Click);
            
            menuAbout = new ApplicationBarMenuItem();
            menuAbout.Text = "About...";
            //No pic
            ApplicationBar.MenuItems.Add(menuAbout);
            menuAbout.Click += new EventHandler(menuAbout_Click);

            menuOffline = new ApplicationBarMenuItem();
            menuOffline.Text = "Offline";
            //ic_menu_off
            ApplicationBar.MenuItems.Add(menuOffline);
            menuOffline.Click += new EventHandler(menuOffline_Click);

            menuSaveMac = new ApplicationBarMenuItem();
            menuSaveMac.Text = "Set Mac Address";
            ApplicationBar.MenuItems.Add(menuSaveMac);
            menuSaveMac.Click += new EventHandler(menuSaveMac_Click);

            menuSetTracking = new ApplicationBarMenuItem();
            menuSetTracking.Text = "Set Tracking";
            ApplicationBar.MenuItems.Add(menuSetTracking);
            menuSetTracking.Click += new EventHandler(menuSetTracking_Click); 
        }
                
        void SelectBuildingFloor_OnBuildingFloorChange(object sender, SelectBuildingFloor.BuildingFloorEventArgs e)
        {
            Building b = LocationService.CurrentBuilding;
            if (b != null)
            {
                mWebMapCommon.changeToFloor(e.SelectedFloor.Number);
            }
        }

        private void menuProvider_Click(object sender, EventArgs e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(Globals.XamlUri_SelectProvider);
            sb.Append("?CurrentProvider=");
            sb.Append((int)mWebMapCommon.mProviderStatus);
            this.NavigationService.Navigate(new Uri(sb.ToString(), UriKind.Relative));
            
            //this.NavigationService.Navigate(new Uri(Globals.XamlUri_SelectProvider, UriKind.Relative));
        }

        //Maybe TODO (cleaner): Have Map2DOnline raise a 'ProviderChanged' event which then prompts an update to the menu item and tracking button
        void SelectProvider_OnProviderChange(object sender, SelectProvider.ProviderEventArgs e)
        {
            mWebMapCommon.setProvider(e.ProviderStatus);            
        }

        private void menuFloorChanger_Click(object sender, EventArgs e)
        {
            Building b = LocationService.CurrentBuilding;
            if (b == null)
                MessageBox.Show(noBuildingErrorMsg);
            else if (b.Building_Floors == null)
                MessageBox.Show("No floors have been added");
            else
            {
                this.NavigationService.Navigate(new Uri(Globals.XamlUri_SelectBuildingFloor, UriKind.Relative));
            }
        }

        private void menuNearbyPOI_Click(object sender, EventArgs e)
        {
            Building b = LocationService.CurrentBuilding;
            if (b == null)
                MessageBox.Show(noBuildingErrorMsg);
            else if (b.Building_Floors == null)
                MessageBox.Show("No floors have been added"); //want to show POIs for the current floor
            else
            {

                NavigationService.Navigate(new Uri(Globals.XamlUri_ShowNearbyPoi, UriKind.Relative));
            }
        }

        void ShowNearbyPoi_OnPoiSelected(object sender, ShowNearbyPoi.PoiEventArgs e)
        {
            Vertex poi = e.SelectedPoi;
            AbsoluteLocation absLoc = poi.AbsoluteLocations.First();
            mWebMapCommon.UI_CenterAt((double)absLoc.latitude, (double)absLoc.longitude);         
        }

        private void menuRoute_Click(object sender, EventArgs e)
        {
        }

        private void menuFindNearest_Click(object sender, EventArgs e)
        {

        }

        private void menuSearch_Click(object sender, EventArgs e)
        {

        }

        private void menuAbout_Click(object sender, EventArgs e)
        {
            NavigationService.Navigate(new Uri(Globals.XamlUri_About, UriKind.Relative));
        }

        private void menuOffline_Click(object sender, EventArgs e)
        {
            Building b = LocationService.CurrentBuilding;
            if (b == null)
                MessageBox.Show(noBuildingErrorMsg);
            else
                this.NavigationService.Navigate(new Uri(Globals.XamlUri_OfflineBingMap, UriKind.Relative));
        }

        private void menuSaveMac_Click(object sender, EventArgs e)
        {
            this.NavigationService.Navigate(new Uri(Globals.XamlUri_SaveMacAddress, UriKind.Relative));
        }

        void menuSetTracking_Click(object sender, EventArgs e)
        {
            this.NavigationService.Navigate(new Uri(Globals.XamlUri_SetTracking, UriKind.Relative));
        }

        private void trackBtn_Click(object sender, RoutedEventArgs e)
        {
            if (mWebMapCommon.mProviderStatus != Map2DOnline.ProviderStatus.USE_NONE)
                mWebMapCommon.IsTrackingPosition = !mWebMapCommon.IsTrackingPosition;
        }

        /*
        private void trackImg_Tap(object sender, GestureEventArgs e)
        {
            mWebMapCommon.IsTrackingPosition = !mWebMapCommon.IsTrackingPosition;
        }
         */ 
        #endregion    
   
        #region Drawing the estimate circle
        /*
        void AddCircle(Location oLoc, double dRadius, double dOpacity)
        {
            MapPolygon polygon = new MapPolygon();
            polygon.Fill = new SolidColorBrush(Colors.Green);
            polygon.Stroke = new SolidColorBrush(Colors.Blue);
            polygon.StrokeThickness = 5;
            polygon.Opacity = dOpacity;

            //this works in miles
            polygon.Locations = DrawACircle(oLoc, dRadius);
            Map1.AddChild(polygon);
        }

        public LocationCollection DrawACircle(Location oLoc, double dRadius)
        {
            var oLocs = new LocationCollection();

            var earthRadius = GeoCodeCalc.EarthRadiusInMiles;
            var lat = GeoCodeCalc.ToRadian(oLoc.Latitude); //radians 
            var lon = GeoCodeCalc.ToRadian(oLoc.Longitude); //radians 
            var d = dRadius / earthRadius; // d = angular distance covered on earths surface 

            for (int x = 0; x <= 360; x++)
            {
                var brng = GeoCodeCalc.ToRadian(x); //radians 
                var latRadians = Math.Asin(Math.Sin(lat) * Math.Cos(d) + Math.Cos(lat) * Math.Sin(d) * Math.Cos(brng));
                var lngRadians = lon + Math.Atan2(Math.Sin(brng) * Math.Sin(d) * Math.Cos(lat), Math.Cos(d) - Math.Sin(lat) * Math.Sin(latRadians));

                var pt = new Location(180.0 * latRadians / Math.PI, 180.0 * lngRadians / Math.PI);
                oLocs.Add(pt);
            }
            return oLocs;
        }
         * */
        #endregion
    }
}