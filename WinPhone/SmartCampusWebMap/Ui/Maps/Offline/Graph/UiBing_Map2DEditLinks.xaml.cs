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
using Microsoft.Phone.Controls.Maps;
using Microsoft.Phone.Shell;
using SmartCampusWebMap.Ui.Dialogs;
using SmartCampusWebMap.RadiomapBackend;
using SmartCampusWebMap.Library.Location;
using System.Device.Location;
using System.Text;
using System.Collections.ObjectModel;
using System.Windows.Media.Imaging;
using System.Data.Services.Client;

namespace SmartCampusWebMap.Ui.Maps.Offline.Graph
{
    public partial class UiBing_Map2DEditLinks : PhoneApplicationPage
    {
        private static Edge mCurrentEdge;
        public static Edge CurrentEdge
        {
            get
            {
                return mCurrentEdge;
            }
            private set
            {
                mCurrentEdge = value;
            }
        }              

        //Contains the functionality whereas this class is concerned with presentation (in Android everything is in Map2DOffline)
        private Map2DEditLinks mWebMapCommon;
        private List<Vertex> mEndPoints = new List<Vertex>();
        //The original images to revert to when calling RemoveEndpoint:
        private List<Image> mPrevEndpointImages = new List<Image>();
        
        private BitmapImage mEndpointBitmapImage;
        private BitmapImage mStandardBitmapImage;

        private Image mEndpointImage = new Image();
        private Image mStandardImage = new Image();

        private Building _currentBuilding;
        
        //Map layers for vertices, edges, and the selected location 
        private MapLayer mapLayerVertices;
        private MapLayer mapLayerEdges;

        //Application bar buttons
        private ApplicationBarIconButton menuAddLink, menuRemoveLink, menuFloorChanger, menuNearbyPOI;
        
        public UiBing_Map2DEditLinks()
        {
            //NOTE: DER ER MASSER AF REDUNDANS MELLEM ONLINE OG OFFLINE FASEN
            
            InitializeComponent();                        
            InitializeMenu();
            InitializeMap();
            InitializeUploadDialog();
            
            _currentBuilding = LocationService.CurrentBuilding;
            
            mWebMapCommon = new Map2DEditLinks();
            SetupEventHandlers();
            //force map update
            mWebMapCommon.handleMapReady();
        }

        #region Map2DOffline events
        private void SetupEventHandlers()
        {
            //Update the page's title to correpond to the selected vertex
            mWebMapCommon.OnTitleChange += new EventHandler<Map2D.TitleArgs>(mWebMapCommon_OnTitleChange); // (source, args) => this.ApplicationTitle.Text = args.Title;
            mWebMapCommon.OnCenterChange += new EventHandler<Map2D.CoordinateArgs>(mWebMapCommon_OnCenterChange);  //(sender, coordinateArgs) => graphMap.Center = new GeoCoordinate(coordinateArgs.Latitude, coordinateArgs.Longitude);
            mWebMapCommon.OnVisibleVerticesChange += new EventHandler<Map2D.VisibleVerticesArgs>(mWebMapCommon_OnVisibleVerticesChange);
            mWebMapCommon.OnVisibleEdgesChange += new EventHandler<Map2D.VisibleEdgesArgs>(mWebMapCommon_OnVisibleEdgesChange);
            mWebMapCommon.OnClearOverlayChange += (sender, args) => { ClearOverlays(); };

            SelectEdgeType.OnEdgeAdded += (object sender, EventArgs e) => this.RefreshUI();
            OnEdgeRemoved += (sender, e) => RefreshUI();
        }

        void mWebMapCommon_OnTitleChange(object sender, Map2D.TitleArgs e)
        {
            this.ApplicationTitle.Text = e.Title;
        }

        void mWebMapCommon_OnCenterChange(object sender, Map2D.CoordinateArgs e)
        {
            graphMap.Center = new GeoCoordinate(e.Latitude, e.Longitude);
        }

        void mWebMapCommon_OnVisibleVerticesChange(object sender, Map2D.VisibleVerticesArgs e)
        {
            IEnumerable<Vertex> vertices = e.VisibleVertices;
            if (vertices == null)
                return;

            foreach (Vertex v in vertices)
            {
                AddVertexPin(v);
            }
        }

