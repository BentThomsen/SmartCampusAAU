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
using System.Device.Location;
using System.Text;
using SmartCampusWebMap.Ui.Offline.Graph;
using Microsoft.Phone.Shell;
using SmartCampusWebMap.Ui.Dialogs;
using SmartCampusWebMap.RadiomapBackend;
using SmartCampusWebMap.Library.Location;
using Microsoft.Phone.Controls.Maps;

namespace SmartCampusWebMap.Ui.Maps.Offline
{
    public partial class UI_OfflineBingMap : PhoneApplicationPage
    {
        private static readonly string noBuildingErrorMsg = "No building present.\n Please go to the online mode and enable the Wi-Fi provider in order to identify an appropriate building";

        //Contains the functionality whereas this class is concerned with presentation (in Android everything is in Map2DOffline)
        private Map2DOffline mWebMapCommon;
        
        //Map layers for vertices, edges, and the selected location 
        private MapLayer mapLayerVertices;
        private MapLayer mapLayerEdges;
        private MapLayer mapLayerSelectedLocation;
        Image selectedLocationImage;
                
        public UI_OfflineBingMap()
        {
            InitializeComponent();
        
            InitializeMenu();
            InitializeMap();

            HideDPad();
  
            mWebMapCommon = new Map2DOffline();
            //NOTE: DER ER MASSER AF REDUNDANS MELLEM ONLINE OG OFFLINE FASEN

            SetupEventHandlers();
            //force map update
            mWebMapCommon.handleMapReady();
        }

        #region Map2DOffline events
        private void SetupEventHandlers()
        {
            //Update the page's title to correpond to the selected vertex
            mWebMapCommon.OnTitleChange += new EventHandler<Map2D.TitleArgs>(mWebMapCommon_OnTitleChange); // (source, args) => this.ApplicationTitle.Text = args.Title;
            mWebMapCommon.OnCenterChange += new EventHandler<Map2D.CoordinateArgs>(mWebMapCommon_OnCenterChange);  //(sender, coordinateArgs) => graphMap.Center = new GeoCoordinate(coordinateArgs.Latitude, coordinateArgs.Longitude);
            mWebMapCommon.OnVisibleVerticesChange += new EventHandler<Map2D.VisibleVerticesArgs>(mWebMapCommon_OnVisibleVerticesChange);
            mWebMapCommon.OnVisibleEdgesChange += new EventHandler<Map2D.VisibleEdgesArgs>(mWebMapCommon_OnVisibleEdgesChange);
            mWebMapCommon.OnClearOverlayChange += (sender, args) => { ClearOverlays(); };
            mWebMapCommon.OnSelectedLocationChange += new EventHandler<Map2DOffline.SelectedLocationArgs>(mWebMapCommon_OnSelectedLocationChange);
            mWebMapCommon.OnTapChange += new EventHandler(mWebMapCommon_OnTapChange);
            
            //When a symbolic location has been added or modified we update the map to show the correct markers. 
            EditSymbolicLocation.OnSymbolicLocationChange += (source, eventArgs) => mWebMapCommon.refreshUI();
        }                              

        void mWebMapCommon_OnTitleChange(object sender, Map2D.TitleArgs e)
        {
            this.ApplicationTitle.Text = e.Title;
        }

        void mWebMapCommon_OnCenterChange(object sender, Map2D.CoordinateArgs e)
        {
            graphMap.Center = new GeoCoordinate(e.Latitude, e.Longitude);
        }

        void mWebMapCommon_OnVisibleVerticesChange(object sender, Map2D.VisibleVerticesArgs e)
        {
            IEnumerable<Vertex> vertices = e.VisibleVertices;
            if (vertices == null)
                return;

            foreach (Vertex v in vertices)
            {
                AddVertexPin(v);
            }
        }

        void mWebMapCommon_OnVisibleEdgesChange(object sender, Map2D.VisibleEdgesArgs e)
        {
            IEnumerable<Edge> edges = e.VisibleEdges;
            if (edges == null)
                return;

            foreach (Edge curEdge in edges)
            {
                AbsoluteLocation origin = null, destination = null;
                bool first = true;
                foreach (Vertex v in curEdge.Vertices)
                {
                    if (first)
                    {
                        origin = v.AbsoluteLocations.First();
                        first = false;
                    }
                    else
                    {
                        destination = v.AbsoluteLocations.First();
                        break;
                    }
                }
                if (origin != null && destination != null)
                {
                    AddEdgeLine((double)origin.latitude, (double)origin.longitude, (double)destination.latitude, (double)destination.longitude);
                }
            }
        }

