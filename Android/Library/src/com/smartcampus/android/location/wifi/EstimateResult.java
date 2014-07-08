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

import com.smartcampus.indoormodel.graph.Vertex;

/**
 * This class represents an estimate result, i.e., an estimated vertex along with 
 * information about the estimate:
 * - The Score or ErrorDistance with which the estimate was obtained. 
 *   - Some algorithms use distances to reason about the quality of a location estimates. The shorter the distance, the better. 
 *   - Other algorithsm use probabilities to reason about the quality of a location estimate. The higher the probability, the better. 
 *   - A particular algorithm will typically only use one of these metrics, i.e., it either reports the distance or the probability of the estimated vertex. 
 * - BcsVertices represent the next-best estimates considered during location determination.
 * @author rhansen
 *
 */
//Consider making subclasses instead for the different types of estimate results
//It could be argued that the class in its current form violates the ISP principle. 
public class EstimateResult {
	private Vertex mVertex;
	private double mDistance;
	private double mErrorEstimate;
	private boolean mUsesDistanceMetric;
	private Vertex[] mBcsVertices;
	private int[] mBcsVerticesIds;
	private double[] mBcsScores;
	
	public EstimateResult(Vertex v, double distance)
	{
		this.mVertex = v;
		this.mDistance = distance;
		this.setUsesDistance(true);
	}
	
	/**
	 * Indicates if the estimate-result uses distance as the quality measure
	 * (the lower the value, the better). 
	 * If false, the estimate-result instead uses another error-estimate quality measure (typically probability) 
	 * where the rule is that the higher the value, the better the estimate. 
	 * @param usesDistance
	 */
	public void setUsesDistance(boolean usesDistance) {
		this.mUsesDistanceMetric = usesDistance;
	}
	
	/**
	 * Indicates whether the result relies on the distance metric as a measure of estimate quality. 
	 * @return
	 */
	public boolean getUsesDistance() {
		return this.mUsesDistanceMetric;
	}
	
	/**
	 * Gets the distance of the estimate (may or may not be used)
	 * @return
	 */
	public double getDistance()
	{
		return this.mDistance;
	}
	
	/**
	 * Gets the error estimate (which may or may not be used)
	 * @return
	 */
	public double getErrorEstimate()
	{
		return this.mErrorEstimate;
	}
	
	/**
	 * Gets the estimated vertex
	 * @return
	 */
	public Vertex getVertex()
	{
		return this.mVertex;
	}
	
	/**
	 * Sets the distance (which is now the official quality metric)
	 * @param distance
	 */
	public void setDistance(double distance)
	{
		this.mDistance = distance;
		this.mUsesDistanceMetric = true;
	}
	
	/**
	 * Sets the error estimate (which is now the official quality metric)
	 * @param d
	 */
	public void setErrorEstimate(double d)
	{
		this.mErrorEstimate = d;
		this.mUsesDistanceMetric = false;
	}
	
	/**
	 * Sets the estimated vertex
	 * @param v
	 */
	public void setVertex(Vertex v)
	{
		this.mVertex = v;
	}
	
	/**
	 * Set the set of best-scoring vertices
	 * It is assumed that the set of bcs scores match the indexes of this array
	 * i.e., the score of the vertex in bcsVertices[1] can be found in bcsScores[1].  
	 * @param bcsVertices It is assumed that the vertices are sorted according to their score. The best scoring vertex (the estimated vertex) is first. 
	 */
	public void setBcsVertices(Vertex[] bcsVertices)
	{
		this.mBcsVertices = bcsVertices;
		this.mBcsVerticesIds = new int[mBcsVertices.length];
		for (int i = 0; i < mBcsVertices.length; i++)
		{
			mBcsVerticesIds[i] = mBcsVertices[i].getId();
		}
	}
	/**
	 * Gets the set of best-scoring vertices. 
	 * @return The best-scoring vertices in sorted order. The best score (the estimated vertex) is first.
	 */
	public Vertex[] getBcsVertices()
	{
		return this.mBcsVertices;
	}
	
	/**
	 * Returns only the id of the best-scoring vertices. 
	 * @return
	 */
	public int[] getBcsVerticesIDs()
	{
		return mBcsVerticesIds;
	}
	
	/**
	 * Set the best scores in ascending order (the best score is first). 
	 * It is assumed that the indexes of bcsVertices and bcsScores match. 
	 * That is, the score bcsScore[1] represent the score of estimate bcsVertices[1]
	 * @param bcsScores scores sorted in ascending order.
	 */
	public void setBcsScores(double[] bcsScores)
	{
		this.mBcsScores = bcsScores;
	}
	/**
	 * Gets the scores of the bcs vertices
	 * @return The best scores in ascending order (the best score first).
	 */
	public double[] getBcsScores()
	{
		return this.mBcsScores;
	}
}
