//
//  DictionaryGraph.m
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

#import "DictionaryGraph.h"
#import "DistanceMeasurements.h"
#import "Constants.h"

@implementation DictionaryGraph

+ (DictionaryGraph *)graphWithJson:(NSDictionary *)data {
    DictionaryGraph *graph = [DictionaryGraph new];

    for (id vertexData in [data valueForKey:@"Vertices"]) {
        [graph addVertex:[Vertex vertexWithJson:vertexData]];
    }

    for (id edgeData in [data valueForKey:@"Edges"]) {
        Vertex *origin = [graph vertexForId:[[edgeData valueForKey:@"vertexOrigin"] intValue]];
        Vertex *destination = [graph vertexForId:[[edgeData valueForKey:@"vertexDestination"] intValue]];

        for (Edge *edge in [graph addUndirectionalEdgesBetweenVertex:origin andVertex:destination]) {
            edge.id = [[edgeData valueForKey:@"ID"] intValue];
            edge.buildingId = [[edgeData valueForKey:@"Building_ID"] intValue];
            edge.staircase = [[edgeData valueForKey:@"is_stair"] boolValue];
            edge.elevator = [[edgeData valueForKey:@"is_elevator"] boolValue];
        }
    }

    return graph;
}

- (id)init
{
    if(!(self = [super init]))
        return nil;

    self.vertices = [NSMutableDictionary new];
    self.verticesByFloor = [NSMutableDictionary new];
    self.edges = [NSMutableArray new];
    self.staircaseEndpoints = [NSMutableArray new];
    self.elevatorEndpoints = [NSMutableArray new];

    return self;
}

- (Edge *)addDirectionalEdge:(Edge *)edge {
    Vertex *origin = edge.origin;
    Vertex *destination = edge.destination;

    [origin addOutEdge:edge];
    [destination addInEdge:edge];

    [self addVertex:origin];
    [self addVertex:destination];

    if (![self.edges containsObject:edge]) {
        [self.edges addObject:edge];
    }

    return edge;
}

- (Edge *)addDirectionalEdgeFromOrigin:(Vertex *)origin toDestination:(Vertex *)destination {
    Edge *edge = [[Edge alloc] initWithOrigin:origin andDestination:destination];
    return [self addDirectionalEdge:edge];
}

- (BOOL)addElevatorVertex:(Vertex *)vertex {
    [self.elevatorEndpoints addObject:vertex];
    return YES;
}

- (BOOL)addStaircaseVertex:(Vertex *)vertex {
    [self.staircaseEndpoints addObject:vertex];
    return YES;
}

- (Edge *)addUndirectionalEdge:(Edge *)edge {
    Vertex *origin = edge.origin;
    Vertex *destination = edge.destination;

    [origin addOutEdge:edge];
    [destination addOutEdge:edge];
    [origin addInEdge:edge];
    [destination addInEdge:edge];

    [self addVertex:origin];
    [self addVertex:destination];

    if(![self.edges containsObject:edge])
        [self.edges addObject:edge];

    return edge;
}

- (NSArray *)addUndirectionalEdgesBetweenVertex:(Vertex *)vertex1 andVertex:(Vertex *)vertex2 {
    Edge *edge1 = [self addDirectionalEdgeFromOrigin:vertex1 toDestination:vertex2];
    Edge *edge2 = [self addDirectionalEdgeFromOrigin:vertex2 toDestination:vertex1];

    return [[NSArray alloc] initWithObjects:edge1, edge2, nil];
}

- (BOOL)addVertex:(Vertex *)vertex {
    if ([self containsVertex:vertex]) return NO;
    
    [self.vertices setObject:vertex forKey:[NSNumber numberWithInt:vertex.id]];
    
    if (vertex.location && vertex.location.absolute) {
        NSNumber *floor = [NSNumber numberWithInt:(int) vertex.location.absolute.altitude];

        if (![self.verticesByFloor objectForKey:floor]) {
            [self.verticesByFloor setObject:[NSMutableArray new] forKey:floor];
        }

        [(NSMutableArray *) [self.verticesByFloor objectForKey:floor] addObject:vertex];
    }
    
    return YES;
}

- (NSArray *)verticesAdjacentToVertex:(Vertex *)vertex {
    return [vertex adjacentVertices];
}

- (BOOL)isVertex:(Vertex *)vertex1 adjacentTo:(Vertex *)vertex2 {
    for (Vertex *vertex in [vertex1 adjacentVertices]) {
        if ([vertex isEqual:vertex2]) return YES;
    }

    return NO;
}

- (BOOL)containsEdge:(Edge *)edge {
    return [self.edges containsObject:edge];
}

- (BOOL)containsVertex:(Vertex *)vertex {
    return [self vertexForId:vertex.id] != nil;
}

- (int)degreeOfVertex:(Vertex *)vertex {
    return [self containsVertex:vertex] ? [vertex degree] : -1;
}

- (NSArray *)destinationsOfVertex:(Vertex *)vertex {
    return [vertex destinations];
}

