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
using System.Text;
using Microsoft.Phone.Controls;
using SmartCampusWebMap.RadiomapBackend;
//extension metoder
using SmartCampusWebMap.Library.ModelExtensions.Indoormodel.Graph;

namespace SmartCampusWebMap.Javascript
{
	public static class JSInterface
	{
		//OK-I GUESS
		public static void centerAt(WebBrowser target, double lat, double lon)
		{
			if (target == null)
				return;
            //string[] args = new string[] { lat.ToString(), lon.ToString() };
            //target.InvokeScript("centerAt", args);

            try
            {
                target.InvokeScript("eval", "centerAt(" + lat + ", " + lon + ")");
            }
            catch (SystemException ex)
            {
                System.Windows.MessageBox.Show(string.Format("{0} \n {1}", ex.Message, ex.StackTrace));
            }
		}

		//OK-I GUESS
		/**
		 * This method is used (or rather CAN be used) for clearing the overlays (markers) on the map
		 * @param target
		 */
		public static void clearOverlays(WebBrowser target)
		{
			if (target == null)
				return;

            //target.InvokeScript("UI_ClearOverlays");
			target.InvokeScript("eval", "clearOverlays()");
			//target.loadUrl("javascript:UI_ClearOverlays()");
		}

		//OK
		private static String createFoliaJsonEdge(Edge e)
		{
            Vertex origin = e.Vertices[0];
            Vertex destination = e.Vertices[1];
			AbsoluteLocation absLoc = origin.AbsoluteLocations[0];
			StringBuilder sb = new StringBuilder();
			sb.Append("{");

			sb.Append("endpoint1: { ");
			sb.Append("   id: ").Append(origin.ID);
			sb.Append(", lat: ").Append(absLoc.latitude);
			sb.Append(", lon: ").Append(absLoc.longitude);
			sb.Append(" }, ");
			absLoc = destination.AbsoluteLocations[0];
			sb.Append("endpoint2: { ");
			sb.Append("   id: ").Append(destination.ID);
			sb.Append(", lat: ").Append(absLoc.latitude);
			sb.Append(", lon: ").Append(absLoc.longitude);
			sb.Append(" } ");

			sb.Append("}");

			return sb.ToString();
		}

		private static String createFoliaJsonLocation(Vertex v)
		{
			if (v == null)
				return "null";

			AbsoluteLocation absLoc = v.AbsoluteLocations[0];
			SymbolicLocation symLoc = null; 
			foreach (SymbolicLocation s in v.SymbolicLocations)
			{
				symLoc = s;
				break;
			}
			
			//[
			// {id: , latitude: , longitude: , altitude: , title: , description: , url: , location_type: }
			// ]

			StringBuilder sb = new StringBuilder();
			sb.Append("{");
			sb.Append("id: ").Append(v.ID);
			sb.Append(", latitude: ").Append(absLoc.latitude);
			sb.Append(", longitude: ").Append(absLoc.longitude);
			sb.Append(", altitude: ").Append(absLoc.altitude);
			String title = symLoc != null ? symLoc.title : "null";
			sb.Append(", title: ").Append("'").Append(title).Append("'");
			String description = symLoc != null ? symLoc.description : "null";
			sb.Append(", description: ").Append("'").Append(description).Append("'");
			String url = symLoc != null ? symLoc.url : "null";
			sb.Append(", url: ").Append("'").Append(url).Append("'");
			sb.Append(", location_type: ").Append(symLoc != null ? symLoc.info_type : -1);
			//ToString capitalizes boolean, so necessary to call toLower
			string isStair = v.isStairEndpoint().ToString().ToLower();
			sb.Append(", isStairEndpoint: ").Append(isStair);
			string isElevator = v.isElevatorEndpoint().ToString().ToLower();
			sb.Append(", isElevatorEndpoint: ").Append(isElevator);
			string isEntrance = (symLoc != null && (symLoc.is_entrance ?? false)).ToString().ToLower();
			sb.Append(", isEntrance: ").Append(isEntrance);
			sb.Append("}");

			return sb.ToString();
		}

