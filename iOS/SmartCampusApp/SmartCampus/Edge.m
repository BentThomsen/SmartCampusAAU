//
//  Edge.m
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

#import "Edge.h"

#import "DistanceMeasurements.h"
#import "Vertex.h"

@implementation Edge

- (id)initWithOrigin:(Vertex *)origin andDestination:(Vertex *)destination {
    int distance = [DistanceMeasurements distanceInMetersFromVertex:origin toVertex:destination];

    return [self initWithOrigin:origin andDestination:destination distance:distance];
}

- (id)initWithOrigin:(Vertex *)origin andDestination:(Vertex *)destination distance:(int)distance {
    if (self = [super init]) {
        self.origin = origin;
        self.destination = destination;
        self.distance = distance;
    }

    return self;
}

- (NSDictionary *)toJSON {
    NSDictionary *data = [[NSDictionary alloc] initWithObjectsAndKeys:
                          [[NSNumber numberWithInt:self.id] stringValue], @"ID",
                          [[NSNumber numberWithInt:self.buildingId] stringValue], @"Building_ID",
                          [[NSNumber numberWithInt:self.origin.id] stringValue], @"vertexOrigin",
                          [[NSNumber numberWithInt:self.destination.id] stringValue], @"vertexDestination",
                          [[NSNumber numberWithBool:self.directional] stringValue], @"directional",
                          [[NSNumber numberWithBool:self.staircase] stringValue], @"staircase",
                          [[NSNumber numberWithBool:self.elevator] stringValue], @"elevator",
                          nil];
    
    return data;
}

- (BOOL)isEqual:(id)object {
    if (object == self) return YES;
    if (!object || ![object isKindOfClass:[self class]]) return NO;

    Edge *other = (Edge *) object;
    return [self.origin isEqual:other.origin]
        && [self.destination isEqual:other.destination];
}

- (int)hashCode {
    return [self.origin hashCode] / [self.destination hashCode];
}

- (BOOL)isDirectional {
    return self.directional;
}

- (BOOL)isElevator {
    return self.elevator;
}

- (BOOL)isStaircase {
    return self.staircase;
}

- (Vertex *)oppositeVertexOfVertex:(Vertex *)vertex {
    if ([vertex isEqual:self.origin]) return self.destination;
    if ([vertex isEqual:self.destination]) return self.origin;
    return nil;
}

- (void)setStaircase:(BOOL)staircase {
    if (self.origin != nil && self.destination != nil) {
        self.origin.stairEndpoint = self.staircase;
        self.destination.stairEndpoint = self.staircase;
    }
}

- (NSString *)description {
    return [NSString stringWithFormat:@"%@ -> %@ = %d",
            self.origin, self.destination, self.distance];
}

@end
