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

package com.smartcampus.webclient;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.core4j.Enumerable;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OClientBehavior;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.format.FormatType;

import com.smartcampus.android.odata.CorrectMethodTunnelingBehavior;
import com.smartcampus.indoormodel.AbsoluteLocation;
import com.smartcampus.indoormodel.Building;
import com.smartcampus.indoormodel.Building_Floor;
import com.smartcampus.indoormodel.SymbolicLocation;
import com.smartcampus.indoormodel.graph.Edge;
import com.smartcampus.indoormodel.graph.Vertex;
import com.smartcampus.wifi.WifiMeasurement;

public class OData4jWebClient implements IWebClient {

	//private OdataConsumerSmartcampus consumer;
	private final ODataConsumer rawConsumer;
	
	public OData4jWebClient()
	{
		//consumer = new OdataConsumerSmartcampus(Globals.SMARTCAMPUS_SERVICE_ROOT_URI);
		
		String serviceRootURI = ConnectionInfo.SMARTCAMPUS_SERVICE_ROOT_URI;		
		OClientBehavior[] requiredBehaviors = new OClientBehavior[] { new CorrectMethodTunnelingBehavior("DELETE", "PUT") }; 
		rawConsumer = ODataConsumer.newBuilder(serviceRootURI).setFormatType(FormatType.JSON).setClientBehaviors(requiredBehaviors).build();
	
	}
	
	@Override
	public int addBuilding(Building b) {
		int vertexID = -1;
		
		//add vertex to server
		OEntity vertexOEntity = rawConsumer.createEntity("Buildings")
		  .properties(OProperties.int32("ID", b.getBuildingID()))
		  .properties(OProperties.string("Building_Name", b.getName()))
		  .properties(OProperties.string("Ifc_Url", b.getIfcUrl()))
		  .properties(OProperties.decimal("Lat", roundToMax(b.getLatitude())))
		  .properties(OProperties.decimal("Lon", roundToMax(b.getLongitude())))
		  .properties(OProperties.string("Max_Address", b.getMaxAddress()))
		  .properties(OProperties.string("Postal_Code", b.getPostalCode()))
		  .properties(OProperties.string("Country", b.getCountry()))
		  .properties(OProperties.string("Url", b.getUrl()))		  
		  .execute();

		if(vertexOEntity != null)
		{
			vertexID = vertexOEntity.getProperty("ID", Integer.class).getValue();
		}	
	
		return vertexID;
	}
	
	@Override
	public int addBuilding_Floor(Building_Floor bf, Building b) {
		
		OEntity bfOEntity = rawConsumer.createEntity("Building_Floors")
		  .properties(OProperties.int32("Building_ID", b.getBuildingID()))
		  .properties(OProperties.int32("Number", bf.getFloorNumber()))
		  .properties(OProperties.string("Name", bf.getFloorName()))		  
		  .execute();

		if(bfOEntity != null)
		{
			return bfOEntity.getProperty("ID", Integer.class).getValue();
		}	
		else
		{
			return Integer.MIN_VALUE;
		}		
	}
	
	@Override
	public void addBuilding_Macs(List<String> newMacs, Building b) {
		for (String curMac : newMacs)
		{
			try
			{
				rawConsumer.createEntity("Building_MacInfos")
				  .properties(OProperties.int32("Building_ID", b.getBuildingID()))
				  .properties(OProperties.string("Mac", curMac))
				  .execute();
			}
			catch (Exception ex)
			{
				continue;
			}
		}		
	}
	
	@Override
	public int addEdge(Edge input, Building b) {
		int edge_id = -1;
		OEntity result = rawConsumer.createEntity("Edges")
			.properties(OProperties.boolean_("directional", input.isDirectional()))
			.properties(OProperties.boolean_("is_elevator", input.isElevator()))
			.properties(OProperties.boolean_("is_stair", input.isStair()))
			.properties(OProperties.int32("vertexOrigin", input.getOrigin().getId()))
			.properties(OProperties.int32("vertexDestination", input.getDestination().getId()))
			.properties(OProperties.int32("Building_ID", b.getBuildingID()))
			.execute();
		
		if (result != null)
			edge_id = result.getProperty("ID", Integer.class).getValue();
			
		return edge_id;				
	}	
	
