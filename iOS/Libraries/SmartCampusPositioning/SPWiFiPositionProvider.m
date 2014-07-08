//
//  SPWiFiPositionProvider.m
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
// DISCLAIMED. IN NO EVENT SHALL AALBORG UNIVERSITY BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import "SPWiFiPositionProvider.h"

#import "NICInfoSummary.h"
#import "SOOData.h"

@implementation SPWiFiPositionProvider

static NSString *_macAddress;

+ (void)startWiFiPositioning {
    NSString *url = [NSString stringWithFormat:START_WIFI_POSITIONING_URL, [self macAddress]];
    [SOOData dataWithURL:url error:nil];
}

+ (void)stopWiFiPositioning {
    NSString *url = [NSString stringWithFormat:STOP_WIFI_POSITIONING_URL, [self macAddress]];
    [SOOData dataWithURL:url error:nil];
}

+ (SPPosition *)position {
    NSString *url = [NSString stringWithFormat:GET_POSITION_URL, [self macAddress]];
    NSDictionary *data = [SOOData jsonWithURL:url error:nil];
    if (data) {
        return [SPPosition positionWithJson:data];
    }

    return nil;
}

+ (SPPosition *)randomPosition:(int)buildingId {
    NSString *url = [NSString stringWithFormat:GET_RANDOM_POSITION_URL, buildingId];
    NSDictionary *data = [SOOData jsonWithURL:url error:nil];
    if (data) {
        return [SPPosition positionWithJson:data];
    }

    return nil;
}

+ (NSString *)macAddress {
    if (_macAddress == nil) {
        NICInfo *info = [[NICInfoSummary new] findNICInfo:WIFI_INTERFACE_NAME];
        if (info) {
            _macAddress = [info.macAddress copy];
        } else {
            NSLog(@"Could not find the device's MAC address!");
        }
    }

    return _macAddress;
}

@end
