
 <!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
<style type="text/css">
  html { height: 100% }
  body { height: 100%; margin: 0; padding: 0 }
  #map_canvas { height: 100% }
</style>

<script type="text/javascript">
var time = new Date().getTime();
var tileUrl = "TODO - INSERT APPROPRIATE TILE FILE";
var floor = 0;
var defaultLocation = {lat:57.01229203773127,lng:9.99149121813967,zoom:18}
</script>
<script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?sensor=true"></script>
<script type="text/javascript" src="js/detect_browser.js?id=1336983883"></script>
<script type="text/javascript" src="js/smartcampus.js?id=1336983883"></script>
<script type="text/javascript" src="js/NativeBridge.js?id=1336983883"></script>
<script type="text/javascript" src="js/googlemap.js?id=1336983883"></script>

</head>

  <body onload="initialize();">
    <div id="log"></div>
    <div id="map_canvas" style="width:100%; height:100%"></div>
  </body>
</html>