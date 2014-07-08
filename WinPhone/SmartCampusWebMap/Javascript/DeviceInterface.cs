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


namespace SmartCampusWebMap.Javascript
{
    //REQUIRES SCREENS TO BE UP AND RUNNING
    /**
     * This classes exposes methods that can be called from javascript. 
     * More specifically, this class is used to call appropriate Android logic
     * whenever the user clicks on a marker or on the map of the webview.  
     * 
     * The events are simply forwarded to the appropriate target (online, offline, add/removeEdge)
     * 
     * @author admin
     *
     */
    public static class DeviceInterface
    {       

        public static WebBrowser Target { get; set; }
        
        /**
         * This method is introduced to allow javascript functions to show 
         * an alert dialog (it is not possible to call javascript's alert)		
         * @param msg
         */
        public static void debugAlert(String msg)
        {
            MessageBox.Show(msg);
        }

        /**
         * This method is called (from javascript) whenever the user taps a marker on the map.
         * In the online phase we want to show directions to the location, the url of the location and what-not. 
         * In the offline phase we want to either edit the location or attach a new measurement. 
         * @param isOnline Indicates whether we are in the online phase
         * @param floorNum The current floor 
         * @param vertexId The id of the selected (tapped) vertex.
         */
        public static void onTap(bool isOnline, int floorNum, int vertexId)
        {
            //TODO: ADD CODE
            //mTarget.onTap(floorNum, vertexId);
        }

        /**
         * This method is called (from javascript) whenever the user taps on the map - not a marker. 
         * @param isOnline Indicates whether we are in the online phase. We only handle this call in the offline phase as the tap determines
         * the location of a new bound location.
         * @param floor the current floor
         * @param lat the latitude of the tapped location
         * @param lon the longitude of the tapped location
         */
        public static void setSelectedLocation(bool isOnline, int floor, double lat, double lon)
        {
            //TODO: ADD CODE
            //mTarget.setSelectedLocation(isOnline, floor, lat, lon);
        }

        /**
         * This method is called (from javascript) when the user wants to delete an edge. 
         * (This is done by clicking on the edge
         * @param originId
         * @param destinationId
         */
        public static void removeLink(int originId, int destinationId)
        {
            /*
		    if (!(mTarget instanceof WebMap2DAddEdge))
			    return;
		
		    ((WebMap2DAddEdge)mTarget).createRemoveLinkConfirmationDialog(
				    originId, destinationId).show();
             */
        }

        /**
         * This method is called (from Javascript) when tiles are loaded,
         * so we call our own setMapReady which centers at the building and updates overlays.
         */
        public static void setMapReady()
        {
            //mTarget.setMapReady();
        }
    }
}
