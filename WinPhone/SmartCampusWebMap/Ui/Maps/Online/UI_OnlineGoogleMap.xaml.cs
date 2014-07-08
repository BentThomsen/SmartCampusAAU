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
using SmartCampusWebMap.Javascript;
using System.Windows.Navigation;
using Microsoft.Xna.Framework.Input;

using SmartCampusWebMap.RadiomapBackend;
using SmartCampusWebMap.Ui.Dialogs;
using SmartCampusWebMap.Library.Location;
using System.Windows.Media.Imaging;
using System.Text;


namespace SmartCampusWebMap.Ui
{
    public partial class UI_OnlineGoogleMap : PhoneApplicationPage
    {
        // Constructor
        public UI_OnlineGoogleMap()
        {
            InitializeComponent();
            InitializeTrackingImages();
            //NOTE: Redundans omkring application bar buttons og handlinger, f.eks. changeFloor
            InitializeMenu();

            mWebMapCommon = new Map2DOnline(webBrowserOnline);
            mWebMapCommon.OnTitleChange += (source, titleArgs) => this.ApplicationTitle.Text = titleArgs.Title;
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
                        menuProvider.IconUri = new Uri(Globals.IconUri_ProviderWifi, UriKind.Relative);
                        break;
                    case Map2DOnline.ProviderStatus.USE_NONE:
                        menuProvider.IconUri = new Uri(Globals.IconUri_ProviderNone, UriKind.Relative);
                        break;
                }
            };
        }   

        private static BitmapImage imgTrackingOn, imgTrackingOff;

        private void InitializeTrackingImages()
        {
            string resourcePrefix = "/SmartCampusWebMap;component";
            imgTrackingOn = new BitmapImage(new Uri(resourcePrefix + Globals.IconUri_TrackingOn, UriKind.RelativeOrAbsolute));
            imgTrackingOff = new BitmapImage(new Uri(resourcePrefix + Globals.IconUri_TrackingOff, UriKind.RelativeOrAbsolute));
        }

        private Map2DOnline mWebMapCommon;

           

        //Application bar buttons
        private ApplicationBarIconButton menuProvider, menuFloorChanger, menuNearbyPOI, menuRoute;
        //Application bar menu items
        ApplicationBarMenuItem menuAbout, menuOffline; /* menuFindNearest, menuSearch, */
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

            /*
            //The remaining menu items
            menuFindNearest = new ApplicationBarMenuItem();
            menuFindNearest.Text = "Find Nearest...";
            //pic = ic_menu_find_nearest
            ApplicationBar.MenuItems.Add(menuFindNearest);
            menuFindNearest.Click += new EventHandler(menuFindNearest_Click);

            menuSearch = new ApplicationBarMenuItem();
            menuSearch.Text = "Search";
            //pic = ic_menu_search
            ApplicationBar.MenuItems.Add(menuSearch);
            menuSearch.Click += new EventHandler(menuSearch_Click);
            */

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
            
            /*
            <item 
    	        android:icon="@drawable/ic_menu_change_view" 
    	        android:title="Change View" 
    	        android:id="@+id/change_view_online">
   	        </item>   	
   	
   	         -->
   	        <item 
    	        android:title="Change Update Intvl." 
    	        android:id="@+id/change_update_interval_online">
   	        </item>
   	        <item 
    	        android:title="Change Update Threshold" 
    	        android:id="@+id/change_update_threshold_online">
   	        </item>   	
   	        * */
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
        }

        void SelectProvider_OnProviderChange(object sender, SelectProvider.ProviderEventArgs e)
        {
            //mWebMapCommon.setProvider(e.ProviderStatus); //update Map2DOnline

            //update menu icon
            switch (mWebMapCommon.mProviderStatus)
            {
                case Map2DOnline.ProviderStatus.USE_GPS:
                    menuProvider.IconUri = new Uri(Globals.IconUri_ProviderGps, UriKind.Relative);
                    break;
                case Map2DOnline.ProviderStatus.USE_WIFI:
                    menuProvider.IconUri = new Uri(Globals.IconUri_ProviderWifi, UriKind.Relative);
                    break;
                case Map2DOnline.ProviderStatus.USE_NONE:
                    menuProvider.IconUri = new Uri(Globals.IconUri_ProviderNone, UriKind.Relative);
                    break;
            }
            
        }

        private void menuFloorChanger_Click(object sender, EventArgs e)
        {
            Building b = LocationService.CurrentBuilding;
            if (b == null)
                MessageBox.Show("Unknown Building");
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
            Vertex poi = e.SelectedPoi;
            AbsoluteLocation absLoc = poi.AbsoluteLocations.First();
            mWebMapCommon.UI_CenterAt((double)absLoc.latitude, (double)absLoc.longitude);
            //JSInterface.centerAt(webBrowserOnline, (double)absLoc.latitude, (double)absLoc.longitude);            
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
            NavigationService.Navigate(new Uri(Globals.XamlUri_OnlineBingMap, UriKind.Relative));
            //NavigationService.Navigate(new Uri(Globals.XamlUri_About, UriKind.Relative));
        }

        private void menuOffline_Click(object sender, EventArgs e)
        {
            this.NavigationService.Navigate(new Uri(Globals.XamlUri_OfflineGoogleMap, UriKind.Relative)); 
        }        
        
        private void trackBtn_Click(object sender, RoutedEventArgs e)
        {
            mWebMapCommon.IsTrackingPosition = !mWebMapCommon.IsTrackingPosition;
        }
                       
        private void trackImg_Tap(object sender, GestureEventArgs e)
        {
            mWebMapCommon.IsTrackingPosition = !mWebMapCommon.IsTrackingPosition;
        }             
    }
}