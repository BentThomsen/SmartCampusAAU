//
//  SPPositionManager.m
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

#import "SPPositionManager.h"

#import "SPWiFiPositionProvider.h"

NSString * const kSPPositionUpdatedNotification = @"SPPositionUpdatedNotification";

@interface SPPositionManager()
@property (nonatomic, strong) SPPosition *position;
@property (nonatomic, strong) CLLocationManager *locationManager;
@property (nonatomic, strong) CMMotionManager *motionManager;
@property (atomic, assign) NSTimeInterval lastDeviceMotionTime;
@property (nonatomic, strong) NSTimer *timer;
@end

@implementation SPPositionManager

- (id)init {
    if (self = [super init]) {
        self.accelerationThreshold = 0.25;
        self.immobilityLockTime = 3.0;
        self.positionTimeDelta = 10.0;
        self.positionAccuracyDelta = 100.0;
        self.wiFiUpdateInterval = 3.0;
    }

    return self;
}

- (void)startUpdatingPosition {
    [self startLocationUpdates];
    [self startMotionUpdates];
    [self startWiFiUpdates];
}

- (void)startLocationUpdates {
    if (self.locationManager == nil) {
        self.locationManager = [CLLocationManager new];
        self.locationManager.delegate = self;
        self.locationManager.desiredAccuracy = kCLLocationAccuracyBest;
    }

    [self.locationManager startUpdatingLocation];
}

- (void)startMotionUpdates {
    if (self.motionManager == nil) {
        self.motionManager = [CMMotionManager new];
    }

    if ([self.motionManager isDeviceMotionAvailable]) {
        [self.motionManager startDeviceMotionUpdatesToQueue:[NSOperationQueue mainQueue]
                                                withHandler:^(CMDeviceMotion *motion, NSError *error) {
                                                    CMAcceleration userAcceleration = motion.userAcceleration;

                                                    if (fabs(userAcceleration.x) > self.accelerationThreshold
                                                        || fabs(userAcceleration.y) > self.accelerationThreshold
                                                        || fabs(userAcceleration.z) > self.accelerationThreshold) {
                                                        self.lastDeviceMotionTime = [NSDate timeIntervalSinceReferenceDate];
                                                    }
                                                }];
    }
}

- (void)startWiFiUpdates {
    [SPWiFiPositionProvider startWiFiPositioning];

    if (self.timer == nil) {
        self.timer = [NSTimer
                      scheduledTimerWithTimeInterval:self.wiFiUpdateInterval
                      target:self
                      selector:@selector(updateWiFiPositionTimed:)
                      userInfo:nil
                      repeats:YES];

        [self updateWiFiPosition];
    }
}

- (void)stopUpdatingPosition {
    [self.locationManager stopUpdatingLocation];
    [self.motionManager stopDeviceMotionUpdates];

    [self.timer invalidate];
    self.timer = nil;

    [SPWiFiPositionProvider stopWiFiPositioning];
}

- (void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations {
    [self updatePosition:[SPPosition positionWithLocation:[locations lastObject]]];
}

- (void)updateWiFiPositionTimed:(NSTimer *)timer {
    [self updateWiFiPosition];
}

- (void)updateWiFiPosition {
    [self updatePosition:[SPWiFiPositionProvider randomPosition:16]];
}

- (void)updatePosition:(SPPosition *)position {
    if ([self isBetterPositionThanCurrentPosition:position]) {
        self.position = position;

        [[NSNotificationCenter defaultCenter]
          postNotificationName:kSPPositionUpdatedNotification
          object:self];
    }
}

- (BOOL)isBetterPositionThanCurrentPosition:(SPPosition *)position {
    if (self.position == nil) {
        // A new position is always better than no position
        return YES;
    }

    // Don't update position if the user hasn't moved in 3.0 seconds
    if ([NSDate timeIntervalSinceReferenceDate] - self.lastDeviceMotionTime > self.immobilityLockTime) {
        return NO;
    }

    // Check relative time of position
    NSTimeInterval timeDelta = position.timestamp.timeIntervalSinceReferenceDate - self.position.timestamp.timeIntervalSinceReferenceDate;
    BOOL isMuchNewer = timeDelta > self.positionTimeDelta;
    BOOL isMuchOlder = timeDelta < -self.positionTimeDelta;
    BOOL isNewer = timeDelta > 0;

    if (isMuchNewer) {
        // Use new position as it is much newer than the current position
        return YES;
    } else if (isMuchOlder) {
        // Don't use the new position as it is much older than the current position
        return NO;
    }

    // Check relative accuracy of position
    double accuracyDelta = position.accuracy - self.position.accuracy;
    BOOL isLessAccurate = accuracyDelta > 0;
    BOOL isMoreAccurate = accuracyDelta < 0;
    BOOL isMuchLessAccurate = accuracyDelta > self.positionAccuracyDelta;

    // Check if same provider
    BOOL isFromSameProvider = position.provider == self.position.provider;

    // Determine position quality
    if (isMoreAccurate) {
        return YES;
    } else if (isNewer && !isLessAccurate) {
        return YES;
    } else if (isNewer && !isMuchLessAccurate && isFromSameProvider) {
        return YES;
    }

    return NO;
}

@end
