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

package com.smartcampus.android.location.wifi;

import java.util.TreeMap;
import com.smartcampus.indoormodel.graph.Vertex;
import com.smartcampus.wifi.WifiMeasurement;

/**
 * This class implements the 'Nearest Neighbor in Signal Space' (NNSS) algorithm
 * @author rhansen
 *
 */
public class AlgorithmNNSS implements IPositioningAlgorithm {
	private final static int MISSING_MAC_PENALTY = -150;
	
	private BCS bcs; // = new BCS(5);
	
	@Override
	public EstimateResult compare(Iterable<Vertex> vertices, WifiMeasurement measurement)
    {
		measurement = getNStrongestAPMeasurement(measurement, 7);
		
		if (vertices == null || measurement == null)
			return null;
		
		bcs = WifiPosEngine.BestCandidateSet; //new BCS(5); //bcs.clear();
        
        double curDist; //distance of current vertice in search space
        EstimateResult result = new EstimateResult(null, Double.MAX_VALUE);
        
        for (Vertex curVertex : vertices) //sammenlign med hver Vertex
        {
            for (WifiMeasurement curFP : curVertex.getFingerPrints()) //sammenlign med hvert fingerprint (usually only one - otherwise use more intelligent approach)
            {
                curDist = 0;
                for (String mac : measurement.getMACs()) //all APs in sample
                    if (curFP.containsMac(mac))
                        curDist += Math.pow((measurement.getAvgDbM(mac) - curFP.getAvgDbM(mac)), 2);
                    else
                        curDist += Math.pow((measurement.getAvgDbM(mac) - MISSING_MAC_PENALTY), 2);
                
                curDist = Math.sqrt(curDist);
                if (curDist < result.getDistance())
                {
                	result.setDistance(curDist);
                	result.setVertex(curVertex);
                }
                bcs.add(curVertex, curDist); //add to best candidate set - which will take care of only using the best estimates. 
            }                
        }
        //The following only yields a local error estimate within the primary- or secondary 
        //vertices and may thus not be appropriate
        result.setErrorEstimate(Math.ceil(bcs.getMaxDistance()));
        return result;
    }
		
	//Public visibility so we can test it (directly) via unit tests
	public static WifiMeasurement getNStrongestAPMeasurement(WifiMeasurement measurement, int n)
	{
		if (measurement.getMACs().size() < n)
			return measurement;
	
		TreeMap<Double, String> strongestAPs = new TreeMap<Double, String>();
		
		//Find the n strongest macs
		//
		for (String mac : measurement.getMACs()) //all APs in sample
		{
			double curMacVal = measurement.getAvgDbM(mac);
			while (strongestAPs.containsKey(curMacVal))
				curMacVal += 0.0001;
            	
			strongestAPs.put(curMacVal, mac);
		
			//NB: TreeMap sorts members in ascending order!
			//Thus, we remove from the head to keep the strongest values
			if (strongestAPs.size() > n)
            	strongestAPs.remove(strongestAPs.firstKey());       
		}
		
		//Create new measurement containing n strongest macs
		WifiMeasurement result = new WifiMeasurement();
		for (double d : strongestAPs.keySet())
		{
			result.addValue(strongestAPs.get(d), (int)d);
		}
		return result;
	}
	
	/*
	public static WifiMeasurement getNStrongestAPMeasurement2(WifiMeasurement measurement, int n)
	{
				
		//validate input
		if (measurement == null || n < 1)
			return null;
		
		if (measurement.getMACs().size() < n)
			return measurement;
	
		double[] keys = new double[n];
		String[] values = new String[n];
		int numInsertedElems = 0;
		
		//Find the n strongest macs
		for (String mac : measurement.getMACs()) //all APs in sample
		{
			double curMacVal = measurement.getAvgDbM(mac);
		
			if (numInsertedElems == 0 || curMacVal > keys[lastIndex])
			{
				keys[numInsertedElems] = curMacVal;
				values[numInsertedElems] = mac;
			}                  
		}
		
		//Create new measurement containing n strongest macs
		WifiMeasurement result = new WifiMeasurement();
		for (double d : strongestAPs.keySet())
		{
			result.addValue(strongestAPs.get(d), (int)d);
		}
		return result;
	}
	*/
}
