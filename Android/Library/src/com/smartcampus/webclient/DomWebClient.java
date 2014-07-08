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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.smartcampus.indoormodel.AbsoluteLocation;
import com.smartcampus.indoormodel.Building;
import com.smartcampus.indoormodel.Building_Floor;
import com.smartcampus.indoormodel.SymbolicLocation;
import com.smartcampus.indoormodel.SymbolicLocation.InfoType;
import com.smartcampus.indoormodel.graph.Edge;
import com.smartcampus.indoormodel.graph.Vertex;
import com.smartcampus.wifi.Histogram;
import com.smartcampus.wifi.WifiMeasurement;

public class DomWebClient implements IWebClient {

	//Used if an unexpected element is encountered during traversal of the DOM
	//(Actually, it is not used currently, as the structure of the RSS is very stable)
	public class UnexpectedNodeException extends Exception
	{
		private static final long serialVersionUID = 1L;
		private String expectedNodeName, actualNodeName;		
		
		public UnexpectedNodeException(String expectedNodeName, String actualNodeName)
		{
			this.expectedNodeName = expectedNodeName;
			this.actualNodeName = actualNodeName;
		}
		
		@Override
		public String getMessage()
		{
			return "Expected Node: " + this.expectedNodeName + ". Actual Node: " + this.actualNodeName;
		}
	}

