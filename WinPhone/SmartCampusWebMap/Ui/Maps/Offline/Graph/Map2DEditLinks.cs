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
using SmartCampusWebMap.Library.Location;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Phone.Controls;

namespace SmartCampusWebMap.Ui.Maps.Offline.Graph
{
    public class Map2DEditLinks : Map2D
    {        
        private List<Vertex> mEndPoints = new List<Vertex>();
        private Building _currentBuilding;

        public Map2DEditLinks()
            : base()
        {
            Initialize();
        }

        public Map2DEditLinks(WebBrowser targetBrowser)
            : base(targetBrowser)
        {
            Initialize();
        }

        private void Initialize()
        {
            _currentBuilding = LocationService.CurrentBuilding;
            //Update the map when changes occur. 
            LocationService.OnBuildingDownload += new EventHandler(LocationService_OnBuildingDownload);            
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
    
        public override int getCurrentFloor()
        {
            return mCurrentSelectedFloor;
        }                        

        public override void onTap(int floorNum, int vertexId)
        {
            //MOved 

            //HACK: Use graph instead
            //onTap(LocationService.CurrentBuilding.Vertices.FirstOrDefault(v1 => v1.ID == vertexId));
        }
    }
}
