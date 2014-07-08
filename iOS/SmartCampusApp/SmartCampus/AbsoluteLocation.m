//
//  AbsoluteLocation.m
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

#import "AbsoluteLocation.h"
#import "Constants.h"

@implementation AbsoluteLocation

+ (AbsoluteLocation *)locationWithJson:(NSDictionary *)data {
    double latitude = [[data valueForKey:@"latitude"] doubleValue];
    double longitude = [[data valueForKey:@"longitude"] doubleValue];
    double altitude = [[data valueForKey:@"altitude"] doubleValue];

    AbsoluteLocation *location = [[AbsoluteLocation alloc] initWithLatitude:latitude longitude:longitude altitude:altitude];
    location.id = [[data valueForKey:@"ID"] intValue];
    location.vertexId = [[data valueForKey:@"Vertex_ID"] intValue];

    return location;
}

- (NSDictionary *)toJSON {
    NSDictionary *data = [[NSDictionary alloc] initWithObjectsAndKeys:
                          [[NSNumber numberWithInt:self.id] stringValue], @"ID",
                          [[NSNumber numberWithInt:self.vertexId] stringValue], @"Vertex_ID",
                          [[NSNumber numberWithDouble:self.latitude] stringValue], @"latitude",
                          [[NSNumber numberWithDouble:self.longitude] stringValue], @"longitude",
                          [[NSNumber numberWithDouble:self.altitude] stringValue], @"longitude",
                          nil];

    return data;
}

- (id)initWithLatitude:(double)latitude
             longitude:(double)longitude
              altitude:(double)altitude {
    if (self = [super init]) {
        self.latitude = latitude;
        self.longitude = longitude;
        self.altitude = altitude;

        return self;
    } else {
        return nil;
    }
}

- (CLLocationCoordinate2D)toLocationCoordinate {
    CLLocationCoordinate2D location;

    location.latitude = self.latitude;
    location.longitude = self.longitude;

    return location;
}

- (BOOL) isEqual:(id)object {
    if(object == self) return YES;
    if(!object || ![object isKindOfClass:[self class]]) return NO;

    AbsoluteLocation *other = (AbsoluteLocation *) object;

    return (self.latitude == other.latitude)
        && (self.longitude == other.longitude)
        && (self.altitude == other.altitude);
}

- (int)hashCode {
    return ((int) (self.latitude * 1e6))
        ^ ((int) (self.longitude * 1e6))
        ^ ((int) (self.altitude * 1e6));
}

#pragma mark NSCopying protocol
-(id) copyWithZone: (NSZone *) zone {
    AbsoluteLocation *copy = [[[self class] alloc] init];

    copy.latitude = self.latitude;
    copy.longitude = self.longitude;
    copy.altitude = self.altitude;

    return copy;
}

@end