        void mWebMapCommon_OnTapChange(object sender, EventArgs e)
        {
            //Go to the Edit Location/Add measurement dialog when the user taps on a vertex
            StringBuilder sb = new StringBuilder();
            sb.Append(Globals.XamlUri_OfflineOnTapAction);
            sb.Append("?title=");
            sb.Append(this.ApplicationTitle.Text);
            this.NavigationService.Navigate(new Uri(sb.ToString(), UriKind.Relative));
        }

        void mWebMapCommon_OnSelectedLocationChange(object sender, Map2DOffline.SelectedLocationArgs e)
        {
            AddSelectedLocation(new GeoCoordinate(e.Latitude, e.Longitude));
        }        
        #endregion

        private Boolean isDPadShown;

        private void ClearOverlays()
        {
            //Note: This may not be the optimal way to clear items in a layer
            mapLayerEdges.Children.Clear();
            mapLayerVertices.Children.Clear();
            mapLayerSelectedLocation.Children.Clear();           
        }

        void InitializeMap()
        {
            graphMap.ZoomLevel = 15;

            mapLayerEdges = new MapLayer();
            mapLayerVertices = new MapLayer();
            mapLayerSelectedLocation = new MapLayer();
            graphMap.Children.Add(mapLayerEdges);
            graphMap.Children.Add(mapLayerVertices);
            graphMap.Children.Add(mapLayerSelectedLocation);
                        
            selectedLocationImage = new Image();
            selectedLocationImage.Source = new System.Windows.Media.Imaging.BitmapImage(new Uri(Globals.IconUri_SelectedLocation, UriKind.Relative));
            selectedLocationImage.Stretch = System.Windows.Media.Stretch.None;            
        }

        private void AddVertexPin(Vertex v)
        {
            //Pushpin pushpin = new Pushpin();
            //pushpin.Style = (Style)(Application.Current.Resources["PushpinStyle"]);

            Image pushpin = MarkerConfig.CreateImage(v);
            pushpin.Tap +=
                (object sender, GestureEventArgs e) =>
                {
                    e.Handled = true;
                    
                    mWebMapCommon.onTap(v); //updates the title

                    //Navigates to the possibilities
                    StringBuilder sb = new StringBuilder();
                    sb.Append(Globals.XamlUri_OfflineOnTapAction);
                    sb.Append("?title=");
                    sb.Append(this.ApplicationTitle.Text);
                    this.NavigationService.Navigate(new Uri(sb.ToString(), UriKind.Relative));
                };
            //pushpin.MouseLeftButtonUp += new MouseButtonEventHandler(pushpin_MouseLeftButtonUp);
            AbsoluteLocation pos = v.AbsoluteLocations.First();
            mapLayerVertices.AddChild(pushpin, new GeoCoordinate((double)pos.latitude, (double)pos.longitude), PositionOrigin.BottomCenter);
        }

        /*
        private void AddVertexPin(Vertex v)
        {
            Pushpin pushpin = new Pushpin();
            pushpin.Style = (Style)(Application.Current.Resources["PushpinStyle"]);
            pushpin.Tap +=
                (object sender, GestureEventArgs e) =>
                {
                    e.Handled = true;

                    mWebMapCommon.onTap(v); //updates the title

                    //Navigates to the possibilities
                    StringBuilder sb = new StringBuilder();
                    sb.Append(Globals.XamlUri_OfflineOnTapAction);
                    sb.Append("?title=");
                    sb.Append(this.ApplicationTitle.Text);
                    this.NavigationService.Navigate(new Uri(sb.ToString(), UriKind.Relative));
                };
            //pushpin.MouseLeftButtonUp += new MouseButtonEventHandler(pushpin_MouseLeftButtonUp);
            AbsoluteLocation pos = v.AbsoluteLocations.First();
            mapLayerVertices.AddChild(pushpin, new GeoCoordinate((double)pos.latitude, (double)pos.longitude), PositionOrigin.BottomCenter);
        } 
         */ 

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

