//
//  WifiMeasurement.m
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

#import "WifiMeasurement.h"
#import <Foundation/NSDate.h>

@implementation WifiMeasurement

-(id) init 
{
    return [self initWithVertexStartTimeEndTime:nil :[NSDate date] :[NSDate date]];
}

-(id) initWithVertex:(Vertex *) v
{
    return [self initWithVertexStartTimeEndTime:v :[NSDate date] :[NSDate date]];
}

-(id) initWithVertexStartTimeEndTime:(Vertex *) v: (NSDate *) measTimeStart: (NSDate *) measTimeEnd
{
    if(!(self = [super init]))
        return nil;
    
    self->_measTimeStart = measTimeStart;
    self->_measTimeEnd = measTimeEnd;
    self->_vertex = v;
    self->_histogram = [[NSMutableDictionary alloc] init];
    self->_additionalInfo = [[NSMutableDictionary alloc] init];
    
    return self;
}

+(NSMutableString *) discardLastCharIfMacIsFull:(NSMutableString *) mac
{
    NSMutableString *result;
    if([mac length] == 17)
        result = [[mac substringToIndex:16] copy];
    else
        result = mac;
    return result;
}

-(void) addValue:(NSMutableString *) mac: (int) ssVal
{
    mac = [[WifiMeasurement discardLastCharIfMacIsFull:mac] copy];
    
    
    if(![self->_histogram objectForKey:mac])
    {
        [self->_histogram setValue:[[NSMutableArray alloc] init] forKey:mac];
    } 
    if(![(NSMutableArray *)[self->_histogram objectForKey:mac] objectAtIndex:ssVal])
    {
        [(NSMutableArray *)[self->_histogram objectForKey:mac] insertObject:0 atIndex:ssVal];
    }
    
    NSNumber *existingCount = [NSNumber numberWithInt:(NSInteger)[(NSArray *)[self->_histogram objectForKey:mac] objectAtIndex:ssVal] + 1];
    
    [(NSMutableArray *)[self->_histogram objectForKey:mac] insertObject:existingCount atIndex:ssVal];
    
}

-(void) addValue:(NSMutableString *) mac: (int) ssVal: (MacInfo *) macInfo
{
    mac = [[WifiMeasurement discardLastCharIfMacIsFull:mac] copy];
    if(macInfo != nil)
        [self->_additionalInfo setObject:macInfo forKey:mac];
    
    [self addValue:mac :ssVal];
}

-(BOOL) isEqual: (id)object
{
    if(object == nil)
        return NO;
    
    if(![object isKindOfClass:[self class]])
        return NO;
    
    return [self hashCode] == [object hashCode];
}

-(int) getAvgDbM:(NSString *) mac
{
    int totalVal = 0;
    int totalCount = 0;
    NSNumber *curCount = 0;
    for(NSNumber *curVal in (NSMutableArray *)[self->_histogram objectForKey:mac])
    {
        curCount = [(NSArray *)[self->_histogram objectForKey:mac] objectAtIndex:[curVal intValue]];
        totalVal += [curVal intValue] * [curCount intValue];
        totalCount += [curCount intValue];
    }
    
    return totalVal / totalCount;   
}

-(NSArray *) getHistogram:(NSString *) mac 
{
    return [self->_histogram objectForKey:mac];
}

-(BOOL) containsMac:(NSString *) mac
{
    if([self->_histogram objectForKey:mac])
        return YES;
    else
        return NO;
}

-(NSDictionary *) getHistograms
{
    return self->_histogram;
}

-(MacInfo *) getMacInfo:(NSString *) mac
{
    return [self->_additionalInfo objectForKey:mac];
}

-(NSDictionary *) getMacInfos
{
    return self->_additionalInfo;
}

-(NSArray *) getMACs
{
    return [self->_histogram allKeys];
}
-(NSDate *) getMEasTimeEnd
{
    return self->_measTimeEnd;
}

-(NSDate *) getMeasTimeStart
{
    return self->_measTimeStart;
}

-(int) getNumMACs
{
    return [[self->_histogram allKeys] count];
}
-(double) getStdDev:(NSString *) mac
{
    double total = 0;
    int mean = [self getAvgDbM:mac];
    int allValues = 0;
    
    
    for(NSNumber *val in [(NSDictionary *)[self->_histogram objectForKey:mac] allKeys])
    {
        NSNumber *numVals = [(NSDictionary *)[self->_histogram objectForKey:mac] objectForKey:val];
        allValues += [numVals intValue];
        
        for(int i = 1; i <= [numVals intValue]; i++)
            total += pow([val intValue] - mean, 2);
    }
    return sqrt(total / allValues); 
}
-(int) getStrongestDb:(NSString *) mac
{
    int max = -255; //lowest possible RSSI value
    for(NSNumber *ss in [(NSDictionary *)[self->_histogram objectForKey:mac] allKeys])
        if([ss intValue] > max)
            max = [ss intValue];
    return max;   
}

-(Vertex *) getVertex
{
    return self->_vertex;
}
-(int) getWeakestDbM:(NSString *) mac
{
    int min = 0;    
    for(NSNumber *ss in [(NSDictionary *)[self->_histogram objectForKey:mac] allKeys])
        if([ss intValue] > min)
            min = [ss intValue];
    return min;  
}
-(int) hashCode
{
    int hash;
    if(self->_vertex != nil)
        hash = [self->_measTimeStart hash] ^ [self->_vertex hashCode];
    else
        hash = NO_VERTEX ^ [self->_measTimeStart hash];
    return hash;
}

-(void) removeMac:(NSString *) mac
{
    if([self->_histogram objectForKey:mac])
        [self->_histogram removeObjectForKey:mac];
    if([self->_additionalInfo objectForKey:mac])
        [self->_additionalInfo removeObjectForKey:mac];
}

-(void) removeMacs:(NSArray *) macs
{
    if(macs == nil)
        return;
    
    for (int i = 0; i < [macs count]; i++)
        [self removeMac:[macs objectAtIndex:i]];
}

-(void) setMeasTimeEnd:(NSDate *) time
{
    self->_measTimeEnd = time;
}
-(void) setMeasTimeStart:(NSDate *) time
{
    self->_measTimeStart = time;
}
-(void) setVertex:(Vertex *) v
{
    self->_vertex = v;
}


-(void) setHistogram:(Histogram *) hist
{
    [self->_histogram setValue:[[NSDictionary alloc] init] forKey:[hist getMac]];
    [(NSDictionary *)[self->_histogram objectForKey:[hist getMac]] setValue:[NSNumber numberWithInt:[hist getValue]] forKey: [NSString stringWithFormat:@"%d",[hist getCount]]]; 
}
@end
