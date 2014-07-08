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

package com.smartcampus.android.navigation;

import java.util.LinkedList;

import com.smartcampus.indoormodel.AbsoluteLocation;
import com.smartcampus.indoormodel.graph.IGraph;
import com.smartcampus.indoormodel.graph.Vertex;

public class NavigationEngine {
			
	 // Note that this implementation assumes that the graph
	 // does not change while a specific instance of the
	 // navigation engine is used. This may not comply
	 // if user contributions to the graph are made...
			
	private LinkedList<AbsoluteLocation> convertVerticesToAbsLoc(LinkedList<Vertex> path) {
		LinkedList<AbsoluteLocation> pathAbsLoc = new LinkedList<AbsoluteLocation>();
		for(Vertex v : path) {
			pathAbsLoc.add(v.getLocation().getAbsoluteLocation());
		}
		return pathAbsLoc;
	}
	
	private LinkedList<Vertex> findLvlChangerWithShortestPath(LinkedList<LinkedList<Vertex>> paths) {
		
		LinkedList<Vertex> bestPath = new LinkedList<Vertex>();
		int bestLength = Integer.MAX_VALUE;
		for(LinkedList<Vertex> currentPath : paths) {
			if(currentPath != null) {
				int pathLength = getPathLength(currentPath);
				if(pathLength < bestLength) {
					bestLength = pathLength;
					bestPath = currentPath;
				}
			}
		}
		return bestPath;
	}
	
	private int getCorrectDestinationFloor(Vertex destinationVertex, int currentFloor) {
		return (int)destinationVertex.getLocation().getAbsoluteLocation().getAltitude();		
	}
		
