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

package com.smartcampus.indoormodel.graph;

import java.util.ArrayList;
import java.util.List;

import com.smartcampus.indoormodel.AbsoluteLocation;
import com.smartcampus.indoormodel.Building;
import com.smartcampus.indoormodel.SymbolicLocation;
import com.smartcampus.wifi.WifiMeasurement;

public class MockGraph {
	
	public static Building getDummyBuilding()
	{
		Building building = new Building();
		
		int[] floors = new int[3];
		floors[0] = 1;
		floors[1] = 2;
		floors[2] = 3;
		building.addMapUrl(1, "http://www.streamspin.com/userlogos/maps/2_633826119465312500.png");
		building.addMapUrl(2, "http://www.streamspin.com/userlogos/maps/9161_633875064519153750.png");
	    building.setStories(3); //probably about to be changed
		building.setBuildingID(1994239428);
	    building.SetName("selma");
	    	
	    building.setPermissableAPs(getPermissableAPs());
	    
	    building.setGraphModel(MockGraph.getDummyGraph());
	    
	    return building;
	}
	
	public static IGraph getDummyGraph()
	{		
		IGraph graph = new DictionaryGraph();
		
		Vertex v13 = new Vertex(1,new AbsoluteLocation(57.012345, 9.990626, 1));
		WifiMeasurement wm13 = new WifiMeasurement();
		int ss13 = -62;
		for (String mac : MockGraph.getPermissableAPs())
			wm13.addValue(mac, ss13--);
		v13.addFingerprint(wm13);
		SymbolicLocation symLoc13 = new SymbolicLocation(1,"Room 3.2.59", "Lone Leth Thomsen's office ", "http://www.cs.aau.dk/~lone/");
		v13.getLocation().setSymbolicLocation(symLoc13);
		graph.addVertex(v13);
		
		Vertex v12 = new Vertex(2,new AbsoluteLocation(57.012054, 9.991484,0 ));
		WifiMeasurement wm12 = new WifiMeasurement();
		int ss12 = -62;
		for (String mac : MockGraph.getPermissableAPs())
			wm12.addValue(mac, ss12--);
		v12.addFingerprint(wm12);
		SymbolicLocation symLoc12 = new SymbolicLocation(2,"Software Innovation Lab", "Where innovative solutions are devised", "http://www.sirl.dk/");
		v12.getLocation().setSymbolicLocation(symLoc12);
		graph.addVertex(v12);
		
		Vertex v11 = new Vertex(3,new AbsoluteLocation(57.012054, 9.991253, 0));
		WifiMeasurement wm11 = new WifiMeasurement();
		int ss11 = -62;
		for (String mac : MockGraph.getPermissableAPs())
			wm11.addValue(mac, ss11--);
		v11.addFingerprint(wm11);
		SymbolicLocation symLoc11 = new SymbolicLocation(3,"Embedded Lab", "Activities: Toys'R'Us ", " ");
		v11.getLocation().setSymbolicLocation(symLoc11);
		graph.addVertex(v11);
		
		Vertex v10 = new Vertex(4,new AbsoluteLocation(57.012059, 9.991075,0  ));
		WifiMeasurement wm10 = new WifiMeasurement();
		int ss10 = -62;
		for (String mac : MockGraph.getPermissableAPs())
			wm10.addValue(mac, ss10--);
		v10.addFingerprint(wm10);
		SymbolicLocation symLoc10 = new SymbolicLocation(4,"Usability Lab", "Houses facilities for conducting usability tests ", " ");
		v10.getLocation().setSymbolicLocation(symLoc10);
		graph.addVertex(v10);
		
		Vertex v9 = new Vertex(5,new AbsoluteLocation(57.012103, 9.99097,0 ));
		WifiMeasurement wm9 = new WifiMeasurement();
		int ss9 = -62;
		for (String mac : MockGraph.getPermissableAPs())
			wm9.addValue(mac, ss9--);
		v9.addFingerprint(wm9);
		SymbolicLocation symLoc9 = new SymbolicLocation(5,"Robotics Lab", "Where Lego Mindstorm comes alive ", " ");
		v9.getLocation().setSymbolicLocation(symLoc9);
		graph.addVertex(v9);
		
		Vertex v8 = new Vertex(6,new AbsoluteLocation(57.012323, 9.99107, 0 ));
		WifiMeasurement wm8 = new WifiMeasurement();
		int ss8 = -62;
		for (String mac : MockGraph.getPermissableAPs())
			wm8.addValue(mac, ss8--);
		v8.addFingerprint(wm8);
		SymbolicLocation symLoc8 = new SymbolicLocation(6,"The Canteen", " ", " ");
		v8.getLocation().setSymbolicLocation(symLoc8);
		graph.addVertex(v8);
		
		Vertex v7 = new Vertex(7,new AbsoluteLocation(57.01251, 9.990834, 1));
		WifiMeasurement wm7 = new WifiMeasurement();
		int ss7 = -62;
		for (String mac : MockGraph.getPermissableAPs())
			wm7.addValue(mac, ss7--);
		v7.addFingerprint(wm7);
		SymbolicLocation symLoc7 = new SymbolicLocation(7,"Meeting Room (Cluster 3)", " ", " ");
		v7.getLocation().setSymbolicLocation(symLoc7);
		graph.addVertex(v7);
		
		Vertex v6 = new Vertex(8,new AbsoluteLocation(57.012394, 990839, 1));
		WifiMeasurement wm6 = new WifiMeasurement();
		int ss6 = -62;
		for (String mac : MockGraph.getPermissableAPs())
			wm6.addValue(mac, ss6--);
		v6.addFingerprint(wm6);
		SymbolicLocation symLoc6 = new SymbolicLocation(8,"Common Room (Cluster 3)", "Where DPT'ers get together..", " ");
		v6.getLocation().setSymbolicLocation(symLoc6);
		graph.addVertex(v6);
		
		Vertex v5 = new Vertex(9,new AbsoluteLocation(57.012347, 9.99076, 1 ));
		WifiMeasurement wm5 = new WifiMeasurement();
		int ss5 = -62;
		for (String mac : MockGraph.getPermissableAPs())
			wm5.addValue(mac, ss5--);
		v5.addFingerprint(wm5);
		SymbolicLocation symLoc5 = new SymbolicLocation(9,"Kitchen (Cluster 3)", "Where fresh fruit and coffee can be found..", " ");
		v5.getLocation().setSymbolicLocation(symLoc5);
		graph.addVertex(v5);
		
		Vertex v4 = new Vertex(10,new AbsoluteLocation(57.012286, 9.990807, 1 ));
		WifiMeasurement wm4 = new WifiMeasurement();
		int ss4 = -62;
		for (String mac : MockGraph.getPermissableAPs())
			wm4.addValue(mac, ss4--);
		v4.addFingerprint(wm4);
		SymbolicLocation symLoc4 = new SymbolicLocation(10,"Room 3.2.33", "This is Christian Thomsen's office", "http://www.cs.aau.dk/~chr/");
		v4.getLocation().setSymbolicLocation(symLoc4);
		graph.addVertex(v4);
		
		Vertex v1 = new Vertex(11,new AbsoluteLocation(57.012296, 9.99059, 1));
		WifiMeasurement wm1 = new WifiMeasurement();
		int ss1 = -62;
		for (String mac : MockGraph.getPermissableAPs())
			wm1.addValue(mac, ss1--);
		v1.addFingerprint(wm1);
		SymbolicLocation symLoc1 = new SymbolicLocation(11,"Room 3.2.38", "This is Bent Thomsen's office", "http://www.cs.aau.dk/~bt/");
		v1.getLocation().setSymbolicLocation(symLoc1);
		graph.addVertex(v1);		
		
		Vertex v2 = new Vertex(12,new AbsoluteLocation(57.01229, 9.990669, 1));
		WifiMeasurement wm2 = new WifiMeasurement();
		int ss2 = -72;
		for (String mac : MockGraph.getPermissableAPs())
			wm2.addValue(mac, ss2--);
		v2.addFingerprint(wm2);
		SymbolicLocation symLoc2 = new SymbolicLocation(12,"Room 3.2.36", "This is Kurt Normark's office", "http://people.cs.aau.dk/~normark/");
		v2.getLocation().setSymbolicLocation(symLoc2);
		graph.addVertex(v2);				
			
		Vertex v3 = new Vertex(13,new AbsoluteLocation(57.01229, 9.990743, 1));
		WifiMeasurement wm3 = new WifiMeasurement();
		int ss3 = -55;
		for (String mac : MockGraph.getPermissableAPs())
			wm2.addValue(mac, ss3--);
		v3.addFingerprint(wm3);
		SymbolicLocation symLoc3 = new SymbolicLocation(13,"Room 3.2.34", "This is Rene Hansen's office", "http://people.cs.aau.dk/~rhansen/oop.html");
		v3.getLocation().setSymbolicLocation(symLoc3);
		graph.addVertex(v3);				
		
		graph.addUndirectionalEdges(v1, v2); 
		graph.addUndirectionalEdges(v2, v3);
		
		
		/* these are random points that are simply used to test navigation */
		Vertex n1 = new Vertex(14,new AbsoluteLocation(57.012359, 9.991575, 1));
		SymbolicLocation symLocn1 = new SymbolicLocation(14,"1", "bla", "blabla");
		n1.getLocation().setSymbolicLocation(symLocn1);
		graph.addVertex(n1);
		
		Vertex n2 = new Vertex(15,new AbsoluteLocation(57.012382, 9.990986, 1));
		SymbolicLocation symLocn2 = new SymbolicLocation(15,"2", "bla", "blabla");
		n2.getLocation().setSymbolicLocation(symLocn2);
		graph.addVertex(n2);
		
		Vertex n3 = new Vertex(16,new AbsoluteLocation(57.012047, 9.991696, 1));
		SymbolicLocation symLocn3 = new SymbolicLocation(16,"3", "bla", "blabla");
		n3.getLocation().setSymbolicLocation(symLocn3);
		graph.addVertex(n3);
		
		Vertex n4 = new Vertex(17,new AbsoluteLocation(57.012038, 9.991107, 1));
		SymbolicLocation symLocn4 = new SymbolicLocation(17,"4",  "bla", "blabla");
		n4.getLocation().setSymbolicLocation(symLocn4);
		graph.addVertex(n4);
		
		Vertex n5 = new Vertex(18,new AbsoluteLocation(57.012047, 9.990876, 1));
		SymbolicLocation symLocn5 = new SymbolicLocation(18,"5",  "bla", "blabla");
		n5.getLocation().setSymbolicLocation(symLocn5);
		graph.addVertex(n5);
		
		Vertex n6 = new Vertex(19,new AbsoluteLocation(57.012034, 9.990935, 1));
		SymbolicLocation symLocn6 = new SymbolicLocation(19,"6",  "bla", "blabla");
		n6.getLocation().setSymbolicLocation(symLocn6);
		graph.addVertex(n6);
		
		Vertex n7 = new Vertex(20,new AbsoluteLocation(57.012288, 9.99101, 1));
		SymbolicLocation symLocn7 = new SymbolicLocation(20,"7",  "bla", "blabla");
		n7.getLocation().setSymbolicLocation(symLocn7);
		graph.addVertex(n7);
				
		
		
		/*
		 * following is for a third floor
		 */
		
		Vertex third1 = new Vertex(21,new AbsoluteLocation(57.012359, 9.991789, 2));
		SymbolicLocation symLocnthird1 = new SymbolicLocation(23,"third: 1", "bla", "blabla");
		third1.getLocation().setSymbolicLocation(symLocnthird1);
		graph.addVertex(third1);
		
		Vertex third2 = new Vertex(22,new AbsoluteLocation(57.012379, 9.991219, 2));
		SymbolicLocation symLocnthird2 = new SymbolicLocation(24,"third: 2", "bla", "blabla");
		third2.getLocation().setSymbolicLocation(symLocnthird2);
		graph.addVertex(third2);
		
		Vertex third3 = new Vertex(23,new AbsoluteLocation(57.012116, 9.990972, 2));
		SymbolicLocation symLocnthird3 = new SymbolicLocation(25,"third: 3", "bla", "blabla");
		third3.getLocation().setSymbolicLocation(symLocnthird3);
		graph.addVertex(third3);
		
		Vertex third4 = new Vertex(24,new AbsoluteLocation(57.012066, 9.991592, 2));
		SymbolicLocation symLocnthird4 = new SymbolicLocation(26,"third: 4", "bla", "blabla");
		third4.getLocation().setSymbolicLocation(symLocnthird4);
		
		Vertex third5 = new Vertex(24,new AbsoluteLocation(57.012337, 9.990968, 2));
		SymbolicLocation symLocnthird5 = new SymbolicLocation(99,"third: 5", "bla", "blabla");
		third4.getLocation().setSymbolicLocation(symLocnthird5);
		
		graph.addVertex(third4);
		graph.addVertex(third5);
		graph.addUndirectionalEdges(third2, third5);
		
		graph.addUndirectionalEdges(third1, third2);
		graph.addUndirectionalEdges(third2, third3);
		graph.addUndirectionalEdges(third3, third4);
				
		graph.addUndirectionalEdges(v12, v11);
		graph.addUndirectionalEdges(v11, v10);
		graph.addUndirectionalEdges(v10, v9);
		graph.addUndirectionalEdges(v9, v8);
		
		graph.addUndirectionalEdges(n1, n2);
		graph.addUndirectionalEdges(n2, n7);
		
		graph.addUndirectionalEdges(n3, n4);
		graph.addUndirectionalEdges(n4, n6);
		graph.addUndirectionalEdges(n6, n5);
		graph.addUndirectionalEdges(n5, n7);
		graph.addUndirectionalEdges(n7, v3);
		
		/*
		graph.addVertex(v1);				
		graph.addUndirectionalEdges(v1, v2); //this inserts v2 as well
		graph.addVertex(v3);
		*/
		return graph;
	}
	
	private static List<String> getPermissableAPs()
	{
		ArrayList<String> result = new ArrayList<String>();
		result.add("00-1D-A2-C8-08-83");
		result.add("00-13-5F-57-F5-13");
		result.add("00-17-DF-2C-86-C6");
		result.add("00-1E-13-6E-6E-05");
		result.add("00-1C-0E-43-47-E0");	
		
		return result;
	}
	
	
	
}
