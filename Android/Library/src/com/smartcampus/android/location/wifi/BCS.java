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

import java.util.ArrayList;

import java.util.List;
import java.util.TreeMap;

import com.smartcampus.baselogic.DistanceMeasurements;
import com.smartcampus.indoormodel.AbsoluteLocation;
import com.smartcampus.indoormodel.graph.Vertex;

/**
 * This class represents a 'Best Candidate Set' of vertices, i.e., the N set of the 
 * best-scoring vertices during location determination (as done within an IPositioningAlgorithm implementation's Compare() method)
 * @author rhansen
 *
 */
public class BCS
{
	private static double defaultErrorDistance = 20;
	
	//The number of elements used in the 'BestCandidateSet'
    private int maxSize; 
    
    //list of estimates sorted according to their corresponding probabilities
    private TreeMap<Double, Vertex> locations = new TreeMap<Double, Vertex>(); 
    
    public BCS()
    {
    	maxSize = 10;
    }
    
    public BCS(int maxSize)
    {
    	this.maxSize = maxSize;
    }
    
    public void clear()
    {
        locations.clear();
    }

    public void setMaxSize(int size)
    {
        this.maxSize = size;
    }

    public int getMaxSize()
    {
        return maxSize;
    }

    /**
     * Add a new element to the 'best candidate set' - if it qualifies. 
     * I.e., the distance is lower than the current worst candidate
     * NOTE: Currently, this is hardcoded to distance, i.e., to be used with NNS
     * @param v Vertex to be inserted if the distance parameter is lower than the current worst element in the BCS.
     * @param distance The (NNS) distance of the vertex which determines whether v will be inserted in the BCS. 
     */
    public boolean add(Vertex v, double distance)
    {
        if (locations.size() >= maxSize && distance > locations.lastKey())
        {
            return false; //is not better than the worst element
        }
        
        //We do not allow duplicate vertices in the BCS
        //Equality check is done on vertex id
        if (locations.containsValue(v))
        {
        	return false;
        }
        //Here, the identity check is done using the Id (which should have a valid value)
        /*
        for (Vertex curVertex : locations.values())
        {
        	if (curVertex.getId() == v.getId())
        	{
        		return false;
        	}
        }
    	*/
        //Insert v        
        //We use the following 'hack' in case of duplicate values
        while (locations.containsKey(distance))
            distance += 0.0001;
        
        //Impl. note: Check for emptiness first as lastKey() will throw NoSuchElement if empty.
        //if (locations.isEmpty() || distance < locations.lastKey())
        locations.put(distance, v);

        if (locations.size() > maxSize)
        	locations.remove(locations.lastKey());
            
        return true;
    }
    
    /**
     * Returns the list of the best estimates and their associated probablities in sorted order. 
     * @return
     */
    public TreeMap<Double, Vertex> getAll()
    {
        return locations;
    }

    /**
     * Returns the probabilityGeoPosition with the highest probability in the BestEstimates3
     * @return
     */
    public Vertex getFirst()
    {
        return locations.get(locations.firstKey());
    }

    /**
     * Returns the probabilityGeoPosition with the lowest probability in the BestEstimates3
     * @return
     */
    public Vertex getLast()
    {
        return locations.get(locations.lastKey());
    }

    /**
     * Returns the vertex with the nth-highest probability in the BestEstimates (the best having n=1)
     * @param n 
     * @return
     */
    public Vertex getNthHighest(int n)
    {
    	//NOTE: Is this collection even sorted??
    	int i = 1; //we start at 1 (rather than zero)
    	for (Vertex v : locations.values())
    	{
    		if (i == n)
    			return v;
    		i++;
    	}    	
    	return null;
    }
    
    /**
     * Returns the vertex with the nth-highest probability in the BestEstimates (the best having n=1)
     * @param n 
     * @return
     */
    public double getNthHighestScore(int n)
    {    	
    	int i = 1; //we start at 1 (rather than zero)
    	for (double score : locations.keySet())
    	{
    		if (i == n)
    			return score;
    		i++;
    	}    	
    	return Double.MAX_VALUE;
    }

    public Vertex[] getAllVerticesSorted()
    {
    	return locations.values().toArray(new Vertex[locations.size()]);
    }
    
    public double[] getAllScoresSorted()
    {
    	double[] result = new double[locations.size()];
    	int i = 0;
    	for (double score : locations.keySet())
    	{
    		result[i] = score;
    		i++;
    	}
    	return result;
    }
    