		private static String createFoliaJsonLocationJava(Vertex v)
		{
			if (v == null)
				return "null";

			AbsoluteLocation absLoc = v.AbsoluteLocations[0];
			SymbolicLocation symLoc = null;
			foreach (SymbolicLocation s in v.SymbolicLocations)
			{
				symLoc = s;
				break;
			}

			StringBuilder sb = new StringBuilder();
			sb.Append("{");
			sb.Append("id: ").Append(v.ID);
			sb.Append(", latitude: ").Append(absLoc.latitude);
			sb.Append(", longitude: ").Append(absLoc.longitude);
			sb.Append(", altitude: ").Append(absLoc.altitude);
			String title = symLoc != null ? symLoc.title : "null";
			sb.Append(", title: ").Append("\"").Append(title).Append("\"");
			String description = symLoc != null ? symLoc.description : "null";
			sb.Append(", description: ").Append("\"").Append(description).Append("\"");
			String url = symLoc != null ? symLoc.url : "null";
			sb.Append(", url: ").Append("\"").Append(url).Append("\"");
			//sb.append(", location_type: ").append(symLoc != null ? symLoc.getType().toString() : "N/A");
			sb.Append(", location_type: ").Append(symLoc != null ? symLoc.info_type ?? -1 : -1);
			string isStairEndpoint = v.isStairEndpoint().ToString().ToLower();
			sb.Append(", isStairEndpoint: ").Append(isStairEndpoint);
			string isElevatorEndpoint = v.isElevatorEndpoint().ToString().ToLower();
			sb.Append(", isElevatorEndpoint: ").Append(isElevatorEndpoint);
			string isEntrance = (symLoc != null ? symLoc.is_entrance : false).ToString().ToLower();
			sb.Append(", isEntrance: ").Append(isEntrance);
			sb.Append("}");

			return sb.ToString();
		}

		//OK-I GUESS
		/**
		 * This method deselected an endpoint in the add/remove webview
		 */
		public static void removeEndpoint(WebBrowser target, int vertexId)
		{
			if (target == null)
				return;

			target.InvokeScript("eval", "removeEndpoint(" + vertexId + ")");
			//target.loadUrl("javascript:removeEndpoint(" + vertexId + ")");
		}

		//NOT TESTED
		public static void search(WebBrowser target, String query)
		{
			if (target == null)
				return;

			target.InvokeScript("eval", "search('" + query + "')");
			//target.loadUrl("javascript:search('" + query + "')");
		}

		//NOT TESTED - afhængig af UI_ShowVertices
		/**
		 * This method is called when a user selects an endpoint in the add/remove webview
		 * @param vertexId
		 */
		public static void setEndpoint(WebBrowser target, int vertexId)
		{
			if (target == null)
				return;

			target.InvokeScript("eval", "setEndpoint(" + vertexId + ")");
			//target.loadUrl("javascript:setEndpoint(" + vertexId + ")");
		}

		//NOT TESTED - har ingen knap
		/**
		 * Tells whether we are tracking the user's current position, i.curEdge., if we want the map to center around it. 
		 * @param target
		 * @param doTrack
		 */
		public static void setIsTracking(WebBrowser target, bool doTrack)
		{
			if (target == null)
				return;

			target.InvokeScript("eval", "setIsTracking(\"" + doTrack.ToString() + "\")");
			//target.loadUrl("javascript:setIsTracking(\"" + stringTrack + "\")");
		}

		//NOT TESTED - bruges vist ikke pt. 
		/**
		 * Tells what the current provider is. If the provider is none - we can remove the estimate-circle.
		 * @param target
		 * @param Status
		 */
		public static void setProviderStatus(WebBrowser target, int status)
		{
			if (target == null)
				return;

			target.InvokeScript("eval", "setProviderStatus('" + status + "')");
			//target.loadUrl("javascript:setProviderStatus('" + Status + "')");
		}

        

		//OK
		/**
		* HACK: Building is not necessary; floor number is hardcoded 
		* This method sends the edges that needs to be shown on a map.
		* NOTE: The floorNum and isOnline parameters may very well be redundant
        * To use curEdge.Vertices update all existing links and change Android/iPhone so an actual link is added.  
		*/
        public static void showEdges(WebBrowser target, IEnumerable<Edge> edges, int floorNum)
		{
			if (target == null)
				return;
            
			String result;
			if (edges == null)
				result = "[]"; //this is the default json if no edges are available
			else
			{
				StringBuilder foliaEdges = new StringBuilder();
				foliaEdges.Append("[");
				bool firstElem = true;
				foreach (Edge e in edges)
				{
					if (!firstElem)
						foliaEdges.Append(", ");
					firstElem = false;
					
                    foliaEdges.Append(JSInterface.createFoliaJsonEdge(e));
				}
				foliaEdges.Append("]");
				result = foliaEdges.ToString();

			}
			try
			{
				//Load the edges
				target.InvokeScript("eval", "showEdges(" + floorNum + ", " + result + ")");
				//target.loadUrl("javascript:showEdges(" + floorNum + ", " + result + ")");
			}
			catch (Exception ex)
			{
				string msg = ex.Message;
				msg = ex.InnerException != null ? msg + ex.InnerException.Message : msg;
				System.Windows.MessageBox.Show(msg);
			}
		}
      
