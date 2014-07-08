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
using SmartCampusWebMap.RadiomapBackend;
using System.Collections.Generic;
using Microsoft.Phone.Controls;
using System.Linq;
using System.Text;
using SmartCampusWebMap.Ui.Maps;
using SmartCampusWebMap.Javascript;
using SmartCampusWebMap.Library.Location;

namespace SmartCampusWebMap.Ui.Maps.Offline
{
	public class Map2DOffline : Map2D
	{
        #region Event to notify the UI about the new selected location
        public class SelectedLocationArgs : EventArgs
        {
            public double Latitude { get; private set; }
            public double Longitude { get; private set; }
            public SelectedLocationArgs(double lat, double lon)
            {
                this.Latitude = lat;
                this.Longitude = lon;
            }
        }
        public event EventHandler<SelectedLocationArgs> OnSelectedLocationChange;
        protected void OnSelectedLocationChanged(object sender, SelectedLocationArgs args)
        {
            if (OnSelectedLocationChange != null)
            {
                OnSelectedLocationChange(sender, args);
            }
        }
        #endregion
 
        #region Event to notify the UI that onTap has been called. UI must then open the Edit Location/Add Measurement 'dialog'
        //we don't have any data to send
        public event EventHandler OnTapChange;
        #endregion


		//Keeps track of if graph has changed
		//Only redraw if graph has changed
		//private bool HasGraphChanged { get; set; }
		public const int RELATIVE_PIXEL_UPDATE_INTERVAL = 3;
		
		//Callback for when the graph has changed (symbolic location added/updated or new measurement added)
		//Corresponds to two Android receivers: SymbolicLocationUploadedReceiver and WifiMeasurementUploadedReceiver
		private void HasGraphChanged(object sender, EventArgs e)
		{
			refreshUI();
		}
				
		//Represents the selected 'vertex' - as determined by map/overlay onTap events.
		//If the selected vertex is 'unbound' (i.curEdge., not part of the graph) it will have ID = -1
        public static Vertex SelectedOfflineVertex { get; private set; }
        //ID for unbound locations
        public static readonly int UNBOUND_ID = -1;

		//Indicates whether the selected offline vertex is bound or not
		private bool isBoundLocation;

		//In offline mode we manually select the current floor
		public override int getCurrentFloor()
		{
			return mCurrentSelectedFloor;
		}
		
		//indicates whether the current selection is a bound location
		public bool isCurrentLocationBound()
		{
			return isBoundLocation;
		}

        public Map2DOffline()
            : base()
        {
            Initialize();
        }

		public Map2DOffline(WebBrowser browser)
			: base(browser)
		{
            Initialize();
		}

        private void Initialize()
        {
            //Update the map when changes occur. 
            LocationService.OnBuildingDownload += new EventHandler(LocationService_OnBuildingDownload);
        }        

		/**
		 * This method is called (from javascript) whenever the user taps a marker on the map. 
		 * @param floorNum The current floor 
		 * @param vertexId The id of the selected (tapped) vertex.
		 */
		public override void onTap(int floorNum, int vertexId)
		{
			//HACK: Use graph instead
            onTap(LocationService.CurrentBuilding.Vertices.FirstOrDefault(v1 => v1.ID == vertexId));	
		}

        public void onTap(Vertex v)
        {
            if (v == null)
                return;

            //NOTE: ID and (lat, lon) is for debugging (tracking down faulty rounded coordinates
            AbsoluteLocation absLoc = v.AbsoluteLocations[0];
            StringBuilder sb = new StringBuilder();
            sb.Append("ID: ").Append(v.ID);
            sb.Append(" (").Append(Math.Round((double)absLoc.latitude, 5)).Append(", ");
            sb.Append(Math.Round((double)absLoc.longitude, 5)).Append(", ");
            sb.Append((int)absLoc.altitude).Append(")");
            String title = sb.ToString();

            this.updateSelectedVertex(v, title, true);

            //navigate
        }

