//
//  WifiMeasurement.h
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
#import <Foundation/NSDate.h>
#import "Vertex.h"
#import "MacInfo.h"
#import "Histogram.h"

@interface WifiMeasurement : NSObject
{
    NSDate *_measTimeStart, *_measTimeEnd;
    Vertex *_vertex;
    NSMutableDictionary *_histogram;
    NSMutableDictionary *_additionalInfo;

#define NO_VERTEX 42
    
}

-(id) init;
-(id) initWithVertex:(Vertex *) v;
-(id) initWithVertexStartTimeEndTime:(Vertex *) v: (NSDate *) measTimeStart: (NSDate *) measTimeEnd;
+(NSString *) discardLastCharIfMacIsFull:(NSMutableString *) mac;
-(void) addValue:(NSString *) mac: (int) ssVal;
-(void) addValue:(NSString *) mac: (int) ssVal: (MacInfo *) macInfo;
-(BOOL) isEqual: (id)object;
-(int) getAvgDbM:(NSString *) mac;
-(NSArray *) getHistogram:(NSString *) mac;
-(BOOL) containsMac:(NSString *) mac;
-(NSDictionary *) getHistograms;
-(MacInfo *) getMacInfo:(NSString *) mac; 
-(NSDictionary *) getMacInfos;
-(NSArray *) getMACs;
-(NSDate *) getMEasTimeEnd;
-(NSDate *) getMeasTimeStart;
-(int) getNumMACs;
-(double) getStdDev:(NSString *) mac;
-(int) getStrongestDb:(NSString *) mac;
-(Vertex *) getVertex;
-(int) getWeakestDbM:(NSString *) mac;
-(int) hashCode;
-(void) removeMac:(NSString *) mac;
-(void) removeMacs:(NSArray *) macs;
-(void) setHistogram:(Histogram *) hist;
-(void) setMeasTimeEnd:(NSDate *) time;
-(void) setMeasTimeStart:(NSDate *) time;
-(void) setVertex:(Vertex *) v;

@end
