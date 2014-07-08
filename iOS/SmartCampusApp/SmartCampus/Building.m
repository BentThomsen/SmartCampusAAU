//
//  Building.m
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

#import "Building.h"

#import "BuildingFloor.h"
#import "DictionaryGraph.h"
#import "Graph.h"

@implementation Building

static Building *_activeBuilding = nil;
static int _currentFloor = 0;

+ (BOOL)hasActiveBuilding {
    return _activeBuilding != nil;
}

+ (Building *)activeBuilding {
    if(_activeBuilding == nil) _activeBuilding = [Building new];

    return _activeBuilding;
}

+ (void)setActiveBuilding:(Building *)building {
    _activeBuilding = building;
}

+ (int)currentFloor {
    return _currentFloor;
}

+ (void)setCurrentFloor:(int)floor {
    _currentFloor = floor;
}

+ (Building *)buildingWithJson:(NSDictionary *)data {
    Building *building = [Building new];

    building.id = [[data valueForKey:@"ID"] intValue];
    building.name = [data valueForKey:@"Building_Name"];
    building.address = [data valueForKey:@"Max_Address"];
    building.postalCode = [data valueForKey:@"Postal_Code"];
    building.country = [data valueForKey:@"Country"];
    building.latitude = [[data valueForKey:@"Lat"] doubleValue];
    building.longitude = [[data valueForKey:@"Lon"] doubleValue];
    building.url = [data valueForKey:@"Url"];
    building.ifcUrl = [data valueForKey:@"Ifc_Url"];
    building.graph = [DictionaryGraph graphWithJson:data];

    for (id buildingFloorData in [data valueForKey:@"Building_Floors"]) {
        [building addFloor:[BuildingFloor buildingFloorWithJson:buildingFloorData]];
    }

    return building;
}

- (NSDictionary *)toJSON {
    NSDictionary *data = [[NSDictionary alloc] initWithObjectsAndKeys:
                          self.name, @"Building_Name",
                          self.address, @"Max_Address",
                          self.postalCode, @"Postal_Code",
                          self.country, @"Country",
                          [[NSNumber numberWithDouble:self.latitude] stringValue], @"Lat",
                          [[NSNumber numberWithDouble:self.longitude] stringValue], @"Lon",
                          self.url, @"Url",
                          self.ifcUrl, @"Ifc_Url",
                          nil];

    return data;
}

- (Building *)init {
    if (self = [super init]) {
        //self.mapUrls = [NSMutableArray new];
        //self.permissableAccessPoints = [NSArray new];
        self.floors = [NSMutableDictionary new];
        self.graph = [DictionaryGraph new];

        return self;
    } else {
        return nil;
    }
}

- (int)minimumFloorNumber {
    if (![self hasFloors]) return 0;

    int minimum = INT32_MAX;

    for (BuildingFloor *floor in [self.floors allValues]) {
        if (floor.number < minimum) minimum = floor.number;
    }

    return minimum;
}

- (int)maximumFloorNumber {
    if (![self hasFloors]) return 0;

    int maximum = INT32_MIN;

    for (BuildingFloor *floor in [self.floors allValues]) {
        if (floor.number > maximum) maximum = floor.number;
    }

    return maximum;
}

- (int)numFloors {
    return [self.floors count];
}

/*
- (BOOL)hasMapUrlForStory:(int)story {
    return [self mapUrlForStory:story] == nil;
}

- (NSString *)mapUrlForStory:(int)story {
    return [self.mapUrls objectAtIndex:story];
}

- (void)addMapUrl:(NSString *)url forStory:(int)story {
    self.stories++;

    if(![self hasMapUrlForStory:story]) {
        [self.mapUrls insertObject:url atIndex:story];
    }
}
 */

- (BOOL)hasFloors {
    return [self.floors count] > 0;
}

- (BOOL)hasFloorAt:(int)number {
    return [self floorAt:number] != nil;
}

- (BuildingFloor *)floorAt:(int)number {
    return [self.floors objectForKey:[NSNumber numberWithInt:number]];
}

- (BOOL)addFloor:(BuildingFloor *)floor {
    if (![self hasFloorAt:floor.number]) {
        [self.floors setObject:floor forKey:[NSNumber numberWithInt:floor.number]];

        return YES;
    }

    return NO;
}    

-(BuildingFloor *) removeFloorAt:(int)number {
    BuildingFloor *floor = [self floorAt:number];

    [self.floors removeObjectForKey:[NSNumber numberWithInt:number]];

    return floor;
}

- (NSString *)description {
    NSMutableString *floors = [NSMutableString stringWithFormat:@"%d floors:", [self numFloors]];

    for (id floor in [self.floors allValues]) {
        [floors appendFormat:@"\r  %@", floor];
    }

    return [NSString stringWithFormat:@"%@, %@\r%@\r%@",
            self.name, self.address, floors, [self.graph description]];
}

@end
