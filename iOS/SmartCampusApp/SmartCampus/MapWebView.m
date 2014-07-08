//
//  MapWebView.m
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

#import "MapWebView.h"

#import "NSObject+SBJson.h"

#import "AppDelegate.h"
#import "AbsoluteLocation.h"
#import "MapOnline.h"
#import "MapOffline.h"
#import "PositionEstimate.h"
#import "SymbolicLocation.h"

@implementation MapWebView

- (void)awakeFromNib {
    [self initVars];
}

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [self initVars];
    }
    return self;
}

- (void) initVars {
}

- (void)loadURL:(NSURL *)url {
    [self loadRequest:[NSURLRequest requestWithURL:url]];
}

#pragma mark - JSInterface
- (void)setCenter:(CLLocationCoordinate2D)coordinate {
    [self stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:@"javascript:centerAt(%g,%g);",coordinate.latitude,coordinate.longitude]];
}

- (void) showSelectedLocation:(CLLocationCoordinate2D)coordinate {
    [self stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:@"javascript:showSelectedLocation(%g,%g);",coordinate.latitude,coordinate.longitude]];
}

- (void) updateSelectedLocation:(CGPoint)point {
    [self stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:@"javascript:updateSelectedLocation(%g,%g);",point.x, point.y]];
}

- (void) clearOverlays {
    [self stringByEvaluatingJavaScriptFromString:@"javascript:clearOverlays();"];
}

- (void) setEndpoint:(NSInteger)vertexId {
    [self stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:@"javascript:setEndpoint(%d);",vertexId]];
}

- (void) removeEndpoint:(NSInteger)vertexId {
    [self stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:@"javascript:removeEndpoint(%d);",vertexId]];
}

- (void) search:(NSString *)query {
    [self stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:@"javascript:search(%@);",query]];
}

- (void) showEdges:(NSArray*)edges onFloor:(NSInteger)floor {
    NSMutableString *result = [NSMutableString stringWithFormat:@"javascript:showEdges(%d, [", floor];

    NSString *sep = @"";
    for (Edge *edge in edges) {
        [result appendString:sep];
        [result appendString:[self createJsonEdge:edge]];
        sep = @", ";
    }

    [result appendString:@"])"];
    [self stringByEvaluatingJavaScriptFromString:result];
}

- (void) showVertices:(NSArray*)vertices onFloor:(NSInteger)floor isOnline:(bool)online {
    NSMutableString *result = [NSMutableString stringWithFormat:@"javascript:addGraphOverlay(%d, %@, [", floor, online ? @"true" : @"false"];

    NSString *sep = @"";
    for (Vertex *vertex in vertices) {
        [result appendString:sep];
        [result appendString:[self createJsonLocation:vertex]];
        sep = @", ";
    }

    [result appendString:@"])"];
    [self stringByEvaluatingJavaScriptFromString:result];
}

- (void) setTracking:(BOOL)tracking {
    NSString *js = [NSString stringWithFormat:@"javascript:setIsTracking(\"%@\");", (tracking ? @"true" : @"false")];
    [self stringByEvaluatingJavaScriptFromString:js];
}

- (void) clearPosition {
    [self stringByEvaluatingJavaScriptFromString:@"javascript:clearLocation();"];
}

- (void) updatePosition:(PositionEstimate*)position {
    NSString *js = [NSString stringWithFormat:@"javascript:updateNewLocation({ latitude:%g, longitude:%g, accuracy:%g });",
                    position.latitude, position.longitude, position.accuracy];
    [self stringByEvaluatingJavaScriptFromString:js];
}

- (void) updateNewLocation:(CLLocation*)location {
}

- (void) updateViewType:(MapWebViewType)viewType {
}

#pragma mark - JSON
- (NSString*) createJsonEdge:(Edge*)edge {
    NSDictionary *dict = [NSDictionary dictionaryWithObjectsAndKeys:
                          [self createJsonEndpoint:edge.origin], @"endpoint1",
                          [self createJsonEndpoint:edge.destination], @"endpoint2",
                          nil
                         ];

    return [dict JSONRepresentation];
}

- (NSDictionary*) createJsonEndpoint:(Vertex*)vertex {
    return [NSDictionary dictionaryWithObjectsAndKeys:
             [NSNumber numberWithInt:vertex.id], @"id",
             [NSNumber numberWithDouble:vertex.latitude], @"lat",
             [NSNumber numberWithDouble:vertex.longitude], @"lon",
             nil
           ];
}

- (NSString*) createJsonLocation:(Vertex*)vertex {
    NSMutableDictionary *location = [NSMutableDictionary dictionaryWithObjectsAndKeys:
                               [NSNumber numberWithInt:vertex.id], @"id",
                               [NSNumber numberWithDouble:vertex.latitude], @"latitude",
                               [NSNumber numberWithDouble:vertex.longitude], @"longitude",
                               [NSNumber numberWithDouble:vertex.altitude], @"altitude",
                               vertex.stairEndpoint, @"isStairEndpoint",
                               vertex.elevatorEndpoint, @"isElevatorEndpoint",
                               @"null", @"title",
                               nil
                             ];

    SymbolicLocation *symbolic = vertex.location.symbolic;
    if (symbolic) {
        [location setValue:symbolic.title forKey:@"title"];
        [location setValue:symbolic.description forKey:@"description"];
        [location setValue:symbolic.url forKey:@"url"];
        [location setValue:[NSNumber numberWithInt:symbolic.type] forKey:@"location_type"];
        [location setValue:[NSNumber numberWithBool:symbolic.entrance] forKey:@"isEntrance"];
    }

    return [location JSONRepresentation];
}

@end
