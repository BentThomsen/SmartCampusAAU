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
using Microsoft.Phone.Shell;
using SmartCampusWebMap.RadiomapBackend;
using SmartCampusWebMap.Javascript;
using SmartCampusWebMap.Ui.Dialogs;
using System.Text;
using SmartCampusWebMap.Ui.Offline.Graph;
using SmartCampusWebMap.Library.Location;

namespace SmartCampusWebMap.Ui.Maps.Offline
{
    public partial class UI_OfflineGoogleMap : PhoneApplicationPage
    {        
        public UI_OfflineGoogleMap()
        {
            InitializeComponent();
            InitializeMenu();
            HideDPad();
  
            mWebMapCommon = new Map2DOffline(webBrowserOffline);
            //Update the page's title to correpond to the selected vertex
            mWebMapCommon.OnTitleChange += (source, args) => this.ApplicationTitle.Text = args.Title;
            //Go to the Edit Location/Add measurement dialog when the user taps on a vertex
            mWebMapCommon.OnTapChange += (source, args) =>
                {
                    StringBuilder sb = new StringBuilder();
                    sb.Append(Globals.XamlUri_OfflineOnTapAction);
                    sb.Append("?title=");
                    sb.Append(this.ApplicationTitle.Text);
                    this.NavigationService.Navigate(new Uri(sb.ToString(), UriKind.Relative));
                };

            //When a symbolic location has been added or modified we update the map to show the correct markers. 
            EditSymbolicLocation.OnSymbolicLocationChange += (source, eventArgs) => mWebMapCommon.refreshUI();
        }

        private Map2DOffline mWebMapCommon;
        private Boolean isDPadShown;

        private void HideDPad()
        {
            dpad_left.Visibility = System.Windows.Visibility.Collapsed;
            dpad_right.Visibility = System.Windows.Visibility.Collapsed;
            dpad_up.Visibility = System.Windows.Visibility.Collapsed;
            dpad_down.Visibility = System.Windows.Visibility.Collapsed;

            isDPadShown = false;
        }

        private void ShowDPad()
        {
            dpad_left.Visibility = System.Windows.Visibility.Visible;
            dpad_right.Visibility = System.Windows.Visibility.Visible;
            dpad_up.Visibility = System.Windows.Visibility.Visible;
            dpad_down.Visibility = System.Windows.Visibility.Visible;

            isDPadShown = true;
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
            menuEditGraph.Text = "Clear Route";
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
            NavigationService.Navigate(new Uri(Globals.XamlUri_EditGraph, UriKind.Relative));
        }

        void menuNearbyPOI_Click(object sender, EventArgs e)
        {
            //Redundans:
            Building b = LocationService.CurrentBuilding;
            if (b == null)
                MessageBox.Show("Unknown Building");
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
            JSInterface.centerAt(webBrowserOffline, (double)absLoc.latitude, (double)absLoc.longitude);
        }

        void menuMeasure_Click(object sender, EventArgs e)
        {
            Building b = LocationService.CurrentBuilding;
            if (b == null)
                return;

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
            if (b.Building_Floors == null)
                MessageBox.Show("No floors have been added");
            else
                this.NavigationService.Navigate(new Uri(Globals.XamlUri_SelectBuildingFloor, UriKind.Relative));
        }

        /*
        private void dpad_left_ImageFailed(object sender, ExceptionRoutedEventArgs curEdge)
        {

        }
        */ 
        
        private void dpad_left_Tap(object sender, GestureEventArgs e)
        {
            JSInterface.updateSelectedLocation(webBrowserOffline, -Map2DOffline.RELATIVE_PIXEL_UPDATE_INTERVAL, 0);        			
        }

        private void dpad_right_Tap(object sender, GestureEventArgs e)
        {
            JSInterface.updateSelectedLocation(webBrowserOffline, Map2DOffline.RELATIVE_PIXEL_UPDATE_INTERVAL, 0);
        			
        }

        private void dpad_up_Tap(object sender, GestureEventArgs e)
        {
            JSInterface.updateSelectedLocation(webBrowserOffline, 0, -Map2DOffline.RELATIVE_PIXEL_UPDATE_INTERVAL);
        }

        private void dpad_down_Tap(object sender, GestureEventArgs e)
        {
            JSInterface.updateSelectedLocation(webBrowserOffline, 0, Map2DOffline.RELATIVE_PIXEL_UPDATE_INTERVAL);                        
        }
                 
        private void LayoutRoot_Tap(object sender, GestureEventArgs e)
        {
            Point p = e.GetPosition(null);
            MessageBox.Show(string.Format("X = {0}, Y = {1}", p.X, p.Y));
        }

        private void LayoutRoot_MouseLeftButtonUp(object sender, MouseButtonEventArgs e)
        {
            /*
            Point p = curEdge.GetPosition(null);
            MessageBox.Show(string.Format("X = {0}, Y = {1}", p.X, p.Y));
             * */
        }

        /*
        private void ApplyColorFilter(BitmapImage bitmapImage, double r, double g, double b)
        {
            byte A, R, G, B;
            Color pixelColor;

            for (int y = 0; y < bitmapImage.Height; y++)
            {
                for (int x = 0; x < bitmapImage.Width; x++)
                {
                    pixelColor = bitmapImage.GetPixel(x, y);
                    A = pixelColor.A;
                    R = (byte)(pixelColor.R * r);
                    G = (byte)(pixelColor.G * g);
                    B = (byte)(pixelColor.B * b);
                    bitmapImage.SetPixel(x, y, Color.FromArgb((int)A, (int)R, (int)G, (int)B));
                }
            }
        }  
        **/
    }
}