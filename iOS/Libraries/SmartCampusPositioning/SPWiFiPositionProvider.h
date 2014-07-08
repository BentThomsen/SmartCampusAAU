//
//  SPWiFiPositionProvider.h
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

#import "SOOData.h"
#import "SPPosition.h"

#define WIFI_INTERFACE_NAME @"en0"

#define WIFI_BASE_URL @"http://smartcampus.cs.aau.dk/"

#define WIFI_POSITIONING_PATH @"WifiSnifferPositioningService/SnifferService.svc/"
#define WIFI_POSITIONING_URL WIFI_BASE_URL WIFI_POSITIONING_PATH

#define START_WIFI_POSITINING_PATH @"StartWifiPositioning?clientMac='%@'"
#define START_WIFI_POSITIONING_URL WIFI_POSITIONING_URL START_WIFI_POSITINING_PATH

#define STOP_WIFI_POSITINING_PATH @"StopWifiPositioning?clientMac='%@'"
#define STOP_WIFI_POSITIONING_URL WIFI_POSITIONING_URL STOP_WIFI_POSITINING_PATH

#define GET_POSITION_PATH @"GetPosition?clientMac='%@'"
#define GET_POSITION_URL WIFI_POSITIONING_URL GET_POSITION_PATH

#define GET_RANDOM_POSITION_PATH @"TestGetRandomPosition?buildingId=%d"
#define GET_RANDOM_POSITION_URL WIFI_POSITIONING_URL GET_RANDOM_POSITION_PATH

@interface SPWiFiPositionProvider : NSObject
+ (void)startWiFiPositioning;
+ (void)stopWiFiPositioning;

+ (SPPosition *)position;
+ (SPPosition *)randomPosition:(int)buildingId;
@end