        void mWebMapCommon_OnVisibleEdgesChange(object sender, Map2D.VisibleEdgesArgs e)
        {
            IEnumerable<RadiomapBackend.Edge> edges = e.VisibleEdges;
            if (edges == null)
                return;

            foreach (RadiomapBackend.Edge curEdge in edges)
            {
                AbsoluteLocation origin = null, destination = null;
                bool first = true;
                foreach (Vertex v in curEdge.Vertices)
                {
                    if (first)
                    {
                        origin = v.AbsoluteLocations.First();
                        first = false;
                    }
                    else
                    {
                        destination = v.AbsoluteLocations.First();
                        break;
                    }
                }
                if (origin != null && destination != null)
                {
                    AddEdgeLine((double)origin.latitude, (double)origin.longitude, (double)destination.latitude, (double)destination.longitude);
                }
            }
        }

        #endregion

        private void ClearOverlays()
        {
            //Note: This may not be the optimal way to clear items in a layer
            mapLayerEdges.Children.Clear();
            mapLayerVertices.Children.Clear();
        }

        #region Map and overlays
        void InitializeMap()
        {
            graphMap.ZoomLevel = 15;

            mapLayerEdges = new MapLayer();
            mapLayerVertices = new MapLayer();
            graphMap.Children.Add(mapLayerEdges);
            graphMap.Children.Add(mapLayerVertices);
        }
        
        private BitmapImage GetEndpointImage()
        {
            if (mEndpointBitmapImage == null)
                mEndpointBitmapImage = new BitmapImage(new Uri("/Images/drawable-hdpi/ic_des_flag.png", UriKind.Relative));

            return mEndpointBitmapImage;
        }

        private BitmapImage GetStandardImage()
        {
            if (mStandardBitmapImage == null)
                mStandardBitmapImage = new BitmapImage(new Uri("/Images/drawable-hdpi/ic_vertex_information.png", UriKind.Relative));

            return mStandardBitmapImage;
        }
                
        private void AddEdgeLine(double originLat, double originLon, double destinationLat, double destinationLon)
        {
            MapPolyline edge = new MapPolyline();
            edge.Stroke = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Colors.Red);
            edge.StrokeThickness = 5;
            edge.Opacity = 0.7;
            edge.Locations = new LocationCollection()
            {
                new GeoCoordinate(originLat, originLon),
                new GeoCoordinate(destinationLat, destinationLon)
            };
            mapLayerEdges.Children.Add(edge);
        }
        
        private void AddVertexPin(Vertex v)
        {            
            Image img = MarkerConfig.CreateImage(v);
            img.Tap += (object sender, GestureEventArgs e) =>
            {
                e.Handled = true;
                
                Tapped_UpdatePins(v, img);      
                Tapped_UpdateMenu();
                Tapped_UpdateTitle();                
            };

            //pushpin.MouseLeftButtonUp += new MouseButtonEventHandler(pushpin_MouseLeftButtonUp);
            AbsoluteLocation pos = v.AbsoluteLocations.First();
            
            mapLayerVertices.AddChild(img, new GeoCoordinate((double)pos.latitude, (double)pos.longitude), PositionOrigin.BottomCenter);
        }

        private void Tapped_UpdatePins(Vertex v, Image img)
        {
            if (isEndpoint(v))
            {
                //removeEndpoint(poiIndex, v);
                removeEndpoint(v);
            }
            else
            {
                //A link has to be between two endpoints
                if (hasTwoEndpoints())
                {
                    MessageBox.Show("Too many endpoints.\n Pleasure remove another endpoint first");
                }
                else
                {
                    //addEndpoint(v, img);
                    addEndpoint(v, img);
                }
            }
        }

        private void addEndpoint(Vertex v, Image img)
        {
            mEndPoints.Add(v);
            mPrevEndpointImages.Add(img); //Save ref to org image
            img.Source = MarkerConfig.getDestinationMarker();
        }

        private void Tapped_UpdateMenu()
        {
            menuAddLink.IsEnabled = false;
            menuRemoveLink.IsEnabled = false;

            if (mEndPoints.Count == 2)
            {
                CurrentEdge = GetLinkIfExists();
                if (CurrentEdge != null)
                    menuRemoveLink.IsEnabled = true;
                else
                    menuAddLink.IsEnabled = true;
            }
        }

