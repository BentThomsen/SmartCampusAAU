//
//  Vertex.h
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

#import <UIKit/UIKit.h>

#import "SmartCampusOData.h"

#import "AggregateLocation.h"
#import "TablePickerController.h"

@class Edge;
@class WifiMeasurement;

@interface Vertex : NSObject <SOJSON, TablePickerItem>

@property (nonatomic, assign)int id;
@property (nonatomic, assign)int buildingId;
@property (nonatomic, strong)AggregateLocation *location;
@property (nonatomic, assign)BOOL stairEndpoint;
@property (nonatomic, assign)BOOL elevatorEndpoint;
@property (nonatomic, strong)NSMutableArray *inEdges;
@property (nonatomic, strong)NSMutableArray *outEdges;
@property (nonatomic, strong)NSMutableArray *fingerprints;
@property (nonatomic, strong)NSMutableArray *radiusVertices;

+ (Vertex *)vertexWithJson:(NSDictionary *)data;

- (id)initWithId:(int)id andAbsoluteLocation:(AbsoluteLocation *)absolute;
- (id)initWithId:(int)id andLocation:(AggregateLocation *)location;
- (id)initWithVertex:(Vertex *)vertex;

- (BOOL)addInEdge:(Edge *)edge;
- (BOOL)addInEdgeFromOrigin:(Vertex *)origin;
- (BOOL)removeInEdge:(Vertex *)origin;

- (BOOL)addOutEdge:(Edge *)edge;
- (BOOL)addOutEdgeToDestination:(Vertex *)destination;
- (BOOL)removeOutEdge:(Vertex *)destination;

- (int)numFingerprints;
- (BOOL)addFingerprint:(WifiMeasurement *)measurement;
- (BOOL)removeFingerprint:(WifiMeasurement *)measurement;

- (BOOL)addRadiusVertex:(Vertex *)value;
- (BOOL)removeRadiusVertex:(Vertex *)value;

- (NSArray *)adjacentVertices;
- (NSArray *)opposingVertices:(NSArray *)edges;

- (BOOL)hasEdge:(Edge *)edge;
- (BOOL)hasInEdge:(Edge *)edge;
- (BOOL)hasOutEdge:(Edge *)edge;

- (int)degree;
- (int)inDegree;
- (int)outDegree;

- (NSArray *)origins;
- (NSArray *)destinations;
- (NSArray *)incidentEdges;

- (double)latitude;
- (double)longitude;
- (double)altitude;

- (BOOL)isEqual:(id)object;
- (int)hashCode;

@end
