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
using Microsoft.Phone.Info;
using System.Windows.Navigation;
using SmartCampusWebMap.WifiSnifferPositioningService;
using System.Data.Services.Client;
using Microsoft.Phone.Shell;
using SmartCampusWebMap.Ui.Maps.Offline;
using SmartCampusWebMap.RadiomapBackend;
using System.Text;
using SmartCampusWebMap.Library.Location;

namespace SmartCampusWebMap.Ui.Offline.Wifi
{
    public partial class WifiScanForm : PhoneApplicationPage
    {   
        //We are using a simple state machine to control which buttons are enabled/disabled
        private enum MeasurementState { NotStarted, Started, Stopped }
        private void SetState(MeasurementState newState)
        {
            switch (newState)
            {
                case MeasurementState.NotStarted:
                    menuStart.IsEnabled = true;
                    menuStop.IsEnabled = false;
                    menuSave.IsEnabled = false;
                    break;
                case MeasurementState.Started:
                    menuStart.IsEnabled = false;
                    menuStop.IsEnabled = true;
                    menuSave.IsEnabled = false;
                    break;
                case MeasurementState.Stopped:
                    menuStart.IsEnabled = true;
                    menuStop.IsEnabled = false;
                    menuSave.IsEnabled = true;
                    break;
            }
        }

        private static SnifferModel context { get; set; }
        private static readonly Uri snifferUri =
            new Uri("http://smartcampus.cs.aau.dk/WifiSnifferPositioningService/SnifferService.svc/");

        private string _macAddress;

        public WifiScanForm()
        {
            InitializeComponent();
            InitializeMenu();
            InitializeUploadDialog();

            _macAddress = LocationService.MacAddress;
            context = new SnifferModel(snifferUri);
            SetState(MeasurementState.NotStarted);
        }

        //Application bar buttons
        private ApplicationBarIconButton menuStart, menuStop, menuSave;

        /// <summary>
        /// Initialize the buttons on the application bar
        /// </summary>
        private void InitializeMenu()
        {
            ApplicationBar = new ApplicationBar();

            menuStart = new ApplicationBarIconButton();
            menuStart.IconUri = new Uri(Globals.IconUri_StartMeasure, UriKind.Relative);
            menuStart.Text = "Start";
            menuStart.Click += new EventHandler(menuStart_Click);
            ApplicationBar.Buttons.Add(menuStart);

            menuStop = new ApplicationBarIconButton();
            menuStop.IconUri = new Uri(Globals.IconUri_StopMeasure, UriKind.Relative);
            menuStop.Text = "Stop";
            menuStop.Click += new EventHandler(menuStop_Click);
            menuStop.IsEnabled = false;
            ApplicationBar.Buttons.Add(menuStop);

            menuSave = new ApplicationBarIconButton();
            menuSave.IconUri = new Uri(Globals.IconUri_Save, UriKind.Relative);
            menuSave.Text = "Save";
            menuSave.Click += new EventHandler(menuSave_Click);
            menuSave.IsEnabled = false;
            ApplicationBar.Buttons.Add(menuSave);            
        }

        /// <summary>
        /// Called when the 'save' button is clicked
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="curEdge"></param>
        void menuSave_Click(object sender, EventArgs e)
        {
            uploadProgressIndicator.IsVisible = true;
            string queryString = string.Format("SaveMeasurement?clientMac='{0}'", _macAddress);
            try
            {
                context.BeginExecute<int>(
                    new Uri(queryString, UriKind.Relative),
                    OnSaveMeasurementComplete, context);
            }
            catch (DataServiceQueryException ex)
            {
                QueryOperationResponse response = ex.Response;
                MessageBox.Show(response.Error.Message);
            }           
        }

