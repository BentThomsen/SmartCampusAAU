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
using SmartCampusWebMap.Library.Location;
using SmartCampusWebMap.Ui.Maps;

namespace SmartCampusWebMap.Ui.Dialogs
{
    public partial class SelectBuildingFloor : PhoneApplicationPage
    {
        public static event EventHandler<BuildingFloorEventArgs> OnBuildingFloorChange;
        public class BuildingFloorEventArgs : EventArgs
        {
            public Building_Floors SelectedFloor { get; private set; }
            public BuildingFloorEventArgs(Building_Floors bf)
            {
                this.SelectedFloor = bf;
            }
        }

        public SelectBuildingFloor()
        {
            InitializeComponent();
            this.ApplicationTitle.Text = Globals.AppName;

            Building b = LocationService.CurrentBuilding;
            if (b == null)
                this.NavigationService.GoBack();
            if (b.Building_Floors == null)
                this.NavigationService.GoBack();

            buildingFloorsListBox.ItemsSource = b.Building_Floors; //We have at least one floor
            buildingFloorsListBox.SelectedItem = b.Building_Floors.FirstOrDefault(bf => bf.Number == Map2D.mCurrentSelectedFloor);
            buildingFloorsListBox.SelectionChanged += new SelectionChangedEventHandler(buildingFloorsListBox_SelectionChanged);

        }

        void buildingFloorsListBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            Building_Floors bf = buildingFloorsListBox.SelectedItem as Building_Floors;
            if (bf != null)
            {
                OnBuildingFloorChanged(this, new BuildingFloorEventArgs(bf));
                //Map2D.mCurrentSelectedFloor = bf.Number;
            }

            this.NavigationService.GoBack();
        }

        protected void OnBuildingFloorChanged(object sender, BuildingFloorEventArgs bfe)
        {
            if (OnBuildingFloorChange != null) //any listeners?
            {
                OnBuildingFloorChange(sender, bfe);
            }
        }
    }
}