	public int addMeasurement(Building building, Vertex vertex, WifiMeasurement input)
	{
		return addMeasurement(input, vertex);
	}	
	
	@Override
	public int addMeasurement(WifiMeasurement input, Vertex vertex)
	{
		int measurement_id = -1;
		OEntity wifiMeasurementOEntity = null;	
		
		//NOTE: The following will ALWAYS throw an IllegalArgumentException
		//The cause is an apparent "Illegal date format"
		//However, it has been verified that that the dates actually get sent 
		//and stored correctly on the server.
		//Well, the entity will be null, so the date properties have been commented out
		try
		{
			wifiMeasurementOEntity = rawConsumer.createEntity("WifiMeasurements")
			.properties(OProperties.int32("Vertex_ID", vertex.getId()))
			//.properties(OProperties.datetime("meas_time_start", input.getMeasTimeStart()))
			//.properties(OProperties.datetime("meas_time_end", input.getMeasTimeEnd()))
			//.link(VERTEX_NAV_PROPERTY, vertexEntity)
			.execute();
		}
		catch (IllegalArgumentException ex)
		{
			ex.printStackTrace();
		}

		//save histograms
		//Commented out: We use the BatchUpdater class instead to upload the measurement's
		//histograms, as sending a lot of separate posts is WILDLY INEFFICIENT!!!
		if (wifiMeasurementOEntity != null)
		{
			measurement_id = wifiMeasurementOEntity.getProperty("ID", Integer.class).getValue();
			
			/*
			Enumeration<String> macs = input.getHistograms().keys();
			while(macs.hasMoreElements()) {
				String mac = macs.nextElement();
				Hashtable<Integer, Integer> h = input.getHistograms().get(mac);
				
				Enumeration<Integer> fingerprintVal = h.keys();
				while(fingerprintVal.hasMoreElements()) {
					int value = fingerprintVal.nextElement();
					int count = h.get(value);
					
					rawConsumer.createEntity("Histograms")
						.properties(OProperties.string("Mac", mac))
						.properties(OProperties.int32("value", value))
						.properties(OProperties.int32("WifiMeasurement_ID", measurement_id))
						.properties(OProperties.int32("count", count))
						.execute();					
					
				}
			}
			*/
		}
				
		return measurement_id;
	}

	@Override
	public int addSymbolicLocation(SymbolicLocation input, Vertex v) {
		OEntity result = rawConsumer.createEntity("SymbolicLocations")
					.properties(OProperties.int32("Vertex_ID", v.getId()))
					.properties(OProperties.string("title", input.getTitle()))
					.properties(OProperties.string("description", input.getDescription()))
					.properties(OProperties.string("url", input.getUrl()))
					.properties(OProperties.boolean_("is_entrance", input.isEntrance()))
					.properties(OProperties.int32("info_type", input.getType().ordinal()))
					.execute();
		
		return result.getProperty("ID", Integer.class).getValue();	
		
	}
	
	@Override
	public int addVertex(Vertex vertex, Building b) {
		int vertexID = -1;
		
		//add vertex to server
		OEntity vertexOEntity = rawConsumer.createEntity("Vertices")
		  .properties(OProperties.int32("Building_ID", b.getBuildingID()))
		  .execute();

		if(vertexOEntity != null)
		{
			vertexID = vertexOEntity.getProperty("ID", Integer.class).getValue();
			vertex.setId(vertexID);
			
			//Add absoluteLocation to server
			AbsoluteLocation absLoc = vertex.getLocation().getAbsoluteLocation();
			double lat = roundToMax(absLoc.getLatitude());
			double lon = roundToMax(absLoc.getLongitude());
			double alt = roundToMax(absLoc.getAltitude());
			 rawConsumer.createEntity("AbsoluteLocations")
			.properties(OProperties.int32("Vertex_ID", vertex.getId()))
			.properties(OProperties.double_("latitude", lat))
			.properties(OProperties.double_("longitude", lon))
			.properties(OProperties.double_("altitude", alt))
			//NOTE: Yes, changing to double did the trick
			//.properties(OProperties.decimal("latitude", absLoc.getLatitude()))
			//.properties(OProperties.decimal("longitude", absLoc.getLongitude()))
			//.properties(OProperties.decimal("altitude", absLoc.getAltitude()))
			.execute();
		}		
		
		return vertexID;
	}
	