        /// <summary>
        /// Called when the 'start measuring' button is clicked
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="curEdge"></param>
        void menuStart_Click(object sender, EventArgs e)
        {
            //Check if new or existing vertex
            Vertex curVertex = Map2DOffline.SelectedOfflineVertex;
            bool isUnbound = curVertex.ID == Map2DOffline.UNBOUND_ID;
            string queryString;
            if (isUnbound)
            {
                //Converts to int and keeps 6 decimal precision - on the server we correspondingly
                //divide by a million to reproduce the coordinates
                //Reason for using ints: http://stackoverflow.com/questions/5094276/how-to-pass-a-decimal-number-to-a-rest-web-service
                const int e6 = 1000000;
                AbsoluteLocation absLoc = curVertex.AbsoluteLocations.First();
                int latE6 = (int)(absLoc.latitude * e6);
                int lonE6 = (int)(absLoc.longitude * e6);
                int alt = (int)absLoc.altitude;
                int buildingId = curVertex.Building_ID;

                //public bool StartMeasuringAtUnboundLocation(string clientMac, int buildingId, int latE6, int lonE6, int altE6)
                StringBuilder sb = new StringBuilder();
                sb.Append("StartMeasuringAtUnboundLocation?");
                sb.Append(string.Format("clientMac='{0}'&", _macAddress));
                sb.Append(string.Format("buildingId={0}&", buildingId));
                sb.Append(string.Format("latE6={0}&", latE6));
                sb.Append(string.Format("lonE6={0}&", lonE6));
                sb.Append(string.Format("alt={0}", alt));
                queryString = sb.ToString();
            }
            else
            {
                //public bool StartMeasuringAtBoundLocation(string clientMac, int vertexId)                
                StringBuilder sb = new StringBuilder();
                sb.Append("StartMeasuringAtBoundLocation?");
                //BEWARE: If device id contains '.' it MAY cause problems for some servers. The question is: CAN device ids contain '.'s??
                sb.Append(string.Format("clientMac='{0}'&", _macAddress));
                sb.Append(string.Format("buildingId={0}&", curVertex.Building_ID));
                sb.Append(string.Format("vertexId={0}", curVertex.ID));
                queryString = sb.ToString();
            }
            try
            {
                context.BeginExecute<bool>(
                    new Uri(queryString, UriKind.Relative),
                    OnStartMeasurementComplete, context);
            }
            catch (DataServiceQueryException ex)
            {
                QueryOperationResponse response = ex.Response;
                MessageBox.Show(_macAddress + "\n" + response.Error.Message);
            }
        }

        /// <summary>
        /// Called when the 'stop measuring' button is clicked
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="curEdge"></param>
        void menuStop_Click(object sender, EventArgs e)
        {
            string queryString = string.Format("StopMeasuring?clientMac='{0}'", _macAddress);  
                
            try
            {
                context.BeginExecute<bool>(
                    new Uri(queryString, UriKind.Relative),
                    OnStopMeasurementComplete, context);
            }
            catch (DataServiceQueryException ex)
            {
                QueryOperationResponse response = ex.Response;
                MessageBox.Show(response.Error.Message);
            }

        }

        /// <summary>
        /// Callback for when 'StartMeasurement(clientMac, vertexId)' on the server returns
        /// </summary>
        /// <param name="result"></param>
        private void OnStartMeasurementComplete(IAsyncResult result)
        {
            // Get the radiomapContext back from the stored state.
            var context = result.AsyncState as SnifferModel;

            try
            {
                // Complete the exection and write out the results.
                bool measurementStarted = context.EndExecute<bool>(result).FirstOrDefault();
                if (measurementStarted) 
                {
                    Dispatcher.BeginInvoke(
                        () =>
                        {
                            SetState(MeasurementState.Started);
                        });
                }
            }
            catch (DataServiceQueryException ex)
            {
                QueryOperationResponse response = ex.Response;
                Dispatcher.BeginInvoke(
                    () =>
                    {
                        SetState(MeasurementState.NotStarted);
                        MessageBox.Show(response.Error.Message);
                    }
                ); 
            }
        }

        /// <summary>
        /// Callback for when 'StopMeasurement(clientMac)' on the server returns
        /// </summary>
        /// <param name="result"></param>
        private void OnStopMeasurementComplete(IAsyncResult result)
        {
            // Get the radiomapContext back from the stored state.
            var context = result.AsyncState as SnifferModel;

            try
            {
                // Complete the exection and write out the results.
                bool measurementStopped = context.EndExecute<bool>(result).FirstOrDefault();
                //If yes, there was a measurement to stop, meaning a sniffer infrastructure is in 
                //place and working
                if (measurementStopped)
                {
                    Dispatcher.BeginInvoke(
                        () =>
                        {
                            SetState(MeasurementState.Stopped);
                        }
                    );
                }
                else //No sniffer measurements received
                {
                    Dispatcher.BeginInvoke(
                        () =>
                        {
                            SetState(MeasurementState.NotStarted);

                            StringBuilder sb = new StringBuilder();
                            sb.Append("The server reported that there was no measurement to stop, which may be caused by either of the following:\n");
                            sb.Append("1) An invalid Mac Adress was sent to the server.\n  Please check the current Mac address in 'Online' -> 'Set Mac Address'\n");
                            sb.Append("2) No Wi-Fi sniffer infrastructure is in place.\n  In this case, please contact the appropriate building administrator");
                            string msg = sb.ToString();
                            MessageBox.Show(msg);
                        }
                    );
                }
            }
            catch (DataServiceQueryException ex)
            {
                QueryOperationResponse response = ex.Response;
                Dispatcher.BeginInvoke(
                    () =>
                    {
                        SetState(MeasurementState.NotStarted);
                        MessageBox.Show(response.Error.Message);
                    }
                );                
            }
        }

