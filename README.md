SmartCampusAAU
==============

SmartCampusAAU is a framework for enabling indoor positioning and navigation in any building. The SmartCampusAAU framework was created as part of the SmartCampusAAU research project which was carried out at Aalborg University, Denmark. The project's web page can be found at http://smartcampus.cs.aau.dk/ and contains instructions on how to use the framework.

SmartCampusAAU takes a collaborative approach: It is the end users who enable indoor positioning and navigation in a building. This is done by building a so-called radio map using the SmartCampusAAU app (for Android, iPhone or Windows Phone).

Once a radio map has been built, developers can make use of indoor positioning and navigation by importing the SmartCampusAAU library into their app.

The SmartCampusAAU app has been developed at the Department of Computer Science, Aalborg University in collaboration with <a href="http://www.mapspeople.com">MapsPeople</a> who have created tiles that display building floors on top of Google Maps in places where Google itself does not provide this service.

Instructions on how to use the SmartCampusAAU app to build radio maps can be found in the <a href="http://smartcampus.cs.aau.dk/downloads.html">downloads</a> section on the SmartCampusAAU web page. Similarly, the page contains instructions on how to use the SmartCampusAAU library to leverage the indoor positioning and navigation capabilities in one's on app.

SmartCampusAAU is based on the location fingerprinting technique which relies on collecting signal strength measurements in a building. SmartCampusAAU supports two kinds of positioning based on the location fingerprinting technique: <em>Device-Based Positioning</em> where Wi-Fi scanning and positioning is done one a user's own SmartPhone, and <em>Infrastructure-Based Positioning</em> where Wi-Fi scanning and positioning is done on the Infrastructure side. Note that whereas Infrastructure-Based Positioning can be done in any building where an ordinary Wi-Fi infrastructure is in place, Infrastructure-Based Positioning requires an extra dedicated positioning infrastructure that is usually not in place.  

<b>IMPORTANT NOTICE:</b> The code from the SmartCampusAAU project is made available as-is. It is no longer being maintained, and we do not offer any advice on how to use or tweak the code in addition to what can be found on this page and on the SmartCampusAAU webpage. 

<h2>Structure</h2>
The following is an overall description of the components that consitute the SmartCampusAAU framework.  

<h3>Backend</h3>
This folder contains the backend. The backend stores the radio maps (Device-Based and/or Infrastructure-Based radio maps). 
<h4>RadioMapService3</h4>
This is the backend that contains the Device-Based radio maps. It has been implemented as a WCF Data Service and the original SmartCampusAAU radio maps are exposed as OData at the following URL: <br>
http://smartcampusaau.cs.aau.dk/RadioMapService3/RadioMapService.svc/ . Append '$metadata' to this URL to view the metadata. 

This is the backend that is used in the Android library and app. A radio map is downloaded to the mobile device on-demand when arriving at a building, and then on-device positioning ensues.  

While the current iPhone version relies on infrastructure-based positioning it has now also become possible to use on-device positioning for iOS devices. In order to achieve this, you can port the functionality from the Android library. 

<h4></h4>