	private static double roundToMax(double val)
	{
		final int maxLength = 16;
		String strVal = Double.toString(val);
		int length = strVal.length();
		if (length <= maxLength)
			return val;
		else
		{
			return Double.valueOf(strVal.substring(0, maxLength - 1));
		}			
	}
	
	@Override
	public void addVertexToGraveYard(int vertexId, int buildingId) {
		//Instead of updating the building-id, we are calling the service operation
		//addVertexToGraveyard(vertexId, buildingId) on the JsonWebClient instead.
		/*
		OEntity result = rawConsumer.getEntity("Vertices", vertexId).execute();
		if (result != null)
		{
			rawConsumer.updateEntity(result)
			.properties(OProperties.int32("Building_ID", 18)) //18 is the ID of the VertexGraveYard
			.execute();
		}
		*/
	}
	
	@Override
	public void deleteEdge(int edgeID) {
		rawConsumer.deleteEntity("Edges", edgeID).execute();
	}

	@Override
	public void deleteEdge(int source_vertexID, int destination_vertexID) {
		//Note: We assume that the edge on the server is uni-directional, i.e., there is only entry.
		String query1 = 
			"(vertexOrigin eq " + source_vertexID + " and vertexDestination eq " + destination_vertexID + ")";
		String query2 = 
			"(vertexOrigin eq " + destination_vertexID + " and vertexDestination eq " + source_vertexID + ")";
		Enumerable<OEntity> result = rawConsumer.getEntities("Edges")
			.filter(query1 + " or " + query2)
			.top(1)
			.execute();		
		
		if (result != null)
		{
			for (OEntity oEdge : result)
				rawConsumer.deleteEntity(oEdge).execute();
		}		
	}
	
