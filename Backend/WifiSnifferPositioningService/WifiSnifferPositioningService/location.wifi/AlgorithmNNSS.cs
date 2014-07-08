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
using System.Text;
using WifiSnifferPositioningService.radiomap;
using WifiSnifferPositioningService.extensions.wifi;

namespace WifiSnifferPositioningService.location.wifi
{
	public class AlgorithmNNSS : IPositioningAlgorithm
	{
		private const int MISSING_MAC_PENALTY = -150;
	
		private BCS bcs;
	
	public EstimateResult compare(IEnumerable<Vertex> vertices, SnifferWifiMeasurement measurement)
	{
		measurement = getNStrongestAPMeasurement(measurement, 7);
		
		if (vertices == null || measurement == null)
			return null;
		
		bcs = WifiPosEngine.BestCandidateSet; //new BCS(5); //bcs.clear();
		
		double curDist; //distance of current vertice in search space
		EstimateResult result = new EstimateResult(null, double.MaxValue);
		
		foreach (Vertex curVertex in vertices) //sammenlign med hver Vertex
		{
			foreach (SnifferWifiMeasurement curFP in curVertex.SnifferWifiMeasurements) //sammenlign med hvert fingerprint (usually only one - otherwise use more intelligent approach)
			{
				curDist = 0;
				foreach (String mac in measurement.getMACs()) //all APs in sample
					if (curFP.containsMac(mac))
						curDist += Math.Pow((measurement.getAvgDbM(mac) - curFP.getAvgDbM(mac)), 2);
					else
						curDist += Math.Pow((measurement.getAvgDbM(mac) - MISSING_MAC_PENALTY), 2);
				
				curDist = Math.Sqrt(curDist);
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
		result.setErrorEstimate(Math.Ceiling(bcs.getMaxDistance()));
		return result;
	}
		
	//Public visibility so we can test it (directly) via unit tests
	public static SnifferWifiMeasurement getNStrongestAPMeasurement(SnifferWifiMeasurement measurement, int n)
	{
		if (measurement.getMACs().Count < n)
			return measurement;
	
		SortedDictionary<double, String> strongestAPs = new SortedDictionary<double, String>();
		
		//Find the n strongest macs
		//
		foreach (String mac in measurement.getMACs()) //all APs in sample
		{
			double curMacVal = measurement.getAvgDbM(mac);
			while (strongestAPs.ContainsKey(curMacVal))
				curMacVal += 0.0001;
				
			strongestAPs.Add(curMacVal, mac);
		
			//NB: TreeMap sorts members in ascending order!
			//Thus, we remove from the head to keep the strongest values
			if (strongestAPs.Count > n)
				strongestAPs.Remove(strongestAPs.First().Key);       
		}
		
		//Create new measurement containing n strongest macs
		SnifferWifiMeasurement result = new SnifferWifiMeasurement();
		foreach (double d in strongestAPs.Keys)
		{
			SnifferHistogram h = new SnifferHistogram();
			h.Mac = strongestAPs[d];
			h.value = (int)d;
			h.count = 1;
			result.SnifferHistograms.Add(h);
			//result.addValue(strongestAPs.get(d), (int)d);
		}
		return result;
	}
	}
}
