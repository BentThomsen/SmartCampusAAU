//
//  Building.h
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

#import <Foundation/Foundation.h>

#import "SOJSON.h"

#import "BuildingFloor.h"
#import "Graph.h"

@interface Building : NSObject<SOJSON>

@property (nonatomic, assign) int id;
@property (nonatomic, copy) NSString *name;
@property (nonatomic, copy) NSString *ifcUrl;
@property (nonatomic, assign) double latitude;
@property (nonatomic, assign) double longitude;
@property (nonatomic, copy) NSString *country;
@property (nonatomic, copy) NSString *postalCode;
@property (nonatomic, copy) NSString *address;
@property (nonatomic, copy) NSString *url;
//@property (nonatomic, strong) NSMutableArray *mapUrls;
//@property (nonatomic, strong) NSArray *permissableAccessPoints;
@property (nonatomic, assign) int stories;
@property (nonatomic, strong) NSMutableDictionary *floors;
@property (nonatomic, strong) Graph *graph;

+ (BOOL)hasActiveBuilding;
+ (Building *)activeBuilding;
+ (void)setActiveBuilding:(Building *)building;

+ (Building *)buildingWithJson:(NSDictionary *)data;
- (NSDictionary *)toJSON;

+ (int)currentFloor;
+ (void)setCurrentFloor:(int)floor;

- (int)minimumFloorNumber;
- (int)maximumFloorNumber;
- (int)numFloors;

//- (BOOL)hasMapUrlForStory:(int)story;
//- (NSString *)mapUrlForStory:(int)story;
//- (void)addMapUrl:(NSString *)url forStory:(int)story;

- (BOOL)hasFloors;
- (BOOL)hasFloorAt:(int)number;

- (BuildingFloor *)floorAt:(int)number;

- (BOOL)addFloor:(BuildingFloor *)floor;
- (BuildingFloor *)removeFloorAt:(int)number;

@end
