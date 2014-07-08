//
//  Vector.m
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

#import <math.h>
#import "Vector2d.h"

@implementation Vector2d

@synthesize x = _x;
@synthesize y = _y;

-(id)initWithX:(double)x y:(double)y {
    if (self = [super init]) {
        self.x = x;
        self.y = y;
    }
    return self;    
}

-(double)clockwiseAngleToVector:(Vector2d*)vector {
    double cross = [self cross:vector];
    double angle = (M_PI * 2) - [self angle:vector];
    if (cross < 0)
        angle = (M_PI * 2) - angle;
    
    return fmodf(angle, (2 * M_PI));
}

-(double)cross:(Vector2d*)vector {
    return self.x * vector.y - vector.x * self.y;
}

-(double)length {
    return sqrt(self.x*self.x + self.y*self.y);
}

-(double)dot:(Vector2d*)vector {
    return (self.x*vector.x + self.y*vector.y);
}

-(double)angle:(Vector2d*)vector {
    double vDot = [self dot:vector] / ([self length]*[vector length]);
    if( vDot < -1.0) vDot = -1.0;
    if( vDot >  1.0) vDot =  1.0;
    return acos(vDot);
}
@end
