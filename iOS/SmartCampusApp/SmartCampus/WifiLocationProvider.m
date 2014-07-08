//
//  WifiLocationProvider.m
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

#import "WifiLocationProvider.h"

#import "LocationService.h"
#import "WifiPositioning.h"

@interface WifiLocationProvider()
@property (nonatomic, strong) NSTimer *timer;
@end

@implementation WifiLocationProvider {
    id positioningStartedObserver;
    id positioningStoppedObserver;
}

- (id)init {
    if (self = [super init]) {
        positioningStartedObserver = [[NSNotificationCenter defaultCenter]
          addObserverForName:LocationServicePositioningStartedNotification
                      object:nil
                       queue:nil
                  usingBlock:^(NSNotification *notification) {
                      if (((LocationService *) notification.object).provider == ProviderTypeWiFi)
                          [WifiPositioning startWiFiPositioning];
                  }];

        positioningStoppedObserver = [[NSNotificationCenter defaultCenter]
          addObserverForName:LocationServicePositioningStoppedNotification
                      object:nil
                       queue:nil
                  usingBlock:^(NSNotification *notification) {
                      if (((LocationService *) notification.object).provider == ProviderTypeWiFi)
                          [WifiPositioning stopWiFiPositioning];
                  }];
    }

    return self;
}

- (PositionEstimate *)location {
    return [WifiPositioning updatePosition];
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:positioningStartedObserver];
    [[NSNotificationCenter defaultCenter] removeObserver:positioningStoppedObserver];
}

@end