		/**
		 * This method is called (from javascript) whenever the user taps on the map - not a marker. 
		 * This captures the lat, lon coordinates of the selected location  
		 * This also means that an unbound location has been selected, i.curEdge., selectedVertex will have id = -1.
		 * @param isOnline Indicates whether we are in the online phase (false)
		 * @param floor the current floor
		 * @param lat the latitude of the tapped location
		 * @param lon the longitude of the tapped location
		 */
		public void setSelectedLocation(bool isOnline, int floor, double lat, double lon)
		{
			Vertex curVertex = new Vertex();
			curVertex.ID = -1;
            curVertex.Building_ID = LocationService.CurrentBuilding.ID;
			AbsoluteLocation absLoc = new AbsoluteLocation();
			absLoc.latitude = Convert.ToDecimal(lat);
			absLoc.longitude = Convert.ToDecimal(lon);
			absLoc.altitude = floor;
			curVertex.AbsoluteLocations.Add(absLoc);

			StringBuilder title = new StringBuilder();
			title.Append("Unbound: ").Append(floor).Append(";").Append(Math.Round(lat, 5)).Append("; ").Append(Math.Round(lon, 5)).Append("; floor ").Append(floor);

			updateSelectedVertex(curVertex, title.ToString(), false);
            UI_ShowSelectedLocation(lat, lon);
		}

        /// <summary>
        /// Notify the UI to show the selected location
        /// (i.e., center at the selected coordinates)
        /// </summary>
        /// <param name="lat"></param>
        /// <param name="lon"></param>
        private void UI_ShowSelectedLocation(double lat, double lon)
        {
            if (uiMode == UiMode.GOOGLE_MAPS)
            {
                JSInterface.showSelectedLocation(browser, lat, lon);
            }
            else if (uiMode == UiMode.BING_MAPS)
            {
                OnSelectedLocationChanged(this, new SelectedLocationArgs(lat, lon));
            }
        }

		public override void ScriptNotify(object sender, NotifyEventArgs e)
		{
			base.ScriptNotify(sender, e);

			string[] methodParts = e.Value.Split('|');
			//MessageBox.Show(curEdge.Value);
			//window.external.Notify("setSelectedLocation|" + online + "|" + floor + "|" + location.lat + "|" + location.lng);
			if (methodParts[0].StartsWith("setSelectedLocation"))
			{
				int floor = int.Parse(methodParts[2]);
				double lat = double.Parse(methodParts[3]);
				double lon = double.Parse(methodParts[4]);				

				StringBuilder title = new StringBuilder();
				title.Append("Unbound: ").Append(floor).Append(";").Append(Math.Round(lat, 5)).Append("; ").Append(Math.Round(lon, 5));

                setSelectedLocation(false, floor, lat, lon);
				//updateSelectedVertex(null, title.ToString(), false);
				UI_ShowSelectedLocation(lat, lon);
			}
            else if (methodParts[0].StartsWith("onTap"))
            {
                //"onTap|" + online + "|" + floor + "|" + vertexItem.id
                int id = int.Parse(methodParts[3]);
                //HACK: Use dictionary graph to get vertex by id
                Building b = LocationService.CurrentBuilding;
                if (b == null)
                    return;

                Vertex v = b.Vertices.FirstOrDefault(v1 => v1.ID == id);
                AbsoluteLocation absLoc = v.AbsoluteLocations.First();
                StringBuilder sb = new StringBuilder();
                sb.Append("ID: ").Append(v.ID);
                sb.Append(" (").Append(Math.Round((double)absLoc.latitude, 5)).Append(", ");
                sb.Append(Math.Round((double)absLoc.longitude, 5)).Append(", ");
                sb.Append((int)absLoc.altitude).Append(")");
                String title = sb.ToString();

                this.updateSelectedVertex(v, title, true);
                //notify UI that onTap has occured
                if (OnTapChange != null)
                    OnTapChange(null, null);                
            }
		}

		/// <summary>
		/// Updates the currently selected vertex
		/// If it is an unbound location, then the selected vertex will have ID = -1
		/// else the selected vertex corresponds to a chosen vertex in the graph
		/// Do null pass null. 
		/// <param name="selectedVertex"></param>
		/// <param name="title"></param>
		/// <param name="isBound"></param>
		public void updateSelectedVertex(Vertex selectedVertex, string title, bool isBound)
		{
			if (selectedVertex == null)
				throw new ArgumentException("Selected Vertex must not be null. ");

			Map2DOffline.SelectedOfflineVertex = selectedVertex;
			this.isBoundLocation = isBound;
            
            //Tell UI to update title
			OnTitleChanged(new TitleArgs(title));
		}

        /// <summary>
        /// XXX
        /// Called when an attempt has been made to download a building
        /// (This is akin to ServiceConnected)
        /// </summary>
        /// <param name="sender">Always null</param>
        /// <param name="curEdge">Always null</param>
        void LocationService_OnBuildingDownload(object sender, EventArgs e)
        {
            Building b = LocationService.CurrentBuilding;
            if (b != null)
            {
                UI_UpdateMap();
            }
        }
	}
}
