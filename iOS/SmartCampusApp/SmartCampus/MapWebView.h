//
//  MapWebView.h
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
#import <CoreLocation/CoreLocation.h>

#import "Edge.h"
#import "PositionEstimate.h"
#import "Vertex.h"

typedef enum  {
    MapWebViewTypeMap = 0,
    MapWebViewTypeSatellite = 1,
    MapWebViewTypeStreet = 2,
    MapWebViewTypeTraffic = 3
} MapWebViewType;

@protocol JSInterface <NSObject>
@required
- (void) setCenter:(CLLocationCoordinate2D)coordinate;
- (void) showSelectedLocation:(CLLocationCoordinate2D)coordinate;
- (void) updateSelectedLocation:(CGPoint)point;
- (void) clearOverlays;
- (void) setEndpoint:(NSInteger)vertexId;
- (void) removeEndpoint:(NSInteger)vertexId;
- (void) search:(NSString*)query;
- (void) showEdges:(NSArray*)edges onFloor:(NSInteger)floor;
- (void) showVertices:(NSArray*)vertices onFloor:(NSInteger)floor isOnline:(bool)online;
- (void) clearPosition;
- (void) updatePosition:(PositionEstimate*)position;
- (void) updateNewLocation:(CLLocation*)location;
- (void) updateViewType:(MapWebViewType)viewType;
- (void) setTracking:(BOOL)tracking;
- (NSString*) createJsonEdge:(Edge*)edge;
- (NSString*) createJsonLocation:(Vertex*)vertex;
@end

@interface MapWebView : UIWebView <JSInterface>

@property (strong, nonatomic) Vertex *selectedVertex;

//@property (nonatomic) BOOL showTiles;

- (void)loadURL:(NSURL *)url;

@end
