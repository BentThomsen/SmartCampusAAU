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
using System.Windows.Media.Imaging;
using System.Linq;
using SmartCampusWebMap.Ui.Offline.Graph;
using SmartCampusWebMap.RadiomapBackend;
using SmartCampusWebMap.Library.ModelExtensions.Indoormodel.Graph; //extension methods 

namespace SmartCampusWebMap.Ui.Maps
{
    public class MarkerConfig
    {

        /*
	    private static final int SYM_LOC_MARKER 			= R.BitmapImage.ic_vertex_information;
	    //private static final int NO_SYM_LOC_MARKER		 	= R.BitmapImage.vertex_no_information;
	    private static final int NO_SYM_LOC_MARKER_GREY	 	= R.BitmapImage.ic_vertex_no_information;
	    private static final int STAIRCASE_MARKER 			= R.BitmapImage.ic_staircase;
	    private static final int ELEVATOR_MARKER 			= R.BitmapImage.ic_elevator;
	    private static final int INFO_TOILET				= R.BitmapImage.ic_toilet;
	    private static final int INFO_ENTRANCE				= R.BitmapImage.ic_entrance;
	    private static final int INFO_FIRE_EXTINGUISHER 	= R.BitmapImage.ic_fireexstinguisher;
	    private static final int INFO_FIRST_AID			 	= R.BitmapImage.ic_firstaid;
	    private static final int INFO_MOVING_WALKWAY	 	= R.BitmapImage.ic_moving_walkway;
	    private static final int INFO_OFFICE			 	= R.BitmapImage.ic_person;
	    private static final int INFO_FOOD					= R.BitmapImage.ic_food;
	    private static final int INFO_LECTURE_ROOM			= R.BitmapImage.ic_lecture_room;
	    */

        private const string IconUri_DefibrellatorMarker = "/Images/drawable-hdpi/ic_defibrellator.png";
        private const string IconUri_DestinationMarker = "/Images/drawable-hdpi/ic_des_flag.png";
        private const string IconUri_ElevatorMarker = "/Images/drawable-hdpi/ic_elevator.png";
        private const string IconUri_EntranceMarker = "/Images/drawable-hdpi/ic_entrance.png";
        private const string IconUri_FireExtinguisherMarker = "/Images/drawable-hdpi/ic_fireexstinguisher.png";
        private const string IconUri_FirstAidMarker = "/Images/drawable-hdpi/ic_firstaid.png";
        private const string IconUri_FoodMarker = "/Images/drawable-hdpi/ic_food.png";
        private const string IconUri_LectureRoomMarker = "/Images/drawable-hdpi/ic_lecture_room.png";
        private const string IconUri_MovingWalkwayMarker = "/Images/drawable-hdpi/ic_moving_walkway.png";
        private const string IconUri_OfficeMarker = "/Images/drawable-hdpi/ic_person.png";
        private const string IconUri_ToiletMarker = "/Images/drawable-hdpi/ic_toilet.png";
        private const string IconUri_NoSymbolicLocationMarker = "/Images/drawable-hdpi/ic_vertex_no_information.png";
        private const string IconUri_StaircaseMarker = "/Images/drawable-hdpi/ic_staircase.png";
        private const string IconUri_SymbolicLocationMarker = "/Images/drawable-hdpi/ic_vertex_information.png";
        private const string IconUri_StarMarker = "/Images/drawable-hdpi/star-3.png";

        private static BitmapImage DefibrellatorMarker;
        private static BitmapImage DestinationMarker;
        private static BitmapImage ElevatorMarker;
        private static BitmapImage EntranceMarker;
        private static BitmapImage FireExtinguisherMarker;
        private static BitmapImage FirstAidMarker;
        private static BitmapImage FoodMarker;
        private static BitmapImage LectureRoomMarker;
        private static BitmapImage MovingWalkwayMarker;
        private static BitmapImage OfficeMarker;
        private static BitmapImage ToiletMarker;
        private static BitmapImage NoSymbolicLocationMarker;
        private static BitmapImage StaircaseMarker;
        private static BitmapImage SymbolicLocationMarker;
        private static BitmapImage StarMarker;