        private void Tapped_UpdateTitle()
        {
            String title = "? - ?";
            if (mEndPoints.Count == 1)
            {
                Vertex v1 = mEndPoints[0];
                SymbolicLocation s1 = v1.SymbolicLocations.FirstOrDefault();
                String end1 = (s1 != null && s1.title != null)
                                ? s1.title
                                : getCoordinatesTitle(v1);
                title = end1 + " - ?";
            }
            else if (mEndPoints.Count == 2)
            {
                Vertex v1 = mEndPoints[0];
                SymbolicLocation s1 = v1.SymbolicLocations.FirstOrDefault();
                String end1 = (s1 != null && s1.title != null)
                                ? s1.title
                                : getCoordinatesTitle(v1);

                Vertex v2 = mEndPoints[1];
                SymbolicLocation s2 = v2.SymbolicLocations.FirstOrDefault();
                String end2 = (s2 != null && s2.title != null)
                                ? s2.title
                                : getCoordinatesTitle(v2);
                title = end1 + " - " + end2;
            }

            this.ApplicationTitle.Text = title;
        }
        
        #endregion
        
        private void InitializeMenu()
        {
            ApplicationBar = new ApplicationBar();

            //The four buttons
            menuAddLink = new ApplicationBarIconButton();
            menuAddLink.IconUri = new Uri(Globals.IconUri_AddLink, UriKind.Relative);
            menuAddLink.Text = "Add Link";
            menuAddLink.Click += new EventHandler(menuAddLink_Click);
            menuAddLink.IsEnabled = false;
            ApplicationBar.Buttons.Add(menuAddLink);

            menuRemoveLink = new ApplicationBarIconButton();
            menuRemoveLink.IconUri = new Uri(Globals.IconUri_RemoveLink, UriKind.Relative);
            menuRemoveLink.Text = "Remove Link";
            menuRemoveLink.Click += new EventHandler(menuRemoveLink_Click);
            menuRemoveLink.IsEnabled = false;
            ApplicationBar.Buttons.Add(menuRemoveLink);

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
                        
        }

        void menuAddLink_Click(object sender, EventArgs e)
        {
            addLink();
        }

        void menuRemoveLink_Click(object sender, EventArgs e)
        {
            removeLink();
        }

        void menuNearbyPOI_Click(object sender, EventArgs e)
        {
            //Redundans:
            if (_currentBuilding == null)
                MessageBox.Show("Unknown Building");
            else if (_currentBuilding.Building_Floors == null)
                MessageBox.Show("No floors have been added"); //want to show POIs for the current floor
            else
            {
                NavigationService.Navigate(new Uri(Globals.XamlUri_ShowNearbyPoi, UriKind.Relative));
            }
        }

        void menuFloorChanger_Click(object sender, EventArgs e)
        {
            if (_currentBuilding.Building_Floors == null)
                MessageBox.Show("No floors have been added");
            else
                this.NavigationService.Navigate(new Uri(Globals.XamlUri_SelectBuildingFloor, UriKind.Relative));
        }

        void SelectBuildingFloor_OnBuildingFloorChange(object sender, SelectBuildingFloor.BuildingFloorEventArgs e)
        {
            if (_currentBuilding != null)
            {
                mWebMapCommon.changeToFloor(e.SelectedFloor.Number);
            }
        }

        void ShowNearbyPoi_OnPoiSelected(object sender, ShowNearbyPoi.PoiEventArgs e)
        {
            Vertex poi = e.SelectedPoi; // ShowNearbyPoi.SelectedVertexPOI;
            AbsoluteLocation absLoc = poi.AbsoluteLocations.First();
            graphMap.Center = new GeoCoordinate((double)absLoc.latitude, (double)absLoc.longitude);
        }

        #region Handling of selected endpoints
        
        private bool isEndpoint(Vertex v)
        {
            foreach (Vertex cur in mEndPoints)
            {
                if (cur.ID == v.ID)
                    return true;
            }
            return false;
        }

        private bool hasTwoEndpoints()
        {
            return mEndPoints.Count == 2;
        }

        /// <summary>
        /// Checks if a link exists between two endpoints exists, 
        /// and returns the link if it does (otherwise null).
        /// </summary>
        /// <returns></returns>
        private Edge GetLinkIfExists()
        {
            Vertex source = mEndPoints[0];
            Vertex destination = mEndPoints[1];
            if (source == null || destination == null)
                return null;

            bool areConnected = false;
            //check both source and destination's edges (in case of directional edges)            
            foreach (Edge e in source.Edges)
            {
                if (e.vertexDestination == destination.ID)
                {
                    return e;
                }
            }
            if (!areConnected)
            {
                foreach (Edge e in destination.Edges)
                {
                    if (e.vertexDestination == source.ID)
                    {
                        return e;
                    }
                }
            }
            return null;
        }

