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
using SmartCampusWebMap.RadiomapBackend;
using SmartCampusWebMap.Ui.Maps;
using System.Data.Services.Client;
using SmartCampusWebMap.Library.Location;

namespace SmartCampusWebMap.Ui.Dialogs
{
    public partial class ShowNearbyPoi : PhoneApplicationPage
    {
        //public static Vertex SelectedVertexPOI;
        public static event EventHandler<PoiEventArgs> OnPoiSelectionChange;
        public class PoiEventArgs : EventArgs
        {
            public PoiEventArgs(Vertex v)
            {
                this.SelectedPoi = v;
            }
            public Vertex SelectedPoi { get; private set; }
        }
        protected virtual void OnPoiSelectionChanged(PoiEventArgs e)
        {
            if (OnPoiSelectionChange != null)
                OnPoiSelectionChange(this, e);
        }

        private List<Vertex> poiVerts;
        
        public ShowNearbyPoi()
        {
            InitializeComponent();
            this.ApplicationTitle.Text = Globals.AppName;
            
            Building b = LocationService.CurrentBuilding;
            if (b == null)
                this.NavigationService.GoBack(); 
            if (b.Building_Floors == null)
                this.NavigationService.GoBack();

            //A graph would make everything better. 
            poiVerts = (from v in b.Vertices 
                         where
                            v.AbsoluteLocations.First().altitude == Map2D.mCurrentSelectedFloor &&
                            v.SymbolicLocations.FirstOrDefault() != null
                        select v).ToList();

            nearbyPoiListBox.ItemsSource = poiVerts.Select(v => v.SymbolicLocations.First());
            nearbyPoiListBox.SelectionChanged += new SelectionChangedEventHandler(nearbyPoiListBox_SelectionChanged);
             
        }

        void nearbyPoiListBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            //NOTE: Det kan være en halvfarlig strategi at bero sig på at indexes i nearbyPoiList svarer overens med poiVerts!!!
            int idx = nearbyPoiListBox.SelectedIndex;
            //SelectedVertexPOI = poiVerts[idx];
            OnPoiSelectionChanged(new PoiEventArgs(poiVerts[idx]));
            NavigationService.GoBack(); 
            
        }
    }
}