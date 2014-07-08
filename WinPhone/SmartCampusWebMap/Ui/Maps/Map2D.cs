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
using Microsoft.Phone.Controls;
using SmartCampusWebMap.Javascript;
using System.Collections.Generic;
using System.Linq;
using SmartCampusWebMap.Library.ModelExtensions.Indoormodel;
using SmartCampusWebMap.Library.Location;

namespace SmartCampusWebMap.Ui.Maps
{
    public abstract class Map2D
    {        
        //The UI can either be Bing maps or Google Maps (experienced exceptions when calling javascript on device - no problems, however, in emulator)
        //If we are using Google Maps, then we are calling javascript method in a browser
        //If we are using Bing Maps, we are just raising events which the Bing Map component responds to.
        protected enum UiMode { BING_MAPS, GOOGLE_MAPS }
        protected UiMode uiMode;

        #region We use events to notify the UI

        /// <summary>
        /// Notifies the UI that the title has changed (different building, floor or location) 
        /// </summary>
        public event EventHandler<TitleArgs> OnTitleChange;
        /// <summary>
        /// Encapsulates a new title
        /// </summary>
        public class TitleArgs : EventArgs
        {
            public TitleArgs(string title) { this.Title = title; }
            public string Title { get; private set; }
        }
        protected virtual void OnTitleChanged(TitleArgs e)
        {
            if (OnTitleChange != null) //any observers?
                OnTitleChange(this, e);
        }
        #endregion

        //The browser UI that is used when gmap is supported
        protected WebBrowser browser;

        #region Bing map events. These events (rather than javascript calls) are invoked when using Bing maps.
        #region Notify the UI to center the map at the specified coordinates.
        public class CoordinateArgs : EventArgs
        {
            public double Latitude { get; private set; }
            public double Longitude { get; private set; }
            public CoordinateArgs(double lat, double lon)
            {
                this.Latitude = lat;
                this.Longitude = lon;
            }
        }
        public event EventHandler<CoordinateArgs> OnCenterChange;
        protected void OnCenterChanged(object sender, CoordinateArgs args)
        {
            if (OnCenterChange != null)
                OnCenterChange(sender, args);
        }
        #endregion

        #region Notify the UI to show the specified vertices
        public class VisibleVerticesArgs : EventArgs
        {
            public VisibleVerticesArgs(IEnumerable<Vertex> vertices)
            {
                this.VisibleVertices = vertices;
            }
            public IEnumerable<Vertex> VisibleVertices { get; private set; }
        }
        public event EventHandler<VisibleVerticesArgs> OnVisibleVerticesChange;
        protected void OnVisibleVerticesChanged(object sender, VisibleVerticesArgs args)
        {
            if (OnVisibleVerticesChange != null)
                OnVisibleVerticesChange(sender, args);
        }
        #endregion

        #region Notify the UI to show the specified edges
        public class VisibleEdgesArgs : EventArgs
        {
            public IEnumerable<Edge> VisibleEdges { get; private set; }
            public VisibleEdgesArgs(IEnumerable<Edge> edges)
            {
                this.VisibleEdges = edges;
            }
        }
        public event EventHandler<VisibleEdgesArgs> OnVisibleEdgesChange;
        protected void OnVisibleEdgesChanged(object sender, VisibleEdgesArgs args)
        {
            if (OnVisibleEdgesChange != null)
                OnVisibleEdgesChange(sender, args);
        }
        #endregion

        public event EventHandler OnClearOverlayChange;
        protected void OnClearOverlayChanged(object sender, EventArgs args)
        {
            if (OnClearOverlayChange != null)
                OnClearOverlayChange(sender, args);
        }
        #endregion

        public static int mCurrentSelectedFloor { get; set; }

        /// <summary>
        /// If this constructor is called it indicates that we want to use Bing Maps as UI
        /// </summary>
        public Map2D()
        {
            uiMode = UiMode.BING_MAPS;
        }  

