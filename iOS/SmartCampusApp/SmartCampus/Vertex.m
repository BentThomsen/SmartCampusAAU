//
//  Vertex.m
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

#import "Vertex.h"
#import "AggregateLocation.h"
#import "WifiMeasurement.h"
#import "Edge.h"

@implementation Vertex

@synthesize title = _title;

+ (Vertex *)vertexWithJson:(NSDictionary *)data {
    int vertexId = [[data valueForKey:@"ID"] intValue];
    AggregateLocation *location = [AggregateLocation new];

    NSArray *absolutes = [data valueForKey:@"AbsoluteLocations"];
    NSArray *symbolics = [data valueForKey:@"SymbolicLocations"];

    if ([absolutes count] == 1) {
        id absoluteData = [absolutes objectAtIndex:0];
        location.absolute = [AbsoluteLocation locationWithJson:absoluteData];
    }

    if ([symbolics count] == 1) {
        id symbolicData = [symbolics objectAtIndex:0];
        location.symbolic = [SymbolicLocation locationWithJson:symbolicData];
    }

    Vertex *vertex = [[Vertex alloc] initWithId:vertexId andLocation:location];
    vertex.buildingId = [[data valueForKey:@"Building_ID"] intValue];

    return vertex;
}

- (NSDictionary *)toJSON {
    NSDictionary *data = [[NSDictionary alloc] initWithObjectsAndKeys:
                          [[NSNumber numberWithInt:self.id] stringValue], @"ID",
                          [[NSNumber numberWithInt:self.buildingId] stringValue], @"Building_ID",
                          [NSArray arrayWithObject:[self.location.absolute toJSON]], @"AbsoluteLocations",
                          [NSArray arrayWithObject:[self.location.symbolic toJSON]], @"SymbolicLocations",
                          nil];

    return data;
}

- (id)initWithId:(int)id andAbsoluteLocation:(AbsoluteLocation *)absolute {
    return [self initWithId:id andLocation:[[AggregateLocation alloc] initWithAbsolute:absolute]];
}

- (id)initWithId:(int)id andLocation:(AggregateLocation *)location {
    if (self = [super init]) {
        self.id = id;
        self.location = location;
        self.inEdges = [NSMutableArray new];
        self.outEdges = [NSMutableArray new];
        self.fingerprints = [NSMutableArray new];
        self.radiusVertices = [NSMutableArray new];
    }

    return self;
}

- (id)initWithVertex:(Vertex *)vertex {
    if (self = [super init]) {
        self.id = vertex.id;
        self.location = [[AggregateLocation alloc] initWithAbsolute:[vertex.location.absolute copy]];
        self.inEdges = [vertex.inEdges copy];
        self.outEdges = [vertex.outEdges copy];
        self.fingerprints = [vertex.fingerprints copy];
        self.radiusVertices = [vertex.radiusVertices copy];
    }
    
    return self;
}

- (BOOL)addInEdge:(Edge *)edge {
    if (edge == nil || [self.inEdges containsObject:edge]) return NO;

    self.stairEndpoint = edge.staircase;
    self.elevatorEndpoint = edge.elevator;

    [self.inEdges addObject:edge];
    edge.destination = self;

    return YES;
}

- (BOOL)addInEdgeFromOrigin:(Vertex *)origin {
    if (origin == nil) return NO;

    [self addInEdge:[[Edge alloc] initWithOrigin:origin andDestination:self]];

    return YES;
}

- (BOOL)removeInEdge:(Vertex *)origin {
    Edge *edge = [[Edge alloc] initWithOrigin:origin andDestination:self];
    if (![self.inEdges containsObject:edge]) return NO;

    [self.inEdges removeObject:edge];

    return YES;
}

- (BOOL)addOutEdge:(Edge *)edge {
    if (edge == nil || [self.outEdges containsObject:edge]) return NO;

    self.stairEndpoint = edge.staircase;
    self.elevatorEndpoint = edge.elevator;

    [self.outEdges addObject:edge];
    edge.origin = self;

    return YES;
}