        //Application bar buttons
        private ApplicationBarIconButton menuMeasure, menuFloorChanger, menuNearbyPOI, menuEditGraph;
        //Application bar menu items
        ApplicationBarMenuItem menuDPad;

        private void InitializeMenu()
        {
            ApplicationBar = new ApplicationBar();

            //The four buttons
            menuMeasure = new ApplicationBarIconButton();
            menuMeasure.IconUri = new Uri(Globals.IconUri_Measure, UriKind.Relative);
            menuMeasure.Text = "Measure";
            menuMeasure.Click += new EventHandler(menuMeasure_Click);
            ApplicationBar.Buttons.Add(menuMeasure);
            
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

            menuEditGraph = new ApplicationBarIconButton();
            menuEditGraph.IconUri = new Uri(Globals.IconUri_EditGraph, UriKind.Relative);
            menuEditGraph.Text = "Edit Links"; //Edit Graph -> nu kun links
            menuEditGraph.Click += new EventHandler(menuEditGraph_Click);
            ApplicationBar.Buttons.Add(menuEditGraph);

            //The remaining menu items
            menuDPad = new ApplicationBarMenuItem();
            menuDPad.Text = "Enable D-Pad...";
            menuDPad.Click += new EventHandler(menuDPad_Click);
            ApplicationBar.MenuItems.Add(menuDPad);            
        }

        void SelectBuildingFloor_OnBuildingFloorChange(object sender, SelectBuildingFloor.BuildingFloorEventArgs e)
        {
            Building b = LocationService.CurrentBuilding;
            if (b != null)
            {
                mWebMapCommon.changeToFloor(e.SelectedFloor.Number);
            }
        }

        void menuDPad_Click(object sender, EventArgs e)
        {
            if (isDPadShown)
            {
                HideDPad();
                menuDPad.Text = "Enabled D-pad";
            }
            else
            {
                ShowDPad();
                menuDPad.Text = "Disable D-pad";
            }
        }

        void menuEditGraph_Click(object sender, EventArgs e)
        {
            //NavigationService.Navigate(new Uri(Globals.XamlUri_EditGraph, UriKind.Relative));
            NavigationService.Navigate(new Uri(Globals.XamlUri_EditLinks, UriKind.Relative));
        }

        void menuNearbyPOI_Click(object sender, EventArgs e)
        {
            //Redundans:
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
            Vertex poi = e.SelectedPoi; // ShowNearbyPoi.SelectedVertexPOI;
            AbsoluteLocation absLoc = poi.AbsoluteLocations.First();
            graphMap.Center = new GeoCoordinate((double)absLoc.latitude, (double)absLoc.longitude);
        }

        void menuMeasure_Click(object sender, EventArgs e)
        {
            Building b = LocationService.CurrentBuilding;
            if (b == null)
            {
                MessageBox.Show(noBuildingErrorMsg);
                return;
            }
            
            if (b.Building_Floors == null)
        	{
                MessageBox.Show("You need to add a floor first. (Edit Graph -> Add Floor");        		
        	}
            //SHOULD! be redundant - use current selected floor
        	//else if (!mCurrentBuilding.hasFloorAt(mCurrentSelectedFloor))
            else if (Map2DOffline.SelectedOfflineVertex == null)
            {
                MessageBox.Show("Please select a location on the map first");
            }
            else
            {
                this.NavigationService.Navigate(new Uri(Globals.XamlUri_WifiScanForm, UriKind.Relative));
            }
        }

        void menuFloorChanger_Click(object sender, EventArgs e)
        {
            Building b = LocationService.CurrentBuilding;
            if (b == null)
            {
                MessageBox.Show(noBuildingErrorMsg);
                return;
            }

            if (b.Building_Floors == null)
                MessageBox.Show("No floors have been added");
            else
                this.NavigationService.Navigate(new Uri(Globals.XamlUri_SelectBuildingFloor, UriKind.Relative));
        }

        /// <summary>
        /// Offline specific: Show the sniper icon to indicate the current selected location
        /// </summary>
        /// <param name="geo"></param>
        private void AddSelectedLocation(GeoCoordinate geo)
        {
            mapLayerSelectedLocation.Children.Clear();
            mapLayerSelectedLocation.AddChild(selectedLocationImage, geo, PositionOrigin.Center);
        }