- (NSArray *)endVerticesOfEdge:(Edge *)edge {
    return [[NSArray alloc] initWithObjects:edge.origin, edge.destination, nil];
}

- (Vertex *)closestVertexToLocation:(AbsoluteLocation *)location {
    int floor = (int) location.altitude;

    double dist, bestDist = DBL_MAX;
    Vertex *closestVertex = nil;

    for (Vertex *vertex in [self verticesForFloor:floor]) {
        dist = [self getDistance
                :[vertex latitude] * 1E6 :location.latitude *1E6
                :[vertex longitude] *1E6 :location.longitude *1E6];

        if(dist < bestDist){
            bestDist = dist;
            closestVertex = vertex;
        }
    }

    return closestVertex;
}

//private method
- (double)getDistance:(double)x1: (double)x2: (double)y1: (double)y2 
{
    return sqrt(pow(x1 - x2, 2) + pow(x1 - x2, 2));
}

- (NSArray *)edgesForFloor:(int)floor {
    NSMutableArray *edges = [NSMutableArray new];

    for (Edge *edge in self.edges) {
        if (edge.origin.altitude == floor || edge.destination.altitude == floor) {
            [edges addObject:edge];
        }
    }

    return edges;
}

- (NSArray *)elevatorEndpointsForFloor:(int)floor {
    NSMutableArray *endpoints = [NSMutableArray new];
    
    for (Vertex *vertex in self.elevatorEndpoints) {
        if (((int) vertex.altitude) == floor) [endpoints addObject:vertex];
    }

    return endpoints;
}

- (NSArray *)staircaseEndpointsForFloor:(int)floor {
    NSMutableArray *endpoints = [NSMutableArray new];

    for (Vertex *vertex in self.staircaseEndpoints) {
        if (((int) vertex.altitude) == floor) [endpoints addObject:vertex];
    }

    return endpoints;
}

- (Vertex *)vertexForId:(int)id {
    return [self.vertices objectForKey:[NSNumber numberWithInt:id]];
}

- (NSArray *)verticesForFloor:(int)floor {
    return [self.verticesByFloor objectForKey:[NSNumber numberWithInt:floor]];
}

- (void)insertRadiusVerticesAtVertex:(Vertex *)source withRadius:(int)radius {
    double dist;
    for(Vertex *target in self.vertices) {
        if ([source isEqual:target]) continue;

        dist = [DistanceMeasurements distanceInMetersFromVertex:source toVertex:target];

        if (dist <= radius) [source addRadiusVertex:target];
    }
}

- (int)numEdges
{
    return [self.edges count];
}

- (int)numVertices
{
    return [self.vertices count];
}

- (BOOL)removeDirectionalEdgeFromOrigin:(Vertex *)origin toDestination:(Vertex *)destination {
    Edge *edge = [[Edge alloc] initWithOrigin:origin andDestination:destination];

    BOOL sourceModified = [origin removeOutEdge:destination] || [destination removeInEdge:origin];
    BOOL edgeModified = NO;
    if ([self.edges containsObject:edge]) {
        [self.edges removeObject:edge];
        edgeModified = YES;
    }

    return sourceModified || edgeModified;
}

- (BOOL)removeElevatorEndpoint:(Vertex *)endpoint {
    if([self.elevatorEndpoints containsObject:endpoint]) {
        [self.elevatorEndpoints removeObject:endpoint];

        return YES;
    }

    return NO;
}

- (BOOL)removeStaircaseEndpoint:(Vertex *)endpoint {
    if([self.staircaseEndpoints containsObject:endpoint]) {
        [self.staircaseEndpoints removeObject:endpoint];

        return YES;
    }

    return NO;
}

- (BOOL)removeUndirectionalEdges:(Vertex *)vertex1: (Vertex *)vertex2 {
    return [self removeDirectionalEdgeFromOrigin:vertex1 toDestination:vertex2]
    || [self removeDirectionalEdgeFromOrigin:vertex2 toDestination:vertex1];
}

- (BOOL)removeVertex:(Vertex *)vertex {
    for (Edge *edge in vertex.incidentEdges) {
        [self.edges removeObject:edge];
    }
    
    [self.vertices removeObjectForKey:[NSNumber numberWithInt:vertex.id]];
    
    if (vertex.location != nil && vertex.location.absolute != nil) {
        int floor = (int) vertex.altitude;
        
        NSMutableArray *verticesOnFloor = [self.verticesByFloor objectForKey:[NSNumber numberWithInt:floor]];
        if (verticesOnFloor) [verticesOnFloor removeObject:vertex];
    }

    return YES;
}

- (NSString *)description {
    NSMutableString* desc = [NSMutableString stringWithFormat:@"Graph with %d vertices and %d edges:",
                             self.numVertices, self.numEdges];

    [desc appendString:@"\r  Vertices:"];
    for (id vertex in [self.vertices allValues]) {
        [desc appendFormat:@"\r    %@", vertex];
    }

    [desc appendString:@"\r  Edges:"];
    for (id edge in self.edges) {
        [desc appendFormat:@"\r    %@", edge];
    }

    return desc;
}

@end
