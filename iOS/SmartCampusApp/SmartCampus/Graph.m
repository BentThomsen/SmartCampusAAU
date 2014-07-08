//
//  IGraph.m
//  SmartCampus
//
// Copyright (c) 2014, Aalborg University
// All rights reserved.

// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     * Neither the name of the <organization> nor the
//       names of its contributors may be used to endorse or promote products
//       derived from this software without specific prior written permission.

// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL AAlBORG UNIVERSITY BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import "Graph.h"

#import "AbsoluteLocation.h"
#import "Edge.h"
#import "Vertex.h"

@implementation Graph

- (int)numVertices {
    [self doesNotRecognizeSelector:_cmd];
    return 0;
}

- (BOOL)containsVertex:(Vertex *)vertex {
    [self doesNotRecognizeSelector:_cmd];
    return NO;
}

- (Vertex *)vertexForId:(int)id {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}

- (NSDictionary *)vertices {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}

- (NSArray *)verticesForFloor:(int)floor {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}


- (BOOL)addVertex:(Vertex *)vertex {
    [self doesNotRecognizeSelector:_cmd];
    return NO;
}


- (BOOL)removeVertex:(Vertex *)vertex {
    [self doesNotRecognizeSelector:_cmd];
    return NO;
}

- (int)numEdges {
    [self doesNotRecognizeSelector:_cmd];
    return 0;
}

- (BOOL)containsEdge:(Edge *)edge {
    [self doesNotRecognizeSelector:_cmd];
    return NO;
}

- (NSArray *)edges {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}

- (NSArray *)edgesForFloor:(int)floor {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}

- (Edge *)addDirectionalEdge:(Edge *)edge {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}

- (Edge *)addDirectionalEdgeFromOrigin:(Vertex *)origin toDestination:(Vertex *)destination {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}

- (BOOL)removeDirectionalEdgeFromOrigin:(Vertex *)origin toDestination:(Vertex *)destination {
    [self doesNotRecognizeSelector:_cmd];
    return NO;
}

- (Edge *)addUndirectionalEdge:(Edge *)edge {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}

- (NSArray *)addUndirectionalEdgesBetweenVertex:(Vertex *)vertex1 andVertex:(Vertex *)vertex2 {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}

- (BOOL)removeUndirectionalEdgesBetweenVertex:(Vertex *)vertex1 andVertex:(Vertex *)vertex2 {
    [self doesNotRecognizeSelector:_cmd];
    return NO;
}

- (BOOL)addElevatorEndpoint:(Vertex *)endpoint {
    [self doesNotRecognizeSelector:_cmd];
    return NO;
}

- (BOOL)removeElevatorEndpoint:(Vertex *)endpoint {
    [self doesNotRecognizeSelector:_cmd];
    return NO;
}

- (NSArray *)elevatorEndpoints {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}

- (NSArray *)elevatorEndpointsForFloor:(int)floor {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}

- (BOOL)addStaircaseEndpoint:(Vertex *)endpoint {
    [self doesNotRecognizeSelector:_cmd];
    return NO;
}

- (BOOL)removeStaircaseEndpoint:(Vertex *)endpoint {
    [self doesNotRecognizeSelector:_cmd];
    return NO;
}

- (NSArray *)staircaseEndpoints {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}

- (NSArray *)staircaseEndpointsForFloor:(int)floor {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}

- (BOOL)isVertex:(Vertex *)vertex1 adjacentTo:(Vertex *)vertex2 {
    [self doesNotRecognizeSelector:_cmd];
    return NO;
}

- (Vertex *)closestVertexToLocation:(AbsoluteLocation *)location {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}

- (NSArray *)destinationsOfVertex:(Vertex *)vertex {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}

- (NSArray *)endVerticesOfEdge:(Edge *)edge {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}

- (NSArray *)verticesAdjacentToVertex:(Vertex *)vertex {
    [self doesNotRecognizeSelector:_cmd];
    return nil;
}

- (void)insertRadiusVerticesAtVertex:(Vertex *)vertex withRadius:(int)radius {
    [self doesNotRecognizeSelector:_cmd];
}

@end
