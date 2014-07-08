//
//  DistanceMeasurements.m
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

#import "DistanceMeasurements.h"

@implementation DistanceMeasurements

+ (double)distanceInKilometersFromVertex:(Vertex *)from toVertex:(Vertex *)to {
    double distance = DBL_MIN;

    double dLat1InRad = from.latitude * (M_PI / 180);
    double dLong1InRad = from.longitude * (M_PI / 180);

    double dLat2InRad = to.latitude * (M_PI / 180);
    double dLong2InRad = to.longitude * (M_PI / 180);

    double dLongitude = dLong2InRad - dLong1InRad;
    double dLatitude = dLat2InRad - dLat1InRad;

    double a = pow(sin(dLatitude / 2), 2) + cos(dLat1InRad) * cos(dLat2InRad) * pow(sin(dLongitude / 2), 2);

    //Converts rectangular coordinates (x, y) to polar (r, theta).
    double c = 2 * atan2(sqrt(a), sqrt(1 - a));

    distance = EARTH_RADIUS_IN_KILOMETERS * c;

    return distance;
}

+ (double)distanceInMetersFromVertex:(Vertex *)from toVertex:(Vertex *)to {
    return METERS_PER_KILOMETER * [self distanceInKilometersFromVertex:from toVertex:to];
}

@end