        private void addLink()
        {
            if (mEndPoints.Count < 2)
            {
                MessageBox.Show("At least one endpoint is missing.");
                return;
            }

            CurrentEdge = GetLinkIfExists();
            if (CurrentEdge != null)
            {
                MessageBox.Show("Link already exists");
            }
            else
            {
                Vertex source = mEndPoints[0];
                Vertex destination = mEndPoints[1];

                //We present the user with different options for annotating the edge
                CurrentEdge = new Edge();
                CurrentEdge.Building = _currentBuilding;
                CurrentEdge.Building_ID = _currentBuilding.ID;
                CurrentEdge.vertexOrigin = source.ID;
                CurrentEdge.vertexDestination = destination.ID;
                CurrentEdge.Vertices.Add(source);
                CurrentEdge.Vertices.Add(destination);

                //We finalize the edge on the next page (if the user chooses to save it)
                //This means, we add the edge to the vertices and building as well
                NavigationService.Navigate(new Uri(Globals.XamlUri_SelectEdgeType, UriKind.Relative));
            }
        }

        private void removeLink()
        {
            if (mEndPoints.Count < 2)
            {
                MessageBox.Show("At least one endpoint is missing.");
                return;
            }

            CurrentEdge = GetLinkIfExists();
            if (CurrentEdge == null)
            {
                MessageBox.Show("There is no link to remove");
            }
            else
            {
                RemoveEdgeTask();    
            }

        }

        private void removeEndpoint(Vertex v)
        {
            int removeIdx = -1;
            //find vertex
            for (int i = 0; i < mEndPoints.Count; i++)
            {
                if (v.ID == mEndPoints[i].ID)
                {
                    removeIdx = i;
                    break;
                }
            }
            //remove vertex
            if (removeIdx != -1)
            {
                mEndPoints.RemoveAt(removeIdx);

                //Revert to original bitmap image
                Image img = mPrevEndpointImages[removeIdx];
                img.Source = MarkerConfig.getCorrectMarker(v); //could also use CreateImage(v) but that creates a new image

                mPrevEndpointImages.RemoveAt(removeIdx);
            }
        }   

        private void RefreshUI()
        {
            mEndPoints = new List<Vertex>();
            mPrevEndpointImages = new List<Image>();
            CurrentEdge = null;

            mWebMapCommon.refreshUI();

            Tapped_UpdateMenu();
            Tapped_UpdateTitle();
        }

        //Write vertex's absLoc like so: (lat; lon; floor)
        private String getCoordinatesTitle(Vertex v)
        {
            AbsoluteLocation absLoc = v.AbsoluteLocations[0];
            StringBuilder sb = new StringBuilder();
            sb.Append("(");
            sb.Append(Math.Round((double)absLoc.latitude, 5));
            sb.Append("; ");
            sb.Append(Math.Round((double)absLoc.longitude, 5));
            sb.Append("; ");
            sb.Append((int)(absLoc.altitude));
            sb.Append(")");
            return sb.ToString();
        }
        
        #endregion
                     
        #region Communication with backend        
        radiomapEntities mContext;
        ProgressIndicator uploadProgressIndicator;
        private bool isUploading;

        private static event EventHandler OnEdgeRemoved;

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

        private void RemoveEdgeTask()
        {
            if (CurrentEdge == null)
                throw new InvalidOperationException("No link to remove");

            isUploading = true;
            mContext = LocationService.radiomapContext;

            //Add the edge
            mContext.DeleteObject(CurrentEdge);
            
            try
            {
                mContext.BeginSaveChanges(OnChangesSaved, mContext);

                uploadProgressIndicator.IsVisible = isUploading;
            }
            catch (Exception ex)
            {
                isUploading = false;
                uploadProgressIndicator.IsVisible = isUploading;

                // Display the error from the response.
                string errorMsg = ex.InnerException != null ?
                    ex.InnerException.Message :
                    ex.Message;
                MessageBox.Show(string.Format("The following error occured: {0}", errorMsg));
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
                        Building b = LocationService.CurrentBuilding;
                        b.Edges.Remove(CurrentEdge);
                        foreach (Vertex v in CurrentEdge.Vertices)
                        {
                            v.Edges.Remove(CurrentEdge);
                        }

                        //Tell everyone the good news!
                        if (OnEdgeRemoved != null)
                        {
                            OnEdgeRemoved(this, EventArgs.Empty);
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
                    MessageBox.Show(string.Format("The following error occured: {0}", ex.Message));
                }
            }
            );
        }

        
        #endregion
    }
}