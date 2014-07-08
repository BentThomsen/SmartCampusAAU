//
//  BuildingFloor.m
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

#import "BuildingFloor.h"

@implementation BuildingFloor

+ (BuildingFloor *)buildingFloorWithJson:(NSDictionary *)data {
    BuildingFloor *floor = [BuildingFloor new];

    floor.id = [[data valueForKey:@"ID"] intValue];
    floor.buildingId = [[data valueForKey:@"Building_ID"] intValue];
    floor.number = [[data valueForKey:@"Number"] intValue];
    floor.name = [data valueForKey:@"Name"];

    return floor;
}

- (NSDictionary *)toJSON {
    NSDictionary *data = [[NSDictionary alloc] initWithObjectsAndKeys:
                          [[NSNumber numberWithInt:self.id] stringValue], @"ID",
                          [[NSNumber numberWithInt:self.buildingId] stringValue], @"Building_ID",
                          [[NSNumber numberWithInt:self.number] stringValue], @"Number",
                          self.name, @"Name",
                          nil];

    return data;
}

- (id)init {
    return [super init];
}

- (id)initWithNumber:(int)number {
    return [self initWithName:nil number:number];
}

- (id)initWithName:(NSString *)name number:(int)number {
    return [self initWithId:0 name:name number:number];
}

- (id)initWithId:(int)id name:(NSString *)name number:(int)number {
    if (self = [super init]) {
        self.id = id;
        self.name = name,
        self.number = number;

        return self;
    } else {
        return nil;
    }
}

- (NSString *)description {
    return [NSString stringWithFormat:@"%d. %@", self.number, self.name];
}

@end