        /// <summary>
        /// Callback for when 'SaveMeasurement(clientMac)' on the server returns
        /// </summary>
        /// <param name="result"></param>
        private void OnSaveMeasurementComplete(IAsyncResult result)
        {
            // Get the radiomapContext back from the stored state.
            var context = result.AsyncState as SnifferModel;

            try
            {
                //vertexId contains the id of the affected vertex or -1 if there was an error
                int vertexId = context.EndExecute<int>(result).FirstOrDefault();
                if (vertexId > -1)
                {
                    //Update the building where the measurement was conducted
                    //We fetch a new copy from the server in order to include other peoples' changes
                    Map2DOffline.SelectedOfflineVertex.ID = vertexId;
                    LocationService.DownloadRadioMap(Map2DOffline.SelectedOfflineVertex.Building_ID);
                }
            }
            catch (DataServiceQueryException ex)
            {
                QueryOperationResponse response = ex.Response;
                Dispatcher.BeginInvoke(
                    () =>
                    {
                        MessageBox.Show(response.Error.Message);
                    }
                );
            }

            Dispatcher.BeginInvoke(
                () =>
                {
                    SetState(MeasurementState.NotStarted); //success or not - go to this state
                    uploadProgressIndicator.IsVisible = false;
                }
            );
        }       

        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            //If previous page was 'dialog' page ('Add new Measurement' chosen)
            //we skip it and go straight back to the offline map.
            string source;
            this.NavigationContext.QueryString.TryGetValue("source", out source);
            if (source != null)
            {
                this.NavigationService.RemoveBackEntry();
            }
        }

        #region Upload changes
        //displays upload progress
        ProgressIndicator uploadProgressIndicator;
        //private static bool isUploading;

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

        //private radiomapEntities mContext;
        /*
        private void SaveChanges()
        {
            bool isUnbound = Map2DOffline.SelectedOfflineVertex.ID == Map2DOffline.UNBOUND_ID;
            if (isUnbound) 
            {
                //Saving the vertex will then proceed to saving the measurement
                SaveNewVertex();
            }
            else
            {
                SaveMeasurement();
            }
        }
         */ 

        /// <summary>
        /// Saves a NEW (previously unbound) vertex persistently. 
        /// Throws an exception if the vertex is not unbound
        /// </summary>
        /// <param name="curVertex"></param>
        /*
        private void SaveNewVertex()
        {
            Vertex curVertex = Map2DOffline.SelectedOfflineVertex;
            if (curVertex.ID != Map2DOffline.UNBOUND_ID)
                throw new InvalidOperationException("Can only save NEW measurements");

            wifiProgressIndicator.IsVisible = true; 
            
            radiomapEntities radiomapContext = LocationService.radiomapContext;

            Building b = LocationService.CurrentBuilding;
            b.Vertices.Add(curVertex);
            radiomapContext.AddRelatedObject(b, "Vertices", curVertex);
            try
            {
                radiomapContext.BeginSaveChanges(OnVertexSaveComplete, radiomapContext);
            }
            catch (Exception ex)
            {
                wifiProgressIndicator.IsVisible = false;
                MessageBox.Show(string.Format("The changes could not be saved.\n"
                + "The following error occurred: {0}", ex.Message));
            }
        }
        */

        /*
        private void SaveMeasurement()
        {
            wifiProgressIndicator.IsVisible = true;
            string queryString = string.Format("SaveMeasurement?clientMac='{0}'", _macAddress);

            try
            {
                radiomapContext.BeginExecute<bool>(
                    new Uri(queryString, UriKind.Relative),
                    OnSaveMeasurementComplete, radiomapContext);
            }
            catch (DataServiceQueryException ex)
            {
                wifiProgressIndicator.IsVisible = false;
                QueryOperationResponse response = ex.Response;
                MessageBox.Show(response.Error.Message);
            }
        }
        */
        /*
        private void OnVertexSaveComplete(IAsyncResult result)
        {
            bool errorOccured = false;

            // Use the Dispatcher to ensure that the 
            // asynchronous call returns in the correct thread.
            Dispatcher.BeginInvoke(() =>
            {
                radiomapEntities radiomapContext = result.AsyncState as radiomapEntities;
                try
                {
                    // Complete the save changes operation and display the response.
                    DataServiceResponse response = radiomapContext.EndSaveChanges(result);
                    foreach (ChangeOperationResponse changeResponse in response)
                    {
                        if (changeResponse.Error != null)
                            errorOccured = true;
                    }
                    if (!errorOccured)
                    {
                        SaveMeasurement();
                    }
                    else
                    {
                        wifiProgressIndicator.IsVisible = false;
                        MessageBox.Show("An error occured saving the vertex. The measurement will not be saved");
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
		*/    

        #endregion

    }
}