    /**
     * Returns the distance from the best (returned) estimate to the n-1 remaining estimates in the BCS
     */ 
    public double getAvgDist()
    {
	    //average distance from best estimate
	    double avgDist = 0.0;
	    if (locations.size() > 1)
	    {
		    Vertex best = locations.get(locations.firstKey());
		    AbsoluteLocation bestLoc, curLoc;
		    bestLoc = best.getLocation().getAbsoluteLocation();
		    
		    for (Vertex v : locations.values())
		    {
			    if (best == v)
				    continue;			    
			    
			    curLoc = v.getLocation().getAbsoluteLocation();
			    avgDist += DistanceMeasurements.CalculateMoveddistanceInMeters(
                    bestLoc.getLatitude(), bestLoc.getLongitude(),
                    curLoc.getLatitude(), curLoc.getLongitude());
		    }
            avgDist /= (locations.size() - 1);
	    }
	    else
		    avgDist = defaultErrorDistance; //default value
		
	    return avgDist; 
	    //AccuracyGuarantees.timeDistanceSmoothing(avgDist);		
    }
    
    public double getAvgDistanceToNthHighest(int n)
    {
    	//average distance from best estimate
	    double avgDist = 0.0;
	    if (n > 1)
	    {
		    Vertex best = locations.get(locations.firstKey());
		    AbsoluteLocation bestLoc, curLoc;
		    int i = 1;
		    
		    //NOTE: Are values returned in sorted order?
		    for (Vertex v : locations.values()) 
		    {
			    if (best == v)
				    continue;
			    
			    if (i++ == n)
			    	break;
			    
			    bestLoc = best.getLocation().getAbsoluteLocation();
			    curLoc = v.getLocation().getAbsoluteLocation();
			    avgDist += DistanceMeasurements.CalculateMoveddistanceInMeters(
                    bestLoc.getLatitude(), bestLoc.getLongitude(),
                    curLoc.getLatitude(), curLoc.getLongitude());
		    }
            avgDist /= n - 1;
	    }
	    else
		    avgDist = defaultErrorDistance; //default value
		
	    return avgDist; 
	    //AccuracyGuarantees.timeDistanceSmoothing(avgDist);
    }
    
    public double getDistanceToNthHighest(int n)
    {
    	Vertex best = getFirst();
    	Vertex v = getNthHighest(n);
    	if (best != null && v != null)
    	{
    		AbsoluteLocation bestLoc, nLoc;
    		bestLoc = best.getLocation().getAbsoluteLocation();
			nLoc = v.getLocation().getAbsoluteLocation();
			return DistanceMeasurements.CalculateMoveddistanceInMeters(
                 bestLoc.getLatitude(), bestLoc.getLongitude(),
                 nLoc.getLatitude(), nLoc.getLongitude());
    	}
    	
    	return defaultErrorDistance; //default
    }

    public double getMaxDistance()
    {
    	//return getDistanceToNthHighest(getMaxSize());
    	try
    	{
	    	Vertex best = getFirst();
	    	if (best == null)
	    		return defaultErrorDistance;
	    	
	    	double maxDistance = 0, curDistance = 0;
	    	AbsoluteLocation bestLoc, curLoc;
	    	bestLoc = best.getLocation().getAbsoluteLocation();
			
	    	for (Vertex v : locations.values())
	    	{
	    		//skip the best
	    		if (v == best)
	    			continue;
	    		
	    		//Calculate the distance from the best location to the current location
	    		curLoc = v.getLocation().getAbsoluteLocation();
	    		curDistance = DistanceMeasurements.CalculateMoveddistanceInMeters(
	    						bestLoc.getLatitude(), bestLoc.getLongitude(),
	    						curLoc.getLatitude(), curLoc.getLongitude());
	    		
	    		//Is this the worst distance yet?    		
	    		if (curDistance > maxDistance)
	    		{
	    			maxDistance = curDistance;    			
	    		}    		
	    	}
	    	    	
	    	return maxDistance; //Return the worst distance
    	}
    	//HACK: CATCHING GENERIC EXCEPTION
    	//prompted by NoSuchElementException - but wasn't able to reproduce the error
    	catch (Exception ex) 
    	{
    		return defaultErrorDistance;
    	}
    }
    
    private static List<Double> smoothingQueue; 
    /**
     * This one simply averages the region sizes.
     * 	 - the same idea was used in "Location Sensing and Privacy in a Context-Aware Computing Environment" by Smailagic et al.  for smoothing
     * position estimates while tracking
     * - The method can easily be made more elegant... probably better off as independent class
     * - history length is currently fixed to 20.
     * 
     * @param p
     * @param regions
     * @param estimatedRegion
     * @return
     */
    public static double timeDistanceSmoothing(double estimatedRegion)
    {
	    if (smoothingQueue == null)
	    {
		    smoothingQueue = new ArrayList<Double>();
	    }
	    smoothingQueue.add(estimatedRegion);
	    if (smoothingQueue.size() > 20)
		    smoothingQueue.remove(19);
		
	    double total = 0;
	    for (double d : smoothingQueue)
	    {
		    total += d;
	    }
	    return total / smoothingQueue.size();		    
    }	
}

