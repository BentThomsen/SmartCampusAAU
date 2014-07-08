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
using System.Threading;
using System.Windows.Controls.Primitives;
using Microsoft.Phone.Controls;

namespace SmartCampusWebMap.Ui
{
    public class Globals
    {
        #region Relative URIs for screens and icons
        public const string IconUri_AddLink                 = "/Images/drawable-ldpi/ic_menu_add_link.png";
        public const string IconUri_ChangeFloor             = "/Images/drawable-ldpi/ic_menu_change_floor.png";
        public const string IconUri_EditGraph               = "/Images/drawable-ldpi/ic_menu_edit_graph.png";
        public const string IconUri_MyLocation              = "/Images/drawable/aqua-sphere-blue.png";
        public const string IconUri_Measure                 = "/Images/drawable-ldpi/ic_menu_measure.png";
        public const string IconUri_NearbyPOI               = "/Images/drawable-ldpi/ic_menu_nearby.png";
        public const string IconUri_ProviderGps             = "/Images/drawable-ldpi/ic_menu_provider_gps.png";
        public const string IconUri_ProviderNone            = "/Images/drawable-ldpi/ic_menu_provider_none.png";
        public const string IconUri_ProviderWifi            = "/Images/drawable-ldpi/ic_menu_provider_wifi.png";
        public const string IconUri_RemoveLink              = "/Images/drawable-ldpi/ic_menu_remove_link.png";
        
        public const string IconUri_Route                   = "/Images/drawable-ldpi/ic_menu_clear_route.png";
        public const string IconUri_Save                    = "/Images/standard/dark/appbar.save.rest.png";
        public const string IconUri_SelectedLocation        = "/Images/drawable/sniper48.png";
        public const string IconUri_StartMeasure            = "/Images/standard/dark/appbar.transport.play.rest.png";
        public const string IconUri_StopMeasure             = "/Images/standard/dark/appbar.transport.stop.rest.png";
        public const string IconUri_TrackingOn              = "/Images/drawable-hdpi/foliapin.png";
        public const string IconUri_TrackingOff             = "/Images/drawable-hdpi/tracking_off.png";        
                
        public const string XamlUri_About                   = "/UI/About/HowItWorks.xaml";
        public const string XamlUri_About_Old               = "/UI/About/About.xaml";
        public const string XamlUri_EditGraph               = "/Ui/Offline/Graph/EditGraph.xaml";
        public const string XamlUri_EditLinks               = "/Ui/Maps/Offline/Graph/UiBing_Map2DEditLinks.xaml";
        public const string XamlUri_EditSymbolicLocation    = "/UI/Offline/Graph/EditSymbolicLocation.xaml";
        public const string XamlUri_OfflineOnTapAction      = "/Ui/Dialogs/OfflineOnTapAction.xaml";
        public const string XamlUri_OfflineBingMap          = "/Ui/Maps/Offline/UI_OfflineBingMap.xaml";
        public const string XamlUri_OfflineGoogleMap        = "/Ui/Maps/Offline/UI_OfflineGoogleMap.xaml";
        public const string XamlUri_OnlineBingMap           = "/UI/Maps/Online/UI_OnlineBingMap.xaml";
        public const string XamlUri_OnlineGoogleMap         = "/UI/Maps/Online/UI_OnlineGoogleMap.xaml";
        public const string XamlUri_SaveMacAddress          = "/Ui/Dialogs/SaveMacAddress.xaml";
        public const string XamlUri_SelectEdgeType          = "/Ui/Dialogs/SelectEdgeType.xaml";
        public const string XamlUri_SelectProvider          = "/Ui/Dialogs/SelectProvider.xaml";
        public const string XamlUri_SelectBuildingFloor     = "/Ui/Dialogs/SelectBuildingFloor.xaml";
        public const string XamlUri_SetTracking             = "/Ui/Dialogs/SetTracking.xaml";
        public const string XamlUri_ShowNearbyPoi           = "/Ui/Dialogs/ShowNearbyPoi.xaml";
        public const string XamlUri_WifiScanForm            = "/UI/Offline/Wifi/WifiScanForm.xaml";
        #endregion

        #region Reusable popup dialog
        /// <summary>
        /// Used for showing a popup dialog with the specified text for the specified duration in milliseconds.
        /// </summary>
        /// <param name="source"></param>
        /// <param name="text"></param>
        /// <param name="duration"></param>
        public static void ShowDialog(PhoneApplicationPage source, string text, int duration)
        {
            Popup p = new Popup();

            //PhoneApplicationPage
            //Template: http://msdn.microsoft.com/en-us/library/system.windows.controls.primitives.popup%28v=vs.95%29.aspx

            // Create some content to show in the popup. Typically you would 
            // create a user control.
            Border border = new Border();
            border.BorderBrush = new SolidColorBrush(Colors.Black);
            border.BorderThickness = new Thickness(5.0);

            StackPanel panel1 = new StackPanel();
            panel1.Background = new SolidColorBrush(Colors.LightGray);

            TextBlock textblock1 = new TextBlock();

            textblock1.Text = text;
            textblock1.FontSize = 25;
            textblock1.Margin = new Thickness(5.0);
            panel1.Children.Add(textblock1);
            border.Child = panel1;

            // Set the Child property of Popup to the border 
            // which contains a stackpanel, textblock and button.
            p.Child = border;

            // Set where the popup will show up on the screen.
            p.VerticalOffset = 25;
            p.HorizontalOffset = 25;
            p.IsOpen = true;
            // Open the popup.

            new Thread((ThreadStart)delegate
            {
                Thread.Sleep(duration);
                source.Dispatcher.BeginInvoke(() => { p.IsOpen = false; });
            }).Start();
        }

        //Template duration and text for showing a popup dialog (cf. ShowDialog(..))
        public const int DURATION_SHORT = 2000;
        public const string CHANGES_SAVED = "Changes saved. Thank you!";
        #endregion

        public static readonly string AppName = "SmartCampusAAU";
    }
}
