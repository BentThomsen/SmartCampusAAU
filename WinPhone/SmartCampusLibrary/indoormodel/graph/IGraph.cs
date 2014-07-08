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
DISCLAIMED. IN NO EVENT SHALL AAlBORG UNIVERSITY BE LIABLE FOR ANY
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

namespace com.smartcampus.indoormodel.graph
{
    public interface IGraph
    {

        Edge addDirectionalEdge(Edge e);

        //We also add any missing vertices
        Edge addDirectionalEdge(Vertex origin, Vertex destination);

        bool addElevatorVertex(Vertex v);

        bool addStaircaseVertex(Vertex v);

        Edge addUndirectionalEdge(Edge e);

        IEnumerable<Edge> addUndirectionalEdges(Vertex v1, Vertex v2);

        bool addVertex(Vertex v);

        IEnumerable<Vertex> adjacentVertices(Vertex v);

        bool areAdjacent(Vertex v, Vertex w);

        bool ContainsEdge(Edge e);

        bool ContainsVertex(Vertex v);

        int degree(Vertex v);

        IEnumerable<Vertex> destinations(Vertex v);

        Vertex[] endVertices(Edge e);

        Vertex getClosestVertex(AbsoluteLocation userAbsLoc);

        List<Edge> getEdges();

        List<Edge> getEdges(int floor);

        List<Vertex> getElevatorVertices();

        List<Vertex> getElevatorVertices(int level);

        List<Vertex> getStaircaseVertices();

        List<Vertex> getStaircaseVertices(int level);

        Vertex getVertexById(int vertexId);

        IEnumerable<Vertex> getVertices();

        List<Vertex> getVertices(int floor);

        IEnumerable<Edge> incidentEdges(Vertex v);

        int inDegree(Vertex v);

        // Methods dealing with directed edges //

        IEnumerable<Edge> inEdges(Vertex v);

        //Methods dealing with positioning 
        //We don't bother to create a subclass for this behavior as our graph is only used for one purpose in this application
        void InsertRadiusVertices(Vertex v, int radius);

        int numEdges();

        int numVertices();

        Vertex opposite(Edge e, Vertex v);

        /**
         * @return All vertices with an edge going to v
         */
        IEnumerable<Vertex> origins(Vertex v);

        int outDegree(Vertex v);

        IEnumerable<Edge> outEdges(Vertex v);

        bool removeDirectionalEdge(Vertex origin, Vertex destination);

        bool removeElevatorVertex(Vertex v);

        bool removeStaircaseVertex(Vertex v);

        bool removeUndirectionalEdges(Vertex v1, Vertex v2);

        bool removeVertex(Vertex v);
    }
}