        public static BitmapImage getDefibrellatorMarker()
        {
            if (DefibrellatorMarker == null)
                DefibrellatorMarker = new BitmapImage(new Uri(IconUri_DefibrellatorMarker, UriKind.Relative));

            return DefibrellatorMarker;
        }
        public static BitmapImage getDestinationMarker()
        {
            if (DestinationMarker == null)
                DestinationMarker = new BitmapImage(new Uri(IconUri_DestinationMarker, UriKind.Relative));

            return DestinationMarker;
        }
        public static BitmapImage getElevatorMarker()
        {
            if (ElevatorMarker == null)
                ElevatorMarker = new BitmapImage(new Uri(IconUri_ElevatorMarker, UriKind.Relative));

            return ElevatorMarker;
        }
        public static BitmapImage getEntranceMarker()
        {
            if (EntranceMarker == null)
                EntranceMarker = new BitmapImage(new Uri(IconUri_EntranceMarker, UriKind.Relative));

            return EntranceMarker;
        }
        public static BitmapImage getFireExtinguisherMarker()
        {
            if (FireExtinguisherMarker == null)
                FireExtinguisherMarker = new BitmapImage(new Uri(IconUri_FireExtinguisherMarker, UriKind.Relative));

            return FireExtinguisherMarker;
        }
        public static BitmapImage getFirstAidMarker()
        {
            if (FirstAidMarker == null)
                FirstAidMarker = new BitmapImage(new Uri(IconUri_FirstAidMarker, UriKind.Relative));

            return FirstAidMarker;
        }
        public static BitmapImage getFoodMarker()
        {
            if (FoodMarker == null)
                FoodMarker = new BitmapImage(new Uri(IconUri_FoodMarker, UriKind.Relative));

            return FoodMarker;
        }
        public static BitmapImage getLectureRoomMarker()
        {
            if (LectureRoomMarker == null)
                LectureRoomMarker = new BitmapImage(new Uri(IconUri_LectureRoomMarker, UriKind.Relative));
            return LectureRoomMarker;
        }
        public static BitmapImage getMovingWalkwayMarker()
        {
            if (MovingWalkwayMarker == null)
                MovingWalkwayMarker = new BitmapImage(new Uri(IconUri_MovingWalkwayMarker, UriKind.Relative));
            return MovingWalkwayMarker;
        }
        public static BitmapImage getOfficeMarker()
        {
            if (OfficeMarker == null)
                OfficeMarker = new BitmapImage(new Uri(IconUri_OfficeMarker, UriKind.Relative));
            return OfficeMarker;
        }
        public static BitmapImage getToiletMarker()
        {
            if (ToiletMarker == null)
                ToiletMarker = new BitmapImage(new Uri(IconUri_ToiletMarker, UriKind.Relative));
            return ToiletMarker;
        }
        public static BitmapImage getNoSymbolicLocationMarker()
        {
            if (NoSymbolicLocationMarker == null)
                NoSymbolicLocationMarker = new BitmapImage(new Uri(IconUri_NoSymbolicLocationMarker, UriKind.Relative));
            return NoSymbolicLocationMarker;
        }
        public static BitmapImage getStaircaseMarker()
        {
            if (StaircaseMarker == null)
                StaircaseMarker = new BitmapImage(new Uri(IconUri_StaircaseMarker, UriKind.Relative));
            return StaircaseMarker;
        }
        public static BitmapImage getSymbolicLocationMarker()
        {
            if (SymbolicLocationMarker == null)
                SymbolicLocationMarker = new BitmapImage(new Uri(IconUri_SymbolicLocationMarker, UriKind.Relative));
            return SymbolicLocationMarker;
        }
        public static BitmapImage getStarMarker()
        {
            if (StarMarker == null)
                StarMarker = new BitmapImage(new Uri(IconUri_StarMarker, UriKind.Relative));
            return StarMarker;
        }

        public static BitmapImage getCorrectMarker(Vertex vertex)
        {
            //First check for 'walking' properties
            if (vertex.isStairEndpoint())
                return getStaircaseMarker();
            if (vertex.isElevatorEndpoint())
                return getElevatorMarker();

            SymbolicLocation symLoc = vertex.SymbolicLocations.FirstOrDefault();
            if (symLoc == null)
                return getNoSymbolicLocationMarker();

            //v has a symbolic location. Now, check for special properties
            //SymbolicLocation symLoc = v.getLocation().getSymbolicLocation();
            if (symLoc.is_entrance.HasValue && symLoc.is_entrance.Value == true)
                return getEntranceMarker();

            //HACK: The enum is found in the 'wrong' class
            switch ((EditSymbolicLocation.InfoType)symLoc.info_type)
            {
                case EditSymbolicLocation.InfoType.NONE:
                    return getSymbolicLocationMarker();  //NONE
                case EditSymbolicLocation.InfoType.OFFICE:
                    return getOfficeMarker();              //OFFICE
                case EditSymbolicLocation.InfoType.DEFIBRELLATOR:
                    return getDefibrellatorMarker();      //DEFIBRELLATOR
                case EditSymbolicLocation.InfoType.FIRST_AID_KIT:
                    return getFirstAidMarker();            //FIRST_AID_KIT
                case EditSymbolicLocation.InfoType.FIRE_EXTINGUISHER:
                    return getFireExtinguisherMarker();    //FIRE_EXTINGUISHER
                case EditSymbolicLocation.InfoType.TOILET:
                    return getToiletMarker();              //TOILET
                case EditSymbolicLocation.InfoType.FOOD:
                    return getFoodMarker();		       //FOOD
                case EditSymbolicLocation.InfoType.LECTURE_ROOM:
                    return getLectureRoomMarker();	       //LECTURE_ROOM
                case EditSymbolicLocation.InfoType.STJERNE_DAG:
                    return getStarMarker();
                default:
                    return getSymbolicLocationMarker(); //NONE  
            }
        }

        public static Image CreateImage(Vertex v)
        {
            Image img = new Image();
            img.Source = MarkerConfig.getCorrectMarker(v);
            const int actualImgHeight = 37;
            const int actualImgWidth = 32;
            img.MinHeight = actualImgHeight;
            img.MaxHeight = actualImgHeight * 1.5;
            img.MinWidth = actualImgWidth;
            img.MaxWidth = actualImgWidth * 1.5;
            return img;
        }
    }
}