	@Override
	public Building downloadRadioMap(int buildingId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getBuildingIdFromMacs(List<String> macs) {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public Iterable<Building> getShallowBuildings() throws IOException
	{
		ArrayList<Building> result = new ArrayList<Building>();
		
		Enumerable<OEntity> oBuildings = rawConsumer
			.getEntities(ODataProperties.Building.ENTITY_NAME)
			.execute();
		
		for (OEntity oBuilding : oBuildings)
		{
			Building curBuilding = new Building();
			int buildingID 		= oBuilding.getProperty("ID", Integer.class).getValue(); 
			String buildingName = oBuilding.getProperty("Building_Name", String.class).getValue();
			double lat			= oBuilding.getProperty("Lat", double.class).getValue();
			//BigDecimal lat		= oBuilding.getProperty("Lat", BigDecimal.class).getValue();
			double lon			= oBuilding.getProperty("Lon", double.class).getValue();
			//BigDecimal lon		= oBuilding.getProperty("Lon", BigDecimal.class).getValue();
			String country		= oBuilding.getProperty("Country", String.class).getValue();
			String postalCode	= oBuilding.getProperty("Postal_Code", String.class).getValue();
			String address		= oBuilding.getProperty("Max_Address", String.class).getValue();
			String ifcUrl		= oBuilding.getProperty("Ifc_Url", String.class).getValue();
			String url 			= oBuilding.getProperty("Url", String.class).getValue();
			
			curBuilding.setBuildingID(buildingID);
			curBuilding.setName(buildingName);
			curBuilding.setLatitude(lat);
			//curBuilding.setLatitude(lat.doubleValue());
			curBuilding.setLongitude(lon);
			curBuilding.setCountry(country);
			curBuilding.setPostalCode(postalCode);
			curBuilding.setMaxAddress(address);
			curBuilding.setIfcUrl(ifcUrl);
			curBuilding.setUrl(url);
			
			//fetch and save MacInfo for the building
			ArrayList<String> macs = new ArrayList<String>();
			Enumerable<OEntity> oMacs = rawConsumer.getEntities("Building_MacInfos")
				.filter("Building_ID eq " + buildingID)
				.execute();			
			for (OEntity oMac : oMacs)
			{
				macs.add(oMac.getProperty("Mac", String.class).getValue());
			}
			curBuilding.setPermissableAPs(macs);
			
			//TODO: Fetch and save building_floors for the building
			Enumerable<OEntity> oFloors = rawConsumer.getEntities("Building_Floors")
				.filter("Building_ID eq " + buildingID)
				.execute();			
			Building_Floor curFloor;
			for (OEntity oFloor : oFloors)
			{
				curFloor = new Building_Floor();
				curFloor.setFloorNumber(oFloor.getProperty("Number", Integer.class).getValue());
				curFloor.setFloorName(oFloor.getProperty("Name", String.class).getValue());
				curFloor.setID(oFloor.getProperty("ID", Integer.class).getValue());
				
				curBuilding.addFloor(curFloor);				
			}			
			
			result.add(curBuilding);
		}
		return result;		
	}


	@Override
	public void updateBuilding(Building b) {
		//consumer.updateSymbolicLocationEntity(input);
		
		OEntity result = rawConsumer.getEntity("Buildings", b.getBuildingID()).execute();
		//TODO: result is null if not found. Handle this situation	
		rawConsumer.updateEntity(result)
			.properties(OProperties.string("Building_Name", b.getName()))
			.properties(OProperties.string("Ifc_Url", b.getIfcUrl()))
			.properties(OProperties.double_("Lat", roundToMax(b.getLatitude())))
			//.properties(OProperties.decimal("Lat", b.getLatitude()))
			.properties(OProperties.double_("Lon", roundToMax(b.getLongitude())))
			//.properties(OProperties.decimal("Lon", b.getLongitude()))
			.properties(OProperties.string("Max_Address", b.getMaxAddress()))
			.properties(OProperties.string("Postal_Code", b.getPostalCode()))
			.properties(OProperties.string("Country", b.getCountry()))
			.properties(OProperties.string("Url", b.getUrl()))	
			.execute();
	}

	@Override
	public void updateBuilding_Floor(Building_Floor input) {
		OEntity result = rawConsumer.getEntity("Building_Floors", input.getID()).execute();
		if (result != null)
		{
			rawConsumer.updateEntity(result)
			.properties(OProperties.int32("Number", input.getFloorNumber()))
			.properties(OProperties.string("Name", input.getFloorName()))
			.execute();
		}
	}

	@Override
	public void updateEdge(Edge input) {
		OEntity result = rawConsumer.getEntity("Edges", input.getId()).execute();
		if (result != null)
		{
			//We do not change the buildingID nor the endpoint ids
			rawConsumer.updateEntity(result)
			.properties(OProperties.boolean_("directional", input.isDirectional()))
			.properties(OProperties.boolean_("is_stair", input.isStair()))
			.properties(OProperties.boolean_("is_elevator", input.isElevator()))			
			.execute();
		}		
	}
	
	@Override
	public void updateSymbolicLocation(SymbolicLocation input) {
		//consumer.updateSymbolicLocationEntity(input);
		
		OEntity result = rawConsumer.getEntity("SymbolicLocations", input.getId()).execute();
		if (result != null)
		{
			rawConsumer.updateEntity(result)
			.properties(OProperties.string("title", input.getTitle()))
			.properties(OProperties.string("description", input.getDescription()))
			.properties(OProperties.string("url", input.getUrl()))
			.properties(OProperties.boolean_("is_entrance", input.isEntrance()))
			.properties(OProperties.int32("info_type", input.getType().ordinal()))					
			.execute();
		}
	}
	
	@Override
	public void updateVertex(Vertex input) {
		
	}

	@Override
	public boolean UploadMeasurement(WifiMeasurement input, Vertex newParam) {
		// TODO Auto-generated method stub
		return false;
	}
}