        /// <summary>
        /// If this browser is called it indicates that we want to use Google maps with the supplied browser
        /// </summary>
        /// <param name="targetBrowser"></param>
        public Map2D(WebBrowser targetBrowser)
        {
            uiMode = UiMode.GOOGLE_MAPS;

            this.browser = targetBrowser;
            setupBrowser();
            UI_UpdateMap(); //loads any tiles
        }              

        protected void loadTiles()
        {
            //Avoid caching
            string noCache = "?t=" + DateTime.Now.ToLongTimeString();
                        
            //NOTE: We are currently using ifc_url to hold tile urls (a hack that became permanent)
            Building b = LocationService.CurrentBuilding;
            if (b != null && b.Ifc_Url != null && b.Ifc_Url != "null")
            {
                browser.Navigate(new Uri(b.Ifc_Url + noCache));
            }
            else
            {
                browser.Navigate(new Uri(LocationService.DEFAULT_TILE_URL + noCache));
            }
        }

        private void setupBrowser()
        {
            browser.IsScriptEnabled = true;
            browser.ScriptNotify += new EventHandler<NotifyEventArgs>(ScriptNotify);
            browser.LoadCompleted += new System.Windows.Navigation.LoadCompletedEventHandler(browser_LoadCompleted);
        }

        void browser_LoadCompleted(object sender, System.Windows.Navigation.NavigationEventArgs e)
        {
            /*
            pageLoaded = true;
            if (requestHandleMapReady)
            {
                requestHandleMapReady = false;
                handleMapReady();
            }
             */
        }

        //Here, all general messages go
        public virtual void ScriptNotify(object sender, NotifyEventArgs e)
        {
            string msg = e.Value;
            string[] msgParts = msg.Split('|');
            string method = msgParts[0];
            if (JavascriptParser.isMapReady(method))
            {
                handleMapReady();
            }            
        }

        //private bool requestHandleMapReady;
        //private bool pageLoaded;

        //Equivalent to MapReadyReceiver in Android
        public void handleMapReady()
        {
            Building b = LocationService.CurrentBuilding;
            if (b != null) 
	    	{
                UI_CenterAt((double)b.Lat.Value, (double)b.Lon.Value);
	    	}
	    	this.refreshUI();
        }

        /// <summary>
        /// This is used to center the map (Bing map or Google Map) at the specified coordinates. 
        /// NOTE: Can be used with either Google Maps or Bing Maps
        /// </summary>
        /// <param name="lat"></param>
        /// <param name="lon"></param>
        public void UI_CenterAt(double lat, double lon)
        {
            if (uiMode == UiMode.GOOGLE_MAPS)
            {
                JSInterface.centerAt(this.browser, lat, lon);
            }
            else if (uiMode == UiMode.BING_MAPS)
            {
                OnCenterChanged(this, new CoordinateArgs(lat, lon));
            }
        }
               
        /// <summary>
        /// Clear map overlay
        /// NOTE: Can be used with either Bing maps or Google Maps
        /// </summary> 
        protected void UI_ClearOverlays()
        {
            if (uiMode == UiMode.GOOGLE_MAPS)
            {
                JSInterface.clearOverlays(browser);
            }
            else if (uiMode == UiMode.BING_MAPS)
            {
                OnClearOverlayChanged(this, null);
            }
        }

        //This method is called to show edges (IFF edges should be shown)
        protected void UI_ShowEdges(int floorNum)
        {
            IEnumerable<Edge> edges = null;
            if (LocationService.CurrentBuilding != null && showEdges())
            {
                edges = getVisibleEdges(floorNum); //mGraph.getEdges(floorNum);

                if (uiMode == UiMode.GOOGLE_MAPS)
                {
                    JSInterface.showEdges(browser, edges, floorNum);
                }
                else if (uiMode == UiMode.BING_MAPS)
                {
                    OnVisibleEdgesChanged(this, new VisibleEdgesArgs(edges));
                }
            }            
        }