	private int getDistance(Vertex origin, Vertex destination) {
    	AbsoluteLocation absOriginLoc = origin.getLocation().getAbsoluteLocation();
    	AbsoluteLocation absDestinationLoc = destination.getLocation().getAbsoluteLocation();
    	
    	int x1 = (int)(absOriginLoc.getLatitude() * 1E6),
    		x2 = (int)(absDestinationLoc.getLatitude() * 1E6),
    		y1 = (int)(absOriginLoc.getLongitude() * 1E6), 
			y2 = (int)(absDestinationLoc.getLongitude() * 1E6);
    	
    	
    	//Maybe casting to an integer may pose problems...
    	return (int)Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
	
	private LinkedList<Vertex> getLvlChangersToFloor(LinkedList<Vertex> lvlChangers, int currentFloor, int destinationFloor) {
		LinkedList<Vertex> result = new LinkedList<Vertex>();
		for(Vertex v : lvlChangers) {			
			int v1Floor = (int)(v.getLocation().getAbsoluteLocation().getAltitude());
			if (v1Floor == currentFloor)
			{
				for(Vertex v2 : v.adjacentVertices()) {
					int v2Floor = (int)(v2.getLocation().getAbsoluteLocation().getAltitude());
					if(v2Floor == destinationFloor) {
						result.add(v);
						break;						
					}
				}
			}			
		}
		return result;
	}
	
	private LinkedList<Vertex> getLvlChangersToHigherLvl(LinkedList<Vertex> lvlChangers, int currentFloor) {
		return getLvlChangersToFloor(lvlChangers, currentFloor, currentFloor + 1);
	}
	
	private LinkedList<Vertex> getLvlChangersToLowerLvl(LinkedList<Vertex> lvlChangers, int currentFloor) {
		return getLvlChangersToFloor(lvlChangers, currentFloor, currentFloor - 1);		
	}
	
	private int getPathLength(LinkedList<Vertex> path) {
		int totalLength = 0;
		
		for(int i = 0; i < path.size() - 1; i++) {
			Vertex origin = path.get(i);
			Vertex destination = path.get(i + 1);
			totalLength = totalLength + getDistance(origin, destination);
		}
		
		return totalLength;
	}
	
	
	// This method may be useful elsewhere (make it public in that case).
	// It may be used to display the total length of the path the user has to
	// walk to get to the destination. 
	 
	/**
	 * This method is used to obtain the route to a destination vertex. The source vertex will automatically be derived
	 * from the user's current position as obtained by the underlying positioning technique. 
	 * @param destinationVertex The {@link Vertex} to which a route must be determined
	 * @return A linked list containing the route represented as absolute locations ({@link AbsoluteLocation}). Consecutive 
	 * absolute locations represent steps in eventually arriving at the destinationVertex
	 * @see AbsoluteLocation
	 * @see Vertex
	 */
	public LinkedList<AbsoluteLocation> getRoute(IGraph graph, AbsoluteLocation userLoc, Vertex destination) {
		
		if(userLoc != null) {
			LinkedList<AbsoluteLocation> routeAbsLocList = new LinkedList<AbsoluteLocation>();
			LinkedList<Vertex> totalShortestPath = new LinkedList<Vertex>();
			
			routeAbsLocList.add(userLoc);
			
			Vertex sourceVertex = graph.getClosestVertex(userLoc);
			
			DijkstraShortestPath dijkstra;
			
			
			//currentfloor HIGHLY depends on the estimated location containing
			// the correct altitude/floor
			 
			int currentFloor = (int)userLoc.getAltitude();
			int destinationFloor = getCorrectDestinationFloor(destination, currentFloor);
			
			
			if(currentFloor != destinationFloor) {
    			// here we might insert a check such that the
				// path accommodates disabled people e.g. only
				// consider elevators and not stairs
				 
				LinkedList<Vertex> lvlChangers = new LinkedList<Vertex>();
				lvlChangers.addAll(graph.getStaircaseVertices(currentFloor));
				lvlChangers.addAll(graph.getElevatorVertices(currentFloor));
				
				while(currentFloor != destinationFloor) {
					
					dijkstra = new DijkstraShortestPath(sourceVertex, graph, currentFloor);
					
					LinkedList<Vertex> relevantLvlChangers = null;
					
					if(currentFloor > destinationFloor) {
						relevantLvlChangers = getLvlChangersToLowerLvl(lvlChangers, currentFloor);
						currentFloor--;
					} else if(currentFloor < destinationFloor) {
						relevantLvlChangers = getLvlChangersToHigherLvl(lvlChangers, currentFloor);
						currentFloor++;
					}

					LinkedList<LinkedList<Vertex>> paths = new LinkedList<LinkedList<Vertex>>();
					//TODO: Will probably be an issue
					if(relevantLvlChangers != null) {
						for(Vertex lvlVertex : relevantLvlChangers) {
							LinkedList<Vertex> dijkstraPath = dijkstra.getShortestPath(lvlVertex);
							paths.add(dijkstraPath);							
						}
					}
					
					LinkedList<Vertex> pathToClosestLvlChanger = findLvlChangerWithShortestPath(paths);
					totalShortestPath.addAll(pathToClosestLvlChanger);
					
					Vertex endVertex = pathToClosestLvlChanger.getLast();
					//find source on next floor
					for (Vertex v : endVertex.adjacentVertices())
					{
						int oppositeFloor = (int)v.getLocation().getAbsoluteLocation().getAltitude();
						if (oppositeFloor == currentFloor)
						{
							sourceVertex = v;
							break;
						}
					}
					
					//Original two lines before }
					//sourceVertex = pathToClosestLvlChanger.getLast();
					//sourceVertex.getLocation().getAbsoluteLocation().setAltitude(currentFloor);				
				}
			}

			dijkstra = new DijkstraShortestPath(sourceVertex, graph, currentFloor);
			LinkedList<Vertex> temp = dijkstra.getShortestPath(destination);
			totalShortestPath.addAll(temp);
			routeAbsLocList.addAll(convertVerticesToAbsLoc(totalShortestPath));
			
			return routeAbsLocList;
		}
		
		return null;
	}  
}