		//OK
		//Show selected location (for new signal strength measurement) (with sniper icon)
		public static void showSelectedLocation(WebBrowser target, double lat, double lon)
		{
			if (target == null)
				return;

			target.InvokeScript("eval", "showSelectedLocation(" + lat + ", " + lon + ")");
			//target.loadUrl("javascript:showSelectedLocation(" + lat + ", " + lon + ")");
		}

		public static void showVertices(WebBrowser browser, IEnumerable<Vertex> vertices, int floorNum, bool isOnline)
		{
			String result;
			if (vertices == null)
				result = "[]"; //this is the default json if no vertices are available
			else
			{
				StringBuilder foliaLocations = new StringBuilder();
				foliaLocations.Append("[");
				bool firstElem = true;
				foreach (Vertex v in vertices)
				{
					if (!firstElem)
						foliaLocations.Append(", ");
					firstElem = false;

					foliaLocations.Append(createFoliaJsonLocationJava(v));
				}
				foliaLocations.Append("]");
				result = foliaLocations.ToString();

			}
			//Til debugging:
			//result = "[{id: 621, latitude: 57.012376850985, longitude: 9.9910009502316, altitude: 0.0, title: 'Aalborg University', description: 'Tag en tur', url: 'no', location_type: 8, isStairEndpoint: false, isElevatorEndpoint: false, isEntrance: false}]";
			
			try
			{
				browser.InvokeScript("eval", "addGraphOverlay(" + floorNum + "," + isOnline.ToString().ToLower() + "," + result + ")");
			}
			catch (Exception ex)
			{
				System.Windows.MessageBox.Show(ex.Message);
			}
		}

		//OK
		//Show the estimated location and a circle around it denoting the positioning accuracy
		public static void updateNewLocation(WebBrowser browser, SmartCampusWebMap.WifiSnifferPositioningService.PositionEstimate location)
		{
			//Create json location and send it to javascript
			StringBuilder jsonLoc = new StringBuilder();

            jsonLoc.Append("{");
            //jsonLoc.Append("hasAltitude: ").Append(location.ha.hasAltitude()).Append(", ");
            jsonLoc.Append("hasAccuracy: ").Append(location.HasAccuracy).Append(", ");
            jsonLoc.Append("hasBearing: ").Append(location.HasBearing).Append(", ");
            jsonLoc.Append("hasSpeed: ").Append(location.HasSpeed).Append(", ");
            jsonLoc.Append("accuracy: ").Append(location.Accuracy).Append(", ");
            jsonLoc.Append("bearing: ").Append(location.Bearing).Append(", ");
            jsonLoc.Append("speed: ").Append(location.Speed).Append(", ");
            jsonLoc.Append("latitude: ").Append(location.Latitude).Append(", ");
            jsonLoc.Append("longitude: ").Append(location.Longitude).Append(", ");
            jsonLoc.Append("altitude: ").Append(location.Altitude); //The last - no comma
            //jsonLoc.Append("time: ").Append(location.Time);
            jsonLoc.Append("}");

			browser.InvokeScript("eval", "updateNewLocation(" + jsonLoc.ToString() + ")"); //DOES work (but still exception)
		}

		//OK
		//Moves the current select location by the specified number of pixels
		public static void updateSelectedLocation(WebBrowser target, int xPixels, int yPixels)
		{
			if (target == null)
				return;

			try
			{
				target.InvokeScript("eval", "updateSelectedLocation(" + xPixels + ", " + yPixels + ")");
			}
			catch (Exception ex)
			{
				System.Windows.MessageBox.Show(ex.Message);
			}
			//target.loadUrl("javascript:updateSelectedLocation(" + xPixels + ", " + yPixels + ")");
		}
	}
}
