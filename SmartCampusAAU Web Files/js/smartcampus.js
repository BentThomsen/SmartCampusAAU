var items = [];
var lines = [];
var poi_map = [];
var estimateCircle;
var provider_status;
var is_tracking = true;

/* smartcampus (0 = gps, 1 = Wi-Fi, 2 = NONE) */
function getProviderStatus() {
	return provider_status;
}

/* smartcampus */
function setProviderStatus(status) {
    provider_status = status;
    alert(provider_status);
    if (provider_status == 2 && estimateCircle != null) {
        estimateCircle.setVisible(false);
    }	
}

/* smartcampus */
function setIsTracking(track) {
    alert(track);    
    if (track == "false")
    	is_tracking = false;
    else
        is_tracking = true;
    
}
/* smartcampus */
function isTracking() {
    return is_tracking;
}

/* return appropriate google.maps.MarkerImage */
function getCorrectMarker(vertex) {
		
	/* First check for 'walking' properties */
	if (vertex.isStairEndpoint) 
		return ic_staircase;
	if (vertex.isElevatorEndpoint)
		return ic_elevator;
	
	if (vertex.title == "null")					
		return ic_vertex_no_information;
	
	/* v has a symbolic location. Now, check for special properties */
	/* SymbolicLocation symLoc = v.getLocation().getSymbolicLocation(); */
	if (vertex.isEntrance)
		return ic_entrance;

	switch (vertex.location_type)
	{
/*
		NONE,
		OFFICE,
		DEFIBRELLATOR,
		FIRST_AID_KIT,
		FIRE_EXTINGUISHER,
		TOILET, 
		FOOD,
		LECTURE_ROOM;
*/
		case 0: return ic_vertex_information;  /* NONE */
		case 1: return ic_person;              /* OFFICE */
		case 2: return ic_defibrellator;      /* DEFIBRELLATOR */
		case 3: return ic_firstaid;            /* FIRST_AID_KIT */
		case 4: return ic_fireextinguisher;    /* FIRE_EXTINGUISHER */
		case 5: return ic_toilet;              /* TOILET */
		case 6: return ic_food;		             /* FOOD */
		case 7: return ic_lecture_room;	       /* LECTURE_ROOM */
    case 8: return ic_star; /* TEMP - KUN TIL STJERNEDAG */
		default: return ic_vertex_information; 
	}	
}  



function showEdges(floorNum,edges){
  /* removes all edges lines stored. */
  for(var i = 0 ; i<lines.length ; i++){
    lines[i].setMap(null);
  }
  lines.length = 0;

  for(var i = 0 ; i<edges.length ; i++){
    var LineCordinates = new Array();
    LineCordinates[0] = new google.maps.LatLng(edges[i].endpoint1.lat, edges[i].endpoint1.lon);
    LineCordinates[1] = new google.maps.LatLng(edges[i].endpoint2.lat, edges[i].endpoint2.lon);

    var Line = new google.maps.Polyline({
      path: LineCordinates,
      geodesic: false,
      strokeColor: "#FF0000", 
      strokeOpacity: 1,
      strokeWeight: 3,
      obj:edges[i]
    });

    google.maps.event.addListener(Line, 'click', function() {
        try { window.DeviceInterface.removeLink(this.obj.endpoint1.id,this.obj.endpoint2.id); } catch(onTabError){}
    });

    Line.setMap(map);
    lines.push(Line);
  }
}

function clearOverlays() {
    /* fjern alle overlays, dvs. alle POI markoerer for nuvaerende etage */
    for(var i = 0 ; i<items.length ; i++){
      items[i].setMap(null);
    }
    items.length = 0;
}

/* smartcampus
 * add vertex layer
 */
function addGraphOverlay(level,isOnline,vertex) {
   online = isOnline;
   floor = level;

   map.overlayMapTypes.removeAt(0, maptiler);
   map.overlayMapTypes.insertAt(0, maptiler);

   /* vis alle overlays (POI markoerer for den angivne etage */
   for(var i = 0 ; i<vertex.length ; i++){
     items.push(addMarker(vertex[i]));
   }   
}

/* add marker to the map */
function addMarker(vertexItem){
    var marker = new google.maps.Marker({
        position: new google.maps.LatLng(vertexItem.latitude,vertexItem.longitude),
        map: map,
        title:vertexItem.title
    });
            
    marker.id = vertexItem.id;
    marker.setIcon(getCorrectMarker(vertexItem));

    if (vertexItem.title != "null") {
      var title = vertexItem.title;
      if (vertexItem.url && !(vertexItem.url === ""))
          url = '<p><a href="' + vertexItem.url + '"> Visit webpage </a></p>';
      else
          url = "";
      var contentString = '<h3>' + title + '</h3>' + '<p>' + vertexItem.description + '</p>' + 
	    url + '<p><a href="javascript:void(0)" onclick="onTap(' + vertexItem.id + ');"> Options &gt;&gt; </a>';
 
        // <a href="[alternative link]" onclick="dosomething(); return false;">
        // url + '<p><a href="http://www.jp.dk" onclick="onTap(' + vertexItem.id + '); return false;"> Options >> </a></p>'; 
        // url + '<p><a href="#" onclick="onTap(' + vertexItem.id + ');"> Options >> </a></p>'; 
        // url + '<p><a href="javascript:onTap(' + vertexItem.id + ');"> Options >> </a></p>'; 

      var infowindow = new google.maps.InfoWindow({
              content: contentString
      });
      //infowindow.position = marker.position;

      google.maps.event.addListener(marker, 'click', function() {
          infowindow.open(map, marker); 
      });
    }
    else {
      google.maps.event.addListener(marker, 'click', function() {
        //try { 
            onTap(vertexItem.id);
        //}
        //catch(onTabError){}
      });
    }
        
    return marker;
}


