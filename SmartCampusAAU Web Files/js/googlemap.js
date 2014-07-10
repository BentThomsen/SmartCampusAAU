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
DISCLAIMED. IN NO EVENT SHALL AALBORG UNIVERSITY BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

var map;
   var marker;

   var maptiler;
   var online = false;

   var c = 0;
   var _checkLocation = false;
   var currentMarker = null;
   var editMarker;
   var icon_location;
   var icon_sniper;
   var icon_location;
   var icon_red;
   var icon_yellow;
   var infoWindow;



var ic_defibrellator;
var ic_des_flag;
var ic_elevator;
var ic_entrance;
var ic_fireextinguisher;
var ic_firstaid;
var ic_food;
var ic_lecture_room;
var ic_moving_walkway;
var ic_person;
var ic_staircase;
var ic_toilet;
var ic_vertex_information;
var ic_vertex_no_information;

var ic_star; /* temp - kun til stjernedag */

 

function initialize() {


      infoWindow = new google.maps.InfoWindow();


      icon_sniper = new google.maps.MarkerImage("gfx/sniper48.png",new google.maps.Size(48.0,48.0), null,new google.maps.Point(24,24));
      icon_location = new google.maps.MarkerImage("gfx/aqua-sphere-blue.png",new google.maps.Size(12.0,12.0), null,new google.maps.Point(6,6));

      icon_yellow = new google.maps.MarkerImage("gfx/yellow-dot.png",new google.maps.Size(32.0,32.0), null,new google.maps.Point(16,32));

      icon_red = new google.maps.MarkerImage("gfx/red-dot.png",new google.maps.Size(32.0,32.0), null,new google.maps.Point(16,32));

      editMarker = new google.maps.Marker({title:"Hello World!",icon : icon_sniper,size:new google.maps.Size(48.0,48.0)});
      //RH: Tilfojet ekstra ikoner
	ic_defibrellator = new google.maps.MarkerImage("gfx/poi/ic_defibrellator.png");
	ic_des_flag = new google.maps.MarkerImage("gfx/poi/ic_des_flag.png");
	ic_elevator = new google.maps.MarkerImage("gfx/poi/ic_elevator.png");
	ic_entrance = new google.maps.MarkerImage("gfx/poi/ic_entrance.png");
	ic_fireextinguisher = new google.maps.MarkerImage("gfx/poi/ic_fireextinguisher.png");
	ic_firstaid = new google.maps.MarkerImage("gfx/poi/ic_firstaid.png");
	ic_food = new google.maps.MarkerImage("gfx/poi/ic_food.png");
	ic_lecture_room = new google.maps.MarkerImage("gfx/poi/ic_lecture_room.png");
	ic_moving_walkway = new google.maps.MarkerImage("gfx/poi/ic_moving_walkway.png");
	ic_person = new google.maps.MarkerImage("gfx/poi/ic_person.png");
	ic_staircase = new google.maps.MarkerImage("gfx/poi/ic_staircase.png");
	ic_toilet = new google.maps.MarkerImage("gfx/poi/ic_toilet.png");
	ic_vertex_information = new google.maps.MarkerImage("gfx/poi/ic_vertex_information.png");
	ic_vertex_no_information = new google.maps.MarkerImage("gfx/poi/ic_vertex_no_information.png");
	ic_star = new google.maps.MarkerImage("gfx/poi/star-3.png");






      var frh = new google.maps.LatLng(defaultLocation.lat,defaultLocation.lng);

 

     var myOptions = {
  
        zoom: defaultLocation.zoom,

        center: frh,

        mapTypeId: google.maps.MapTypeId.ROADMAP,

        streetViewControl: false,

        mapTypeControl:true

      };


      map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);


      maptiler = new google.maps.ImageMapType({

      getTileUrl: function(coord, zoom) {

          var url = tileUrl+floor+"/z" + zoom + "/x" +coord.x+ "/y" + coord.y + ".png";

          return url;

      },

      tileSize: new google.maps.Size(256, 256),

        isPng: true
      });



      map.overlayMapTypes.insertAt(0, maptiler);


      google.maps.event.addListener(map, 'click', function(event) {
          try { 
			if (IsWindows()) {
				window.external.Notify("setSelectedLocation|" + online + "|" + floor + "|" + event.latLng.lat() + "|" + event.latLng.lng());
			}
            else /* if (IsAndroid()) */ {
			    window.DeviceInterface.setSelectedLocation(online,floor,event.latLng.lat(),event.latLng.lng());
			}
          } catch(setSelectedLocationError){}

      });

      try { add_poi(null); } catch(addPoiError){}

      try {
          if (IsWindows()) {
            window.external.Notify("setMapReady");
          }
          else /* if (IsAndroid()) */ {
            window.DeviceInterface.setMapReady();
          }          
      } catch(setMapReadyError){}


    }