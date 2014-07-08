//
//  SPPositionManager.h
//  SmartCampus
//
//  Copyright (c) 2013 Aalborg University. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import <CoreMotion/CoreMotion.h>

#import "SPPosition.h"

extern NSString * const kSPPositionUpdatedNotification;

@interface SPPositionManager : NSObject<CLLocationManagerDelegate>

@property (nonatomic, strong, readonly) SPPosition *position;
@property (nonatomic, assign) double accelerationThreshold;
@property (nonatomic, assign) double immobilityLockTime;
@property (nonatomic, assign) double positionTimeDelta;
@property (nonatomic, assign) double positionAccuracyDelta;
@property (nonatomic, assign) double wiFiUpdateInterval;

- (void)startUpdatingPosition;
- (void)stopUpdatingPosition;

@end