	/* Used to open an http connection to a given url
     * 1) To open a connection to a server, you first create an instance of the URL class and initialize it with the URL of the server.
     * 2) When the connection is established, you pass this connection to an URLConnection object.
     * 3) You then verify whether the protocol is indeed HTTP; if not you will throw an IOException. 
     * 4) The URLConnection object is then cast into an HttpURLConnection object and you set the various properties of the HTTP connection.
     * 5) Next, you connect to the HTTP server and get a response from the server. 
     * 6) If the response code is HTTP_OK, you then get the InputStream object from the connection so that you can begin to read incoming data from the server.
     * 7) The function then returns the InputStream object obtained. 
     * */
    private static InputStream OpenHttpConnection(String urlString) 
    	throws IOException
    {
        InputStream in = null;
        int response = -1;
               
        URL url = new URL(urlString); 
        URLConnection conn = url.openConnection();
                 
        if (!(conn instanceof HttpURLConnection))                     
            throw new IOException("Not an HTTP connection");
        
        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect(); 

            response = httpConn.getResponseCode();                 
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();                                 
            }                     
        }
        catch (Exception ex)
        {
            throw new IOException("Error connecting");            
        }
        return in;     
    }

	public boolean addAbsoluteLocation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int addBuilding(Building b) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int addBuilding_Floor(Building_Floor bf, Building b) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addBuilding_Macs(List<String> newMacs, Building b) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int addEdge(Edge input, Building b) {
		// TODO Auto-generated method stub
		return 0;
	}

	private void addFingerprints(Node n, Vertex v)
	{
		NodeList fingerprints = getFingerprintBranch(n);
		
		Node current;
		for (int i = fingerprints.getLength() - 1; i >= 0; i--)
		{
			current = fingerprints.item(i);
			if (isWifiMeasurement(current))
			{
				WifiMeasurement meas = createWifiMeasurement(current);
				v.addFingerprint(meas);
			}
		}		
	}
	
	private void addHistograms(Node n, WifiMeasurement m)
	{
		NodeList histograms = getHistogramBranch(n);
		
		Node current;
		for (int i = 0; i < histograms.getLength(); i++)
		{
			current = histograms.item(i);
			if (isHistogram(current))
			{
				Histogram h = createHistogram(current);
				m.setHistogram(h);
			}
		}		
	}
	
	@Override
	public int addMeasurement(Building building, Vertex vertex,
			WifiMeasurement input) {
		//todo
		return -1;
	}
	
	
	public int addMeasurement(WifiMeasurement input, Vertex v) {
		// TODO Auto-generated method stub
		return 0;
	}
	   
    public boolean addSymbolicLocation(SymbolicLocation input) {
		// TODO Auto-generated method stub
		return false;
	}

    
   
	@Override
	public int addSymbolicLocation(SymbolicLocation input, Vertex v) {
		// TODO Auto-generated method stub
		return -1;
	}
	
    public int addVertex(Vertex input) {
		// TODO Auto-generated method stub
		return -1;
	}
    
    @Override
	public int addVertex(Vertex input, Building b) {
		// TODO Auto-generated method stub
		return 0;
	}
    
    @Override
	public void addVertexToGraveYard(int vertexId, int buildingId) {
		
	}
    
	//NOTE: NEW
    private void addVertices(Node n, Building b)
    {
    	NodeList vertices = getVertexBranch(n);
		
		Node current;
		for (int i = vertices.getLength() - 1; i >= 0; i--)
		{
			current = vertices.item(i);
			if (isVertex(current))
			{
				Vertex v = createVertex(current);
				b.getGraphModel().addVertex(v);
			}
		}	
    }
	
	private boolean containsContent(Node input)
	{
		boolean result = false;
		
		if (input.getNodeType() == Node.ELEMENT_NODE)
		{
			NodeList anyContent = ((Element)input).getElementsByTagName("content");
			result = anyContent.getLength() > 0; //is there content?
		}
		return result;
	}
	
	//Precondition: node = 'link' (under vertex) 
	private AbsoluteLocation createAbsoluteLocation(Node absPosEntry)
	{
		//if (!absPosEntry.getLocalName().equalsIgnoreCase("link"))
		//	throw new UnexpectedNodeException("link", absPosEntry.getNodeName());
			
		String latitude  = getPropertyValue(absPosEntry, ODataProperties.AbsoluteLocation.LATITUDE);
		String longitude = getPropertyValue(absPosEntry, ODataProperties.AbsoluteLocation.LONGITUDE);
		String altitude  = getPropertyValue(absPosEntry, ODataProperties.AbsoluteLocation.ALTITUDE);
		
		//We don't perform exception handling. These values MUST be present, otherwise bug.
		return new AbsoluteLocation(
				Double.parseDouble(latitude), 
				Double.parseDouble(longitude),
				Double.parseDouble(altitude));
	}
	
	/*
	private Edge createEdge(Node edgeEntry, IGraph g)
	{
		String strId			= getPropertyValue(edgeEntry, ODataProperties.Edge.ID);
		String strOrigin 		= getPropertyValue(edgeEntry, ODataProperties.Edge.VERTEX_ORIGIN);
		String strDestination 	= getPropertyValue(edgeEntry, ODataProperties.Edge.VERTEX_DESTINATION);
		String strIsDirectional = getPropertyValue(edgeEntry, ODataProperties.Edge.IS_DIRECTIONAL);
		String strIsElevator 	= getPropertyValue(edgeEntry, ODataProperties.Edge.IS_ELEVATOR);
		String strIsStair 		= getPropertyValue(edgeEntry, ODataProperties.Edge.IS_STAIR);
		
		int originId 	  		= Integer.parseInt(strOrigin);
		int destinationId 		= Integer.parseInt(strDestination);
		int id            		= Integer.parseInt(strId);
		boolean isDirectional 	= strIsDirectional == null ? false : Boolean.parseBoolean(strIsDirectional);
		boolean isElevator      = strIsElevator    == null ? false : Boolean.parseBoolean(strIsElevator);
		boolean isStair			= strIsStair       == null ? false : Boolean.parseBoolean(strIsStair);
		
		Edge result = new Edge(g.getVertexById(originId), g.getVertexById(destinationId));
		result.setId(id);
		result.setDirectional(isDirectional);
		result.setElevator(isElevator);
		result.setStair(isStair);
		
		return result;
	}
	*/
	/*
	private Iterable<Edge> createEdges(Node current, Building result) {
		return null;
	}
	*/
	    
    private Building createBuilding(Node buildingEntry) throws UnexpectedNodeException
	{
		Building result = new Building();
		
		NodeList building_children = buildingEntry.getChildNodes();
		Node current;
		Iterable<ShallowEdge> shallowEdges = new ArrayList<ShallowEdge>();
		//build the vertices from the received xml file
		for (int i = 0; i < building_children.getLength(); i++)
		{
			current = building_children.item(i);
			if (isVertexBranch(current))
			{
				addVertices(current, result);
			}
			else if (isEdgeBranch(current))
			{
				shallowEdges = createShallowEdges(current, result);
			}
			else if (isProperties(current)) //content 
			{
				final String id 	 = getPropertyValue(current, ODataProperties.Building.ID);
				final String name 	 = getPropertyValue(current, ODataProperties.Building.NAME);
				final String ifc_url = getPropertyValue(current, ODataProperties.Building.IFC_URL);
				final String strLat	 = getPropertyValue(current, ODataProperties.Building.LATITUDE);
				final String strLon	 = getPropertyValue(current, ODataProperties.Building.LONGITUDE);
				final String country = getPropertyValue(current, ODataProperties.Building.COUNTRY);
				final String postal	 = getPropertyValue(current, ODataProperties.Building.POSTAL_CODE);
				final String max_add = getPropertyValue(current, ODataProperties.Building.MAX_ADDRESS);
				final String url 	 = getPropertyValue(current, ODataProperties.Building.URL);
	    		
				result.setBuildingID(Integer.parseInt(id));
				result.setName(name);	
				result.setIfcUrl(ifc_url);
				double lat = strLat == null ? -1 : Double.parseDouble(strLat);
				result.setLatitude(lat);
				double lon = strLon == null ? -1 : Double.parseDouble(strLon);
				result.setLongitude(lon);
				result.setCountry(country);
				result.setPostalCode(postal);
				result.setMaxAddress(max_add);
				result.setUrl(url);
			}
		}
		//Turn the shallow edges into 'real' edges that become part of the graph model
		ShallowEdge.addUndirectionalEdges(result.getGraphModel(), shallowEdges);
		return result;
	}
		
	private Histogram createHistogram(Node currentNode)
	{
		String mac     = getPropertyValue(currentNode, ODataProperties.Histogram.MAC);
		String value   = getPropertyValue(currentNode, ODataProperties.Histogram.VALUE);
		String count   = getPropertyValue(currentNode, ODataProperties.Histogram.COUNT);
		
		return new Histogram(-1, mac, Integer.parseInt(value), Integer.parseInt(count));		
	}
	
	private Iterable<Building> createShallowBuildings(Node root)
	{
		//TODO: 
		return null;
	}
	
	private ShallowEdge createShallowEdge(Node edgeEntry)
	{
		/*
		 * <d:ID m:type="Edm.Int32">2</d:ID>
              <d:directional m:type="Edm.Boolean">false</d:directional>
              <d:vertexOrigin m:type="Edm.Int32">1</d:vertexOrigin>
              <d:vertexDestination m:type="Edm.Int32">2</d:vertexDestination>
              <d:Building_ID m:type="Edm.Int32">1</d:Building_ID>
              <d:is_stair m:type="Edm.Boolean" m:null="true" />
              <d:is_elevator m:type="Edm.Boolean" m:null="true" />
		 */
		String strId  			= getPropertyValue(edgeEntry, ODataProperties.Edge.ID);
		String strVertexOrigin 	= getPropertyValue(edgeEntry, ODataProperties.Edge.VERTEX_ORIGIN);
		String strVertexDest	= getPropertyValue(edgeEntry, ODataProperties.Edge.VERTEX_DESTINATION);
		String strDirectional 	= getPropertyValue(edgeEntry, ODataProperties.Edge.IS_DIRECTIONAL);
		String strIsStair 		= getPropertyValue(edgeEntry, ODataProperties.Edge.IS_STAIR);
		String strIsElevator 	= getPropertyValue(edgeEntry, ODataProperties.Edge.IS_ELEVATOR);
		
		int id 					= Integer.parseInt(strId);
		int vertexOrigin 		= Integer.parseInt(strVertexOrigin);
		int vertexDestination 	= Integer.parseInt(strVertexDest);
		boolean isDirectional 	= strDirectional == null ? false : Boolean.parseBoolean(strDirectional);
		boolean isStair 		= strIsStair == null 	 ? false : Boolean.parseBoolean(strIsStair);
		boolean isElevator 		= strIsElevator == null  ? false : Boolean.parseBoolean(strIsElevator);
		
		ShallowEdge result = new ShallowEdge(vertexOrigin, vertexDestination);
		result.setId(id);
		result.setDirectional(isDirectional);
		result.setStair(isStair);
		result.setElevator(isElevator);
		return result;
	}

	private Iterable<ShallowEdge> createShallowEdges(Node n, Building b)
    {
    	ArrayList<ShallowEdge> result = new ArrayList<ShallowEdge>();
    	
    	NodeList edges = getEdgeBranch(n);
		
		Node current;
		for (int i = 0; i < edges.getLength(); i++)
		{
			current = edges.item(i);
			if (isEdge(current))
			{
				ShallowEdge e = createShallowEdge(current);
				result.add(e);
			}
		}
		
		return result;
    }

	//Precondition: node = 'link' (under vertex) 
	private SymbolicLocation createSymbolicLocation(Node symLocEntry) 
	{
		//if (!absPosEntry.getLocalName().equalsIgnoreCase("link"))
		//	throw new UnexpectedNodeException("link", absPosEntry.getNodeName());
		SymbolicLocation result = null;
		final String na = "N/A";
		
		if (containsContent(symLocEntry))
		{		
			String title       = getPropertyValue(symLocEntry, ODataProperties.SymbolicLocation.TITLE);
			String description = getPropertyValue(symLocEntry, ODataProperties.SymbolicLocation.DESCRIPTION);
			String url         = getPropertyValue(symLocEntry, ODataProperties.SymbolicLocation.URL);
			String strId       = getPropertyValue(symLocEntry, ODataProperties.SymbolicLocation.ID);
			String strEntrance = getPropertyValue(symLocEntry, ODataProperties.SymbolicLocation.IS_ENTRANCE);
			String strInfo     = getPropertyValue(symLocEntry, ODataProperties.SymbolicLocation.INFO_TYPE);
			
			title = title != null ? title : na;
			description = description != null ? description : na;
			url = url != null ? url : na;
			int id = Integer.parseInt(strId);
			boolean isEntrance = strEntrance == null ? false : Boolean.parseBoolean(strEntrance);
			int infoVal = 0;
			try
			{
				infoVal = strInfo == null ? 0 : Integer.parseInt(strInfo);
			}
			catch (NumberFormatException ex) { }
			
			//remember to set id in vertex
			result = new SymbolicLocation(id, title, description, url); 
			result.setEntrance(isEntrance);
			result.setType(InfoType.getValue(infoVal));
		}
		return result;
	}
	
	// .../entry
	private Vertex createVertex(Node vertexEntry)
	{
		Vertex result = new Vertex();
		
		NodeList vertex_children = vertexEntry.getChildNodes();
		Node current;
		//build the vertices from the received xml file
		for (int i = 0; i < vertex_children.getLength(); i++)
		{
			current = vertex_children.item(i);
			if (isAbsoluteLocation(current))
			{
				AbsoluteLocation absLoc = createAbsoluteLocation(current);
				result.getLocation().setAbsoluteLocation(absLoc);
			}
			else if (isSymbolicLocation(current))
			{
				SymbolicLocation symLoc = createSymbolicLocation(current);
				result.getLocation().setSymbolicLocation(symLoc);
			}
			else if (isWifiMeasurementBranch(current))
			{
				addFingerprints(current, result);						
			}
			//NOTE: It is important that this check comes last!!!
			//as, e.g., d:ID can also be found in AbsoluteLocation
			//NOTE: Not all IDs have been set
			else if (isProperties(current)) //content 
			{
				String id = getPropertyValue(current, ODataProperties.Vertex.ID);
				result.setId(Integer.parseInt(id));	
			}
		}
			
		return result;
	}
	
	private WifiMeasurement createWifiMeasurement(Node currentNode)
	{
		WifiMeasurement result = new WifiMeasurement();
		
		NodeList childNodes = currentNode.getChildNodes();
		Node child;
				
		for (int i = 0; i < childNodes.getLength(); i++)
		{			
			child = childNodes.item(i);
		
			if (isHistogramBranch(child))
			{
				addHistograms(child, result);				
			}
			else if (isProperties(child))
			{
				String startMeasStr  = getPropertyValue(child, ODataProperties.WifiMeasurement.MEAS_TIME_START);
				String endMeasStr    = getPropertyValue(child, ODataProperties.WifiMeasurement.MEAS_TIME_END);
				
				Date startMeas = null;
				Date endMeas = null;
				if (startMeasStr != null)
					startMeas = decodeStringDate(startMeasStr); //new Date(Date.parse(startMeasStr));
				if (endMeasStr != null)
					endMeas = decodeStringDate(endMeasStr); //new Date(Date.parse(endMeasStr));
				
				result.setMeasTimeStart(startMeas);
				result.setMeasTimeEnd(endMeas);
			}
		}		
			
		return result;
	}	
	
	/*
	 * Returns a date from a string representation of a date. 
	 * The string has the following format: 2011-09-16T00:00:00
	 * 2 0 1 1 - 0 9 - 1 6 T  0  0  :  0  1  :  1  0  <- entries 
	 * 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 <-index no.'s
	*/
    private Date decodeStringDate(String input)
	{
		if (input == null)
			return null;		
		
		int year   = Integer.parseInt(input.substring(0, 4));
		int month  = parseAndRemoveLeadingZero(input.substring(5, 7));
		int day    = parseAndRemoveLeadingZero(input.substring(8, 10));
		int hour   = parseAndRemoveLeadingZero(input.substring(11, 13));
		int minute = parseAndRemoveLeadingZero(input.substring(14, 16));
		int second = parseAndRemoveLeadingZero(input.substring(17, 19));
		
		//months are 0-11
		return new Date(year, month - 1, day, hour, minute, second);		
	}	
	
	@Override
	public void deleteEdge(int edgeID) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void deleteEdge(int source_vertexID, int destination_vertexID) {
		// TODO Auto-generated method stub
		
	}
	
	public Building downloadRadioMap(int buildingId) {
		//TODO: Throw (or rethrow) exceptions - then take appropriate action at call-site)
		Building b = downloadRadioMapFromBuildingId(buildingId);
		return b;		
	}		
	
	/*
	 * The remainder of this class is used to download a building in XML format and
	 * the parse the input of a constructed Document Object Model. 
	 */
	private Building downloadRadioMapFromBuildingId(int buildingId)
	{
    	Building result = null;
    	
    	//RemoveNonCollectiveMeasurements returns a feed
    	String url = ConnectionInfo.SMARTCAMPUS_SERVICE_ROOT_URI + "RemoveNonCollectiveMeasurements?building_id=" + buildingId + "&$expand=Edges,Vertices,Vertices/AbsoluteLocations,Vertices/SymbolicLocations,Vertices/WifiMeasurements,Vertices/WifiMeasurements/Histograms";
        
    	//Buildings(id) returns an entry (i.e., NOT a feed)
        
		InputStream in = null;
        try {
            in = OpenHttpConnection(url);
            
            Document doc = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            
            try {
                db = dbf.newDocumentBuilder();
                doc = db.parse(in);
            }
            catch (ParserConfigurationException e) { }
            catch (SAXException e) { }        
            
            Node root = doc.getFirstChild();
            
            if (isFeed(root))
            	root = getBuildingRootForFeed(root);
            result = createBuilding(root);
            
        
        } catch (Exception e1)
        {
        	e1.printStackTrace();        	
        }
        return result;
    }
	
	public int getBuildingIdFromMacs(List<String> macs) {
		// TODO Auto-generated method stub
		return -1;
	}
	
	/**
	 * This is used to find the building root when the xml is returned as a feed (root = feed) (IQueryable) rather 
	 * than when the building is returned as an entry (e.g., /Buildings(4)), i.e., a specific Building
	 * 
	 * 1) Feed-example result  : uri/RadioMapService.svc/RemoveNonCollectiveMeasurements?building_id=4
	 * - Root is 'feed'. The building is found under the 'entry' node
	 * 2) Entry-example result : uri/RadioMapService.svc/Buildings(4)
	 * @param n
	 * @return
	 */
	private Node getBuildingRootForFeed(Node n)
	{
		//first level
		Node entryElement = n.getFirstChild();
        while (!isElement(entryElement, "entry"))
        	entryElement = entryElement.getNextSibling();
        
        return entryElement;
	}
	
	private Node getDomRoot(String url) throws FactoryConfigurationError {
		InputStream in = null;
		Node root = null;
		try {
            in = OpenHttpConnection(url);
            
            Document doc = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            
            try {
                db = dbf.newDocumentBuilder();
                doc = db.parse(in);
            }
            catch (ParserConfigurationException e) { }
            catch (SAXException e) { }        
            
            root = doc.getFirstChild();                       
        
        } catch (Exception e1)
        {
        	e1.printStackTrace();        	
        }
		return root;
	}
	
	private NodeList getEdgeBranch(Node n)
	{
		return getNestedEntries(n);
	}
	
	private NodeList getFingerprintBranch(Node n)
	{
		return getNestedEntries(n);
	}		
	
	private NodeList getHistogramBranch(Node n)
	{
		return getNestedEntries(n);
	}	
	
	private NodeList getNestedEntries(Node n)
	{
		NodeList result = null;
		
        //first level
		Node inlineElement = n.getFirstChild();
        while (!isElement(inlineElement, "m:inline"))
        	inlineElement = inlineElement.getNextSibling();
        
        //second level
        Node feedElement = inlineElement.getFirstChild();
        while (!isElement(feedElement, "feed"))
        	feedElement = feedElement.getNextSibling();
        
        result = feedElement.getChildNodes();
        
        return result;
	}
	
	private String getPropertyValue(Node currentNode, String propertyName)
	{
		String result = null;
		NodeList n = ((Element)currentNode).getElementsByTagName(propertyName);
		//Properties are in the following form: <d:id>1</d:id>. 
		//We get the first child of the nodeList above (as each property has a unique name, so there is only one child)
		//Then we get the first child of this - i.e., the text node - and retrieve the node value
		if (n != null)
			if (n.getLength() > 0)
				if (n.item(0).getFirstChild() != null)
					result = n.item(0).getFirstChild().getNodeValue();
		
		return result;
	}
	
	@Override
	public Iterable<Building> getShallowBuildings()
	{
		Iterable<Building> result = null; 
        String url = ConnectionInfo.SMARTCAMPUS_SERVICE_ROOT_URI + "Buildings?$expand=Building_MacInfos,Building_Floors";
       			
		Node root = getDomRoot(url);
      //printResult(result);
		if (root != null)
			result = createShallowBuildings(root); 
        return result;
	}
	
	private NodeList getVertexBranch(Node n)
    {
    	return getNestedEntries(n);
    }
	
	private boolean isAbsoluteLocation(Node input)
	{
		return isElement(input, "link", "AbsoluteLocations");
	}
	
	private boolean isEdge(Node possibleEdge)
	{
		return isElement(possibleEdge, "entry"); 
	}
		
	private boolean isEdgeBranch(Node input)
	{
		return isElement(input, "link", "Edges");
	}
	
	private boolean isElement(Node input, String nodeName)
	{
		return input.getNodeType() == Node.ELEMENT_NODE && input.getNodeName().equalsIgnoreCase(nodeName);		
	}

	/**
	 * Helper method to determine if we have a particular node. 
	 * @param input
	 * @param nodeName
	 * @param titleVal
	 * @return
	 */
	private boolean isElement(Node input, String nodeName, String titleVal)
	{
		boolean result = false;
		if (input.getNodeType() == Node.ELEMENT_NODE && input.getNodeName().equalsIgnoreCase(nodeName))
		{
			Node titleAtr = input.getAttributes().getNamedItem("title");
			if (titleAtr.getNodeValue().equalsIgnoreCase(titleVal))
				result = true;
		}
		return result;
	}

	private boolean isFeed(Node input)
	{
		boolean result = false;
		if (input.getNodeType() == Node.ELEMENT_NODE && input.getNodeName().equalsIgnoreCase("feed"))
		{
			result = true;
		}
		return result;
	}

	private boolean isHistogram(Node input)
	{
		return isElement(input, "entry");
	}

	private boolean isHistogramBranch(Node input)
	{
		return isElement(input, "link", "Histograms");
	}
	
	private boolean isProperties(Node input)
	{
		return isElement(input, "content");
	}

	private boolean isSymbolicLocation(Node input)
	{
		return isElement(input, "link", "SymbolicLocations");
	}

	private boolean isVertex(Node possibleVertex)
	{
		return isElement(possibleVertex, "entry"); 
	}

	private boolean isVertexBranch(Node input)
	{
		return isElement(input, "link", "Vertices");
	}

	private boolean isWifiMeasurement(Node input)
	{
		return isElement(input, "entry");
	}

	private boolean isWifiMeasurementBranch(Node input)
	{
		return isElement(input, "link", "WifiMeasurements");
	}

	/*
     * A given part of a date string (month, minute, etc.) may have a leading zero which this method removes.
     */
	private int parseAndRemoveLeadingZero(String input)
	{
		int result;
		if (input.charAt(0) == '0')
			result = Integer.parseInt(input.substring(1));
		else
			result = Integer.parseInt(input);
		return result;
	}

	@Override
	public void updateBuilding(Building b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateBuilding_Floor(Building_Floor input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateEdge(Edge input) {
		// TODO Auto-generated method stub
		
	}

	public void updateSymbolicLocation(SymbolicLocation input) {
		// TODO Auto-generated method stub		
	}

	public void updateVertex(Vertex input) {
		
	}

	public boolean UploadMeasurement(WifiMeasurement input, Vertex newParam) {
		// TODO Auto-generated method stub
		return false;
	}
}