        //NOTE: Consider refactoring
        //Change name to 'ShowVertices'
        //This method is called to show the vertices
        protected void UI_ShowVertices(int floorNum)
        {
            IEnumerable<Vertex> verts = getVisibleVertices(floorNum);
            if (uiMode == UiMode.GOOGLE_MAPS)
            {
                JSInterface.showVertices(browser, verts, floorNum, isOnline());
            }
            else
            {
                OnVisibleVerticesChanged(this, new VisibleVerticesArgs(verts));
            }
        }

        /// <summary>
        /// Updates the 'background' map, curEdge.g., loads new tiles corresponding to a new building
        /// </summary>
        protected void UI_UpdateMap()
        {
            if (uiMode == UiMode.GOOGLE_MAPS)
            {
                //When the page has loaded, the javascript code calls setMapReady -> handleMapReady()
                loadTiles();
            }
            else if (uiMode == UiMode.BING_MAPS)
            {
                //We don't load any tiles when using bing maps so we handle the ui refresh straight away
                handleMapReady();
            }
        }

        public virtual int getCurrentFloor()
        {
            return mCurrentSelectedFloor;
        }

        /// <summary>
        /// Updates the title and vertices/edges that needs to be shown
        /// </summary>
        public void refreshUI()
        {
            Building b = LocationService.CurrentBuilding;
            if (b != null)
            {
                OnTitleChanged(new TitleArgs(concatBuildingAndFloorName(mCurrentSelectedFloor)));
                this.updateOverlays(mCurrentSelectedFloor);                
            }
        }

        protected void updateOverlays(int floorNum)
        {
            UI_ClearOverlays();

            //Show edges (if they should be shown):  
            UI_ShowEdges(floorNum);

            //Show vertices:
            UI_ShowVertices(floorNum);

        }

        public void changeToFloor(int newFloor)
        {
            Building b = LocationService.CurrentBuilding;
            if (b != null)
            {
                mCurrentSelectedFloor = newFloor;
                refreshUI();
            }
        }
        
        //The default behavior is to show all the graph's edges. 
        //However, in the online phase, we only want to show a route. 
        protected IEnumerable<Edge> getVisibleEdges(int floorNum)
        {
            //throw new NotImplementedException();
            //HACK
            Building b = LocationService.CurrentBuilding;
            //TODO: We are currently showing everything. We need the graph
            if (b != null)
                return b.getEdges(floorNum);
            else
                return null;             
        }
            
        /// <summary>
        /// The default behavior is to show everything. 
        /// However, in the online phase we only want to show routes. 
        /// </summary>
        /// <param name="floorNum"></param>
        /// <returns></returns>
        protected virtual IEnumerable<Vertex> getVisibleVertices(int floorNum)
        {
            Building b = LocationService.CurrentBuilding;
            if (b == null || b.Vertices == null)
                return null;

            return b.Vertices.Where(v => (int)(v.AbsoluteLocations.First().altitude) == floorNum);
        }
               
        protected virtual bool isOnline()
        {
            return false;
        }

        /**
	     * This method is called (from javascript) whenever the user taps a marker on the map. 
	     * @param floorNum The current floor 
	     * @param vertexId The id of the selected (tapped) vertex.
	     */
        public abstract void onTap(int floorNum, int vertexId); 	

        /**
         * Indicates whether we desire to show the edges of the graph.
         * @return The default value is true
         */
        protected virtual bool showEdges()
        {
            return true;
        }

        //Concatenates building name and floor name, to give an overall indication of where a user is.
        protected String concatBuildingAndFloorName(int floorNum)
        {
            Building b = LocationService.CurrentBuilding;
            if (b == null)
                return " Unknown building ";

            String building_name = b.Building_Name;
            if (building_name == null)
                return "Unknown Building";
            else
            {
                string floor_name = "Unknown floor";
                foreach (Building_Floors floor in b.Building_Floors)
                {
                    if (floor.Number == floorNum)
                    {
                        floor_name = floor.Name ?? string.Format(" floor #{0}", floor.Number);
                        break;
                    }
                }
                
                return building_name + " - " + floor_name;
            }
        }
    }    
}
