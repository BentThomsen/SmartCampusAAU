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
using SmartCampusWebMap.Ui.Maps.Offline.Graph;
using SmartCampusWebMap.RadiomapBackend;
using Microsoft.Phone.Shell;
using SmartCampusWebMap.Library.Location;
using System.Data.Services.Client;
using System.Text;

namespace SmartCampusWebMap.Ui.Dialogs
{
    public partial class SelectEdgeType : PhoneApplicationPage
    {
                
        private enum EdgeType { None, Elevator, Stairs }
        private Edge mCurrentEdge;

        private ApplicationBarIconButton menuSave;
        
        public SelectEdgeType()
        {
            InitializeComponent();
            this.ApplicationTitle.Text = Globals.AppName;
            
            InitializeCurrentEdge();
            
            InitializeMenu();

            InitializeListbox();

            InitializeUploadDialog();
        }

        private void InitializeCurrentEdge()
        {
            mCurrentEdge = UiBing_Map2DEditLinks.CurrentEdge;
            if (mCurrentEdge == null)
                throw new ArgumentNullException("There is no current edge!");
        }

        private void InitializeListbox()
        {
            edgeTypeListbox.ItemsSource = new string[]
            {
                EdgeType.None.ToString(),
                EdgeType.Elevator.ToString(),
                EdgeType.Stairs.ToString()
            };
            edgeTypeListbox.SelectedIndex = (int)EdgeType.None;
        }

        private void InitializeMenu()
        {
            ApplicationBar = new ApplicationBar();
                        
            menuSave = new ApplicationBarIconButton();
            menuSave.IconUri = new Uri(Globals.IconUri_Save, UriKind.Relative);
            menuSave.Text = "Save";
            menuSave.Click += new EventHandler(menuSave_Click);
            ApplicationBar.Buttons.Add(menuSave);
        }

        void menuSave_Click(object sender, EventArgs e)
        {
            //Reset edge types
            mCurrentEdge.is_elevator = false;
            mCurrentEdge.is_stair = false;
            
            //Set appropriate edge type
            EdgeType edgeType = (EdgeType)edgeTypeListbox.SelectedIndex;
            switch (edgeType)
            {
                case EdgeType.None:
                    //None - do nothing
                    break;
                case EdgeType.Elevator:
                    mCurrentEdge.is_elevator = true;
                    break;
                case EdgeType.Stairs:
                    UiBing_Map2DEditLinks.CurrentEdge.is_stair = true;
                    break;
            };

            StringBuilder sb = new StringBuilder();
            sb.Append("Building_ID = " + mCurrentEdge.Building_ID + "\n");
            sb.Append("Origin ID = " + mCurrentEdge.vertexOrigin + "\n");
            sb.Append("Destination ID = " + mCurrentEdge.vertexDestination + "\n");
            sb.Append("Is Elevator = " + mCurrentEdge.is_elevator + "\n");
            sb.Append("Is Stair = " + mCurrentEdge.is_stair + "\n");

            MessageBoxResult mbr = MessageBox.Show(sb.ToString());
            if (mbr == MessageBoxResult.OK)
                AddEdgeTask();
        }

        #region Uploading of edge
        radiomapEntities mContext;
        ProgressIndicator uploadProgressIndicator;
        private bool isUploading;

        public static event EventHandler OnEdgeAdded;
        
        private void InitializeUploadDialog()
        {
            uploadProgressIndicator = new ProgressIndicator()
            {
                IsVisible = false,
                IsIndeterminate = true,
                Text = "Uploading Changes ..."
            };
            SystemTray.SetProgressIndicator(this, uploadProgressIndicator);
        }

        //Upload a symbolicLocation in the background (update or create)
        //Form: AsyncTask<[Input Parameter Type], [Progress Report Type], [Result Type]>
        private void AddEdgeTask()
        {
            isUploading = true;
            mContext = LocationService.radiomapContext;
            
            /*
            Uri radiomapUri = new Uri("http://smartcampusaau.cs.aau.dk/RadioMapService3/RadioMapService.svc/");
            mContext = new radiomapEntities(radiomapUri);
            Building b = (mContext.Buildings.Where(b1 => b1.ID == LocationService.CurrentBuilding.ID)).First();
            Vertex v0 = (mContext.Vertices.Where(v => v.ID == mCurrentEdge.Vertices[0].ID)).First();
            Vertex v1 = (mContext.Vertices.Where(v => v.ID == mCurrentEdge.Vertices[1].ID)).Single();
            mContext.AddToEdges(mCurrentEdge);
            mContext.AddLink(b, "Edges", mCurrentEdge);
            mContext.AddLink(v0, "Edges", mCurrentEdge);
            mContext.AddLink(v1, "Edges", mCurrentEdge);
            */

            //Link to/from the building and endpoints
            mContext.AddToEdges(mCurrentEdge);
            Building b = LocationService.CurrentBuilding;
            b.Edges.Add(mCurrentEdge);
            /*
            foreach (Vertex v in mCurrentEdge.Vertices)
            {
                if (!v.Edges.Contains(mCurrentEdge))
                    v.Edges.Add(mCurrentEdge);
            }
            */
            try
            {
                mContext.BeginSaveChanges(OnChangesSaved, mContext);

                uploadProgressIndicator.IsVisible = isUploading;

            }
            catch (Exception ex)
            {
                isUploading = false;
                uploadProgressIndicator.IsVisible = isUploading;

                MessageBox.Show(string.Format("The changes could not be saved.\n"
                    + "The following error occurred: {0}", ex.Message));
            }
        }

        private void OnChangesSaved(IAsyncResult result)
        {
            bool errorOccured = false;

            // Use the Dispatcher to ensure that the 
            // asynchronous call returns in the correct thread.
            Dispatcher.BeginInvoke(() =>
            {
                isUploading = false;
                uploadProgressIndicator.IsVisible = isUploading;

                mContext = result.AsyncState as radiomapEntities;
                try
                {
                    // Complete the save changes operation and display the response.
                    DataServiceResponse response = mContext.EndSaveChanges(result);

                    foreach (ChangeOperationResponse changeResponse in response)
                    {
                        if (changeResponse.Error != null) errorOccured = true;
                    }
                    if (!errorOccured)
                    {
                        Globals.ShowDialog(this, Globals.CHANGES_SAVED, Globals.DURATION_SHORT);

                        //Finalize edge, i.e., add it to the endpoint's edges and to the building
                        /*
                        Building b = LocationService.CurrentBuilding;
                        b.Edges.Add(mCurrentEdge);
                        foreach (Vertex v in mCurrentEdge.Vertices)
                        {
                            if (!v.Edges.Contains(mCurrentEdge))
                                v.Edges.Add(mCurrentEdge);
                        }
                        */
                        NavigationService.GoBack();
                        //Tell everyone the good news!
                        if (OnEdgeAdded != null)
                        {
                            OnEdgeAdded(this, EventArgs.Empty);
                        } 
                    }
                    else
                    {
                        MessageBox.Show("An error occured. One or more changes could not be saved.");
                    }
                }
                catch (Exception ex)
                {
                    // Display the error from the response.
                    string errorMsg = ex.InnerException != null ?
                        ex.InnerException.Message :
                        ex.Message;
                    MessageBox.Show(string.Format("The following error occured: {0}", errorMsg));
                }
            }
            );
        }
        #endregion

    }
}