        private void dpad_left_Tap(object sender, GestureEventArgs e)
        {
            //JSInterface.updateSelectedLocation(webBrowserOffline, Map2DOffline.RELATIVE_PIXEL_UPDATE_INTERVAL, 0);
            if (currentClickedPoint != null)
            {
                currentClickedPoint.X -= Map2DOffline.RELATIVE_PIXEL_UPDATE_INTERVAL;
                GeoCoordinate geo = graphMap.ViewportPointToLocation(currentClickedPoint);
                mWebMapCommon.setSelectedLocation(false, mWebMapCommon.getCurrentFloor(), geo.Latitude, geo.Longitude);
            }              
        }

        private void dpad_right_Tap(object sender, GestureEventArgs e)
        {
            //JSInterface.updateSelectedLocation(webBrowserOffline, Map2DOffline.RELATIVE_PIXEL_UPDATE_INTERVAL, 0);
            if (currentClickedPoint != null)
            {
                currentClickedPoint.X += Map2DOffline.RELATIVE_PIXEL_UPDATE_INTERVAL;
                GeoCoordinate geo = graphMap.ViewportPointToLocation(currentClickedPoint);
                mWebMapCommon.setSelectedLocation(false, mWebMapCommon.getCurrentFloor(), geo.Latitude, geo.Longitude);
            }   
        }

        private void dpad_up_Tap(object sender, GestureEventArgs e)
        {
            //JSInterface.updateSelectedLocation(webBrowserOffline, 0, Map2DOffline.RELATIVE_PIXEL_UPDATE_INTERVAL);     
            if (currentClickedPoint != null)
            {
                currentClickedPoint.Y -= Map2DOffline.RELATIVE_PIXEL_UPDATE_INTERVAL;
                GeoCoordinate geo = graphMap.ViewportPointToLocation(currentClickedPoint);
                mWebMapCommon.setSelectedLocation(false, mWebMapCommon.getCurrentFloor(), geo.Latitude, geo.Longitude);
            }            
        }

        private void dpad_down_Tap(object sender, GestureEventArgs e)
        {
            //JSInterface.updateSelectedLocation(webBrowserOffline, 0, Map2DOffline.RELATIVE_PIXEL_UPDATE_INTERVAL);     
            if (currentClickedPoint != null)
            {
                currentClickedPoint.Y += Map2DOffline.RELATIVE_PIXEL_UPDATE_INTERVAL;
                GeoCoordinate geo = graphMap.ViewportPointToLocation(currentClickedPoint);
                mWebMapCommon.setSelectedLocation(false, mWebMapCommon.getCurrentFloor(), geo.Latitude, geo.Longitude); 
            }
        }        
    
        /// <summary>
        /// Represents the point that was most recently clicked/tapped on the screen
        /// This point is updated by  map taps as well as by the dpad
        /// </summary>
        private Point currentClickedPoint;

        /// <summary>
        /// Occurs when the map is tapped
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="curEdge"></param>
        private void graphMap_Tap(object sender, GestureEventArgs e)
        {
            //if a pin was tapped then handled will be true
            if (e.Handled)
                return;

            if (LocationService.CurrentBuilding == null)
            {
                MessageBox.Show(noBuildingErrorMsg);
                return;
            }

            //Add sniper icon and update selected location            
            currentClickedPoint = e.GetPosition(graphMap);
            GeoCoordinate geo = graphMap.ViewportPointToLocation(currentClickedPoint);
            AddSelectedLocation(geo);

            mWebMapCommon.setSelectedLocation(false, mWebMapCommon.getCurrentFloor(), geo.Latitude, geo.Longitude);
        }        

        private void HideDPad()
        {
            dpadPanel.Visibility = System.Windows.Visibility.Collapsed;
            isDPadShown = false;
        }

        private void ShowDPad()
        {
            dpadPanel.Visibility = System.Windows.Visibility.Visible;
            isDPadShown = true;
        }

        /*
        private void dpad_up_ImageFailed(object sender, ExceptionRoutedEventArgs e)
        {

        }
         */ 
    }
}