- (BOOL)addOutEdgeToDestination:(Vertex *)destination {
    if (destination == nil) return NO;

    [self addOutEdge:[[Edge alloc] initWithOrigin:self andDestination:destination]];

    return YES;
}

- (BOOL)removeOutEdge:(Vertex *)destination {
    Edge *edge = [[Edge alloc] initWithOrigin:self andDestination:destination];
    if (![self.outEdges containsObject:edge]) return NO;

    [self.outEdges removeObject:edge];

    return YES;
}

- (BOOL)hasFingerprints {
    return self.fingerprints != nil && [self numFingerprints] > 0;
}

- (int)numFingerprints {
    return [self.fingerprints count];
}

- (BOOL)addFingerprint:(WifiMeasurement *)measurement {
    if ([self hasFingerprints] || measurement == nil) return NO;
    
    [self.fingerprints addObject:measurement];
    measurement.vertex = self;

    return YES;
}

- (BOOL)removeFingerprint:(WifiMeasurement *)measurement {
    if (![self.fingerprints containsObject:measurement]) return NO;

    [self.fingerprints removeObject:measurement];

    return YES;
}

- (BOOL)addRadiusVertex:(Vertex *)vertex {
    for (Vertex *object in [self adjacentVertices]) {
        if ([object isEqual:vertex]) return NO;
    }

    [self.radiusVertices addObject:vertex];

    return YES;
}

- (BOOL)removeRadiusVertex:(Vertex *)vertex {
    if (![self.radiusVertices containsObject:vertex]) return NO;

    [self.radiusVertices removeObject:vertex];

    return YES;
}

- (NSArray *)adjacentVertices {
    NSMutableArray *adjacent = [NSMutableArray new];

    [adjacent addObjectsFromArray:[self opposingVertices:self.inEdges]];
    [adjacent addObjectsFromArray:[self opposingVertices:self.outEdges]];

    return adjacent;
}

- (NSArray *)opposingVertices:(NSArray *)edges {
    NSMutableArray *opposing = [NSMutableArray new];

    for (Edge *edge in edges) {
        [opposing addObject:[edge oppositeVertexOfVertex:self]];
    }

    return opposing;
}

- (BOOL)hasEdge:(Edge *)edge {
    return [self hasInEdge:edge] || [self hasOutEdge:edge];
}

- (BOOL)hasInEdge:(Edge *)edge {
    return [self.inEdges containsObject:edge];
}

- (BOOL)hasOutEdge:(Edge *)edge {
    return [self.outEdges containsObject:edge];
}

- (int)degree {
    return [self inDegree] + [self outDegree];
}

- (int)inDegree {
    return [self.inEdges count];
}

- (int)outDegree {
    return [self.outEdges count];
}

- (NSArray *)origins {
    return [self opposingVertices:self.inEdges];
}

- (NSArray *)destinations {
    return [self opposingVertices:self.outEdges];
}

- (BOOL)isEqual:(id)object {
    if (object == self) return YES;
    if (!object || ![object isKindOfClass:[self class]]) return NO;

    Vertex *other = (Vertex *) object;
    return [self.location isEqual:other.location];
}

- (int)hashCode {
    return [self.location hashcode];
}    

- (NSArray *)incidentEdges {
    NSMutableArray *incident = [NSMutableArray new];

    [incident addObjectsFromArray:self.inEdges];
    [incident addObjectsFromArray:self.outEdges];

    return incident;
}

- (double)latitude {
    return self.location.absolute.latitude;
}

- (double)longitude {
    return self.location.absolute.longitude;
}

- (double)altitude {
    return self.location.absolute.altitude;
}

- (NSString *)description {
    AbsoluteLocation* absolute = self.location.absolute;
    return [NSString stringWithFormat:@"(%g, %g, %g)",
            absolute.latitude, absolute.longitude, absolute.altitude];
}

#pragma mark TablePickerItem
- (NSString *)title {
    if (_title == nil) {
        AggregateLocation *location = self.location;

        if (location.symbolic != nil) {
            _title = location.symbolic.title;
        } else {
            _title = [NSString stringWithFormat:@"%g, %g",
                      location.absolute.latitude, location.absolute.longitude];
        }
    }

    return _title;
}

@end
