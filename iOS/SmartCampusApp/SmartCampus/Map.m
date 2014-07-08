//
//  Map.m
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

#import "Map.h"
#import "MapWebViewController.h"
#import "TablePickerController.h"

@implementation Map

- (id)initWithMapWebView:(MapWebView *)mapWebView {
    self = [super init];

    if (self) {
        self.mapWebView = mapWebView;
    }

    return self;    
}

- (BOOL)isOnline {
    [NSException raise:NSInternalInconsistencyException
                format:@"You must override %@ in a subclass", NSStringFromSelector(_cmd)];
    return NO;
}

- (void)setupNearbyController:(TablePickerController*)tablePickerViewController {
    NSArray *floorVerts;

    if ([Building hasActiveBuilding]) {
        floorVerts = [self getVisibleVertices:[[Building activeBuilding].graph verticesForFloor:[Building currentFloor]]];
    }

    [tablePickerViewController setItems:floorVerts];
    tablePickerViewController.title = NSLocalizedString(@"Select nearby", nil);
}

- (NSArray*)getVisibleVertices:(NSArray*)vertices {
    [self doesNotRecognizeSelector:_cmd];
    return nil;    
}

- (void)setMapReady {
}

- (void)onTap:(NSInteger)floor vertexId:(NSInteger)vertexId {
    [NSException raise:NSInternalInconsistencyException 
                format:@"You must override %@ in a subclass", NSStringFromSelector(_cmd)];
}

- (void)setSelectedLocation:(BOOL)isOnline floor:(NSInteger)floor latitude:(NSNumber*)lat longitude:(NSNumber*)lng {
    [NSException raise:NSInternalInconsistencyException 
                format:@"You must override %@ in a subclass", NSStringFromSelector(_cmd)];
}

@end
