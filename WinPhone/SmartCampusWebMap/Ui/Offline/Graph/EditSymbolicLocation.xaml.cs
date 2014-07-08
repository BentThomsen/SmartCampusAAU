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
using System.Windows.Navigation;
using Microsoft.Phone.Shell;
using SmartCampusWebMap.Ui.Maps.Offline;
using SmartCampusWebMap.RadiomapBackend;
using System.Data.Services.Client;
using System.Windows.Data;
using System.Windows.Controls.Primitives;
using System.Threading;
using SmartCampusWebMap.Library.Location;

namespace SmartCampusWebMap.Ui.Offline.Graph
{
    public partial class EditSymbolicLocation : PhoneApplicationPage
    {
        public static event EventHandler OnSymbolicLocationChange;

        //displays upload progress
        ProgressIndicator uploadProgressIndicator;
        private static bool isUploading;

        //radiomapContext used to save changes
        radiomapEntities mContext;
                
        //Upload a symbolicLocation in the background (update or create)
	    //Form: AsyncTask<[Input Parameter Type], [Progress Report Type], [Result Type]>
	    private void UploadSymbolicLocationTask(SymbolicLocation tmpSymLoc)
        {
            isUploading = true;
            mContext = LocationService.radiomapContext;

		    Vertex currentVertex = Map2DOffline.SelectedOfflineVertex;
			SymbolicLocation currentSymLoc = currentVertex.SymbolicLocations.FirstOrDefault();
			//1) upload NEW symbolic location 
			if (currentSymLoc == null)
            {
                mContext.AddRelatedObject(currentVertex, "SymbolicLocations", tmpSymLoc);
                currentVertex.SymbolicLocations.Add(tmpSymLoc);
			}
			//2) or UPDATE existing
			else
			{
                //copy updated values 
                currentSymLoc.title = tmpSymLoc.title;
                currentSymLoc.description = tmpSymLoc.description;
                currentSymLoc.url = tmpSymLoc.url;
                currentSymLoc.is_entrance = tmpSymLoc.is_entrance;    
                
                mContext.UpdateObject(currentSymLoc);				
			}
            try
            {
                mContext.BeginSaveChanges(OnChangesSaved, mContext);
                
                //uploadProgressBar.Visibility = System.Windows.Visibility.Visible;
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
                        NavigationService.GoBack();
                        if (OnSymbolicLocationChange != null)
                        {
                            OnSymbolicLocationChange(this, null);
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
        	
	
        public enum InfoType 
	    {
		    NONE,
		    OFFICE,
		    DEFIBRELLATOR,
		    FIRST_AID_KIT,
		    FIRE_EXTINGUISHER,
		    TOILET, 
		    FOOD,
		    LECTURE_ROOM, 
		    STJERNE_DAG //kun relevant for stjernedag
		}       

	    public static String prettyPrint(InfoType info)
	    {
	    	String res = "None";
	    	switch (info)
	    	{
	    	case InfoType.DEFIBRELLATOR: 
	    		res = "Defibrellator";
	    		break;
	    	case InfoType.FIRE_EXTINGUISHER:
	    		res = "Fire Extinguisher";
	    		break;
	    	case InfoType.FIRST_AID_KIT:
	    		res = "First Aid Kit";
	    		break;
	    	case InfoType.OFFICE:
	    		res = "Office";
	    		break;
	    	case InfoType.TOILET:
	    		res = "Toilet";
	    		break;
	    	case InfoType.FOOD:
	    		res = "Food";
	    		break;
	    	case InfoType.LECTURE_ROOM:
	    		res = "Lecture Room";
	    		break;	
	    	case InfoType.STJERNE_DAG:
	    		res = "Stjerne Stand";
	    		break;
	    	default: 
	    		res = "None";
	    		break;
	    	}
	    	return res;
	    }

        private ApplicationBarIconButton saveButton;

        public EditSymbolicLocation()
        {
            InitializeComponent();
            this.ApplicationTitle.Text = Globals.AppName;

            initializeInputBoxes();
            initializeInfoTypes();
            initializeSaveButton();
            InitializeUploadDialog();
        }

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

        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            //We get to here from the 'Edit Location' "dialog" which we skip so we go straight back to the offline map.
            this.NavigationService.RemoveBackEntry();
        }

        private void initializeInputBoxes()
        {            
            SymbolicLocation tmpSymLoc = Map2DOffline.SelectedOfflineVertex.SymbolicLocations.FirstOrDefault();
            if (tmpSymLoc == null)
            {
                tmpSymLoc = new SymbolicLocation();
            }
            else //populate the input boxes
            {
                //Binding is not worth the hassle, but goes like this:
                //Binding titleBinding = new Binding("title");
                //titleBinding.Source = tmpSymLoc;
                //titleTextBox.SetBinding(TextBlock.TextProperty, titleBinding);
                titleTextBox.Text = tmpSymLoc.title ?? "";
                descriptionTextBox.Text = tmpSymLoc.description ?? "";
                urlTextBox.Text = tmpSymLoc.url ?? "";
                isEntranceCheckBox.IsChecked = tmpSymLoc.is_entrance;
            }    
        }

        private void initializeInfoTypes()
        {
            //NOTE: Hardcoded number of values!
            string[] infoTypeValues = new string[9];
            for (int i = 0; i < 9; i++)
            {
                infoTypeValues[i] = prettyPrint((InfoType)i);
            }

            infoTypeListBox.Items.Clear();
            infoTypeListBox.ItemsSource = infoTypeValues;
        }

        private void initializeSaveButton()
        {
            ApplicationBar = new ApplicationBar();

            saveButton = new ApplicationBarIconButton();
            saveButton.IconUri = new Uri(Globals.IconUri_Save, UriKind.Relative);
            saveButton.Text = "Save";
            saveButton.Click += new EventHandler(saveButton_Click);
            ApplicationBar.Buttons.Add(saveButton);

            //menuProvider.IconUri = new Uri(Globals.IconUri_ProviderNone, UriKind.Relative);
            /*
            menuProvider.Click += new EventHandler(menuProvider_Click);
            ApplicationBar.Buttons.Add(menuProvider);
            SelectProvider.OnProviderChange += new EventHandler<SelectPr
             * */
        }

        void saveButton_Click(object sender, EventArgs e)
        {            							
			//tmpSymLoc is used to pass data to UploadSymbolicLocationTask
            //Whether the operation is CREATE or UPDATE is determined in UploadSymbolicLocationTask
            SymbolicLocation tmpSymLoc = new SymbolicLocation();
            tmpSymLoc.title = titleTextBox.Text;
            tmpSymLoc.description =  descriptionTextBox.Text;
            tmpSymLoc.url = urlTextBox.Text;
			tmpSymLoc.is_entrance = isEntranceCheckBox.IsChecked;
            int selectedInfoType = infoTypeListBox.SelectedIndex;
            tmpSymLoc.info_type = selectedInfoType < 0 ? 0 : selectedInfoType;

            UploadSymbolicLocationTask(tmpSymLoc);             
        }

        private void textBox_KeyUp(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Enter)
            {
                this.Focus();
            }
        }

        
    }
}