function onTap(vertexId) {
	try { 
		if (IsWindows()) {
        window.external.Notify("onTap|" + online + "|" + floor + "|" + vertexId);
    }
    else if (IsAndroid()) {
        window.DeviceInterface.onTap(online,floor,vertexId);
    }
		alert(vertexId);
 	} catch(onTabError){}


}

/* Called from Smartcampus app */
function updateNewLocation(location) {  
    
      var newLocation = new google.maps.LatLng(location.latitude,location.longitude);   

      if(currentMarker == null) {
        currentMarker = new google.maps.Marker({
            position: newLocation,
            map: map,
            title:"Hello World!",
            icon : icon_location
        });
        
        var circleOptions = {
            strokeColor: "#FF0000",
          strokeOpacity: 0.8,
          strokeWeight: 2,
            fillColor: "#FF0000",
            fillOpacity: 0.35,
            map: map,
            center: newLocation,
            radius: Math.ceil(location.accuracy)
        };
        
        estimateCircle = new google.maps.Circle(circleOptions);  
      }
      /* Center around location if 'tracking' is enabled */
      if (isTracking()) {
          map.panTo(newLocation);
          //map.setCenter(newLocation);
      }	
      /* update location */
      estimateCircle.setCenter(newLocation);
      estimateCirle.setRadius(Math.ceil(location.accuracy));
      estimateCircle.setVisible(true);
      currentMarker.setPosition(newLocation);      
}

/* Called from Smartcampus app (and calls back to the SmartCampus app)
 * Called (in the offline phase) when the user clicks on the map
 * Params: x and y screen coordinates of the device. 
 */
function updateSelectedLocation(x,y){

    var curent_location = editMarker.getPosition();

    current_point = map.getProjection().fromLatLngToPoint(curent_location);

    var location = map.getProjection().fromPointToLatLng(new google.maps.Point(current_point.x+(x*0.000001),current_point.y+(y*0.000001)));

    editMarker.setOptions({position: location ,map: map});

    try { 
      if (IsWindows()) {
        window.external.Notify("setSelectedLocation|" + online + "|" + floor + "|" + location.lat() + "|" + location.lng());
      }
      else if (IsAndroid()) {
        window.DeviceInterface.setSelectedLocation(online,floor,location.lat(),location.lng());
      }
    } catch(setUpdateSelectedLocationError){}
}



/* Called from Smartcampus app
 * Called in the Edit Links page when choosing an endpoint (vertex with the given id)
 */
function setEndpoint(vertexId){
    for(var i = 0 ; i<items.length ; i++){
        if(items[i].id == vertexId){
            items[i].setIcon(icon_yellow);
            return;
        }
    }
}

/* Called from Smartcampus app
 * Called in the Edit Links page when removing an endpoint (vertex with the given id)
 */
function removeEndpoint(vertexId){
    for(var i = 0 ; i<items.length ; i++){
        if(items[i].id == vertexId){
	          items[i].setIcon(getCorrectMarker(items[i]));
            return;
        }
    }
}

/*Called from Smartcampus app
 * Centers the map around the specified lat, lon coordinates
 */
function centerAt(lat,lng){
    map.setCenter(new google.maps.LatLng(lat,lng));
}

/* Called from Smartcampus app. 
 * Allows to change the view type from the app
 */
function setViewType(viewType){  
  switch(viewType)
  {
  case 'MAP':
    map.setMapTypeId(google.maps.MapTypeId.ROADMAP);
    break;
  case 'SATELLITE':
    map.setMapTypeId(google.maps.MapTypeId.SATELLITE);
    break;
  case 'STREET':
    map.setMapTypeId(google.maps.MapTypeId.HYBRID);
    break;
  case 'TRAFFIC':
    map.setMapTypeId(google.maps.MapTypeId.TERRAIN);
    break;
  }
}

function changeFloor(k){
  floor = k.level;
  map.overlayMapTypes.removeAt(0, maptiler);
  map.overlayMapTypes.insertAt(0, maptiler);
}

function showSelectedLocation(lat,lng){
  editMarker.setOptions({position: new google.maps.LatLng(lat,lng) ,map: map});
}

function search(s){
  document.getElementById("log").innerHTML = typeof(s) + " value : "+s;
  var keywords = s;
  var pattern=new RegExp(keywords,"i");
  if(keywords.length>1){
    for(var i = 0 ; i<poi_map.length;i++){
      if(pattern.test(poi_map[i].title)){
        poi_map[i].setMap(map);
      }else{
        poi_map[i].setMap(null);
      }
    }
  }else{
    clear_poi();
  }
}

function add_poi(b){

  for(var i = 0 ; i<poi.length;i++){
       marker = new google.maps.Marker({
        position: new google.maps.LatLng(poi[i].LAT,poi[i].LNG),
        map: b,
        poi:poi[i],
        title:poi[i].NAME
       });
       google.maps.event.addListener(marker,"click",function() {
         infoWindow.setOptions({position:this.getPosition(),content:this.poi.NAME+"<br/><a href='http://"+this.poi.URL+"'>"+this.poi.URL+"</a>"});
         infoWindow.open(map);
       });
       poi_map.push(marker);
  }
}

function clear_poi(){
  for(var i = 0 ; i<poi_map.length;i++){
    poi_map[i].setMap(null);
  }
}