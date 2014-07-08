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

import com.smartcampus.indoormodel.Building;
import com.smartcampus.indoormodel.graph.IGraph;
import com.smartcampus.indoormodel.graph.Vertex;
import com.smartcampus.wifi.WifiMeasurement;

/**
 * This class functions as a 'Wi-Fi positioning engine'. That is, it takes care of computing location estimates 
 * based on the location fingerprinting technique. 
 * To that end it needs a building (graph) of vertices where each vertex has associated Wi-Fi measurements. 
 * This class makes use of the 'Weighted Graph' positioning technique
 * @author rhansen
 *
 */
public class WifiPosEngine {

	private Building mCurrentBuilding;
	private IGraph mGraph;
	private Iterable<Vertex> secondarySearchSpace;	
	private Vertex prevBestEstimateVertex;
	private int numSecondaryBest;
	private IPositioningAlgorithm mPosAlgorithm;
	//This is a set which holds all the best scoring candidates
	//Candidates will be added from the appropriate algorithm during location determination
	//(We don't bother with encapsulation)
	public static BCS BestCandidateSet;
		
	public WifiPosEngine(Building currentBuilding)
	{
		//this(currentBuilding, new AlgorithmHyperNNSS());
		this(currentBuilding, new AlgorithmNNSS());
	}
	
	public WifiPosEngine(Building currentBuilding, IPositioningAlgorithm posAlgorithm)
	{
		setCurrentBuilding(currentBuilding);
		setPositioningAlgorithm(posAlgorithm);
	}
	
	void setPositioningAlgorithm(IPositioningAlgorithm posAlgorithm)
	{
		this.mPosAlgorithm = posAlgorithm;
	}
		
	public Building getCurrentBuilding()
	{
		return mCurrentBuilding;
	}
	
	
	public EstimateResult getEstimate(WifiMeasurement currentMeasurement)
    {  
		//Check ready state
		if (mGraph == null)
			return null;
		
		//Maintain primary and secondary search space
		//Cf OfflineClientPocketPCUF
		if (secondarySearchSpace == null)
		{
			secondarySearchSpace = mGraph.getVertices();
		}
		EstimateResult primaryEstimate = new EstimateResult(null, Double.MAX_VALUE);
		EstimateResult secondaryEstimate = new EstimateResult(null, Double.MAX_VALUE);
		
		BestCandidateSet = new BCS(10); //candidates are added in the compare methods below
		
		//measurement is compared with primary search space (adjacent vertices to previous estimated vertex)
		//and secondary search space (non-connected nodes or the full graph)
		if (prevBestEstimateVertex != null)
		{
			primaryEstimate = mPosAlgorithm.compare(prevBestEstimateVertex.adjacentVertices(), currentMeasurement);
		}
		secondaryEstimate = mPosAlgorithm.compare(secondarySearchSpace, currentMeasurement);
				
		//Changed to accomodate hyper, where we return null if online meas only has one mac
		//Vertex best = null;
		EstimateResult bestEstimate = null;
		if (primaryEstimate != null) 
			bestEstimate = primaryEstimate; 
		if (secondaryEstimate != null)
		{
			//The primary estimate may be overriden by a secondary if it is better for the second time in a row
			if (secondaryEstimate.getDistance() < primaryEstimate.getDistance())
			{
				numSecondaryBest++;
				if (numSecondaryBest >= 2 || prevBestEstimateVertex == null)
				{
					numSecondaryBest = 0;
					bestEstimate = secondaryEstimate; //.getVertex();
				}
			}
			else
			{
				numSecondaryBest = 0;
			}			
		}
		prevBestEstimateVertex = bestEstimate.getVertex();
		
		//Currently, the error estimate is also calculated in the compare methods, 
		//but we override that logic here since this implementation considers the global 
		//candidates - not just the local primary- or secondary candidates. 
		//We throw in an extra 5 meters to account for movement
		double error = Math.ceil(BestCandidateSet.getDistanceToNthHighest(3)); //  + 5;
		bestEstimate.setErrorEstimate(error);
		bestEstimate.setBcsScores(BestCandidateSet.getAllScoresSorted());
		bestEstimate.setBcsVertices(BestCandidateSet.getAllVerticesSorted());
		return bestEstimate;
    }
	
	public void setCurrentBuilding(Building currentBuilding)
	{
		this.mCurrentBuilding = currentBuilding;
		if (this.mCurrentBuilding != null)
		{
			this.mGraph = mCurrentBuilding.getGraphModel();
			secondarySearchSpace = this.mGraph.getVertices();
		}
	}
	
}
