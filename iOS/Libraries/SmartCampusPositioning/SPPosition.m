//
//  SPPosition.m
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

#import "SPPosition.h"

@implementation SPPosition

+ (SPPosition *)positionWithJson:(NSDictionary *)data {
    SPPosition *position = [SPPosition new];

    NSString *time = [data valueForKey:@"Time"];
    time = [time substringFromIndex:6];
    time = [time substringToIndex:[time length] - 2];

    position.provider = SPPositionProviderWiFi;
    position.timestamp = [NSDate dateWithTimeIntervalSince1970:[time doubleValue] / 1000.0];
    position.latitude = [[data valueForKey:@"Latitude"] doubleValue];
    position.longitude = [[data valueForKey:@"Longitude"] doubleValue];
    position.altitude = [[data valueForKey:@"Altitude"] doubleValue];
    position.accuracy = [[data valueForKey:@"Accuracy"] doubleValue];
    position.speed = [[data valueForKey:@"Speed"] doubleValue];
    position.course = [[data valueForKey:@"Bearing"] doubleValue];
    position.hasAccuracy = [[data valueForKey:@"HasAccuracy"] boolValue];
    position.hasSpeed = [[data valueForKey:@"HasSpeed"] boolValue];
    position.hasCourse = [[data valueForKey:@"HasBearing"] boolValue];
    position.buildingId = [[data valueForKey:@"Building_ID"] intValue];
    position.vertexId = [[data valueForKey:@"VertexID"] intValue];
    position.floor = [[data valueForKey:@"Altitude"] intValue];

    return position;
}

+ (SPPosition *)positionWithLocation:(CLLocation *)location {
    SPPosition *position = [SPPosition new];

    position.provider = SPPositionProviderGPS;
    position.timestamp = location.timestamp;
    position.latitude = location.coordinate.latitude;
    position.longitude = location.coordinate.longitude;
    position.altitude = location.altitude;
    position.accuracy = location.horizontalAccuracy;
    position.speed = location.speed;
    position.course = location.course;
    position.hasAccuracy = YES;
    position.hasSpeed = YES;
    position.hasCourse = YES;

    return position;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"(%+.6f, %+.6f, %+.6f) +/- %+.6f",
            self.latitude, self.longitude, self.altitude, self.accuracy];
}

@end
