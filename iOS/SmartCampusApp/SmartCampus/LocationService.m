//
//  LocationService.m
//  SmartCampus
//
//  Copyright (c) 2012-2013 Aalborg University. All rights reserved.
//

#import "LocationService.h"
#import "LocationProvider.h"
#import "GpsLocationProvider.h"
#import "NoneLocationProvider.h"
#import "WifiLocationProvider.h"

NSString * const LocationServicePositioningStartedNotification = @"positioningStartedNotification";
NSString * const LocationServicePositioningStoppedNotification = @"positioningStoppedNotification";
NSString * const LocationServicePositionUpdatedNotification = @"positionUpdatedNotification";

@interface LocationService()
@property (nonatomic, strong) PositionEstimate *position;

@property (nonatomic, strong) id<LocationProvider> locationProvider;
@property (nonatomic, strong) NSDictionary *locationProviders;
@property (nonatomic, strong) NSTimer *timer;
@end

@implementation LocationService

static LocationService* _shared = nil;

+ (LocationService *)shared {
	@synchronized(self) {
		if (_shared == nil) _shared = [self new];
		return _shared;
	}
}

+ (id)alloc {
    @synchronized(self) {
        _shared = [super alloc];
        return _shared;
    }
}

- (id)init {
    @synchronized(self) {
        if (self = [super init]) {
            self.locationProviders = [NSDictionary
              dictionaryWithObjectsAndKeys:
                [GpsLocationProvider new], [[NSNumber numberWithInt:ProviderTypeGPS] stringValue],
                [NoneLocationProvider new], [[NSNumber numberWithInt:ProviderTypeNone] stringValue],
                [WifiLocationProvider new], [[NSNumber numberWithInt:ProviderTypeWiFi] stringValue],
              nil];

            [self addObserver:self forKeyPath:@"provider" options:0 context:nil];
            [self addObserver:self forKeyPath:@"online" options:0 context:nil];
        }

        return self;
    }
}

- (void)willChangeValueForKey:(NSString *)key {
    if ([key isEqualToString:@"providerType"]) {
        [self stopPositioning];
    }
}

- (void)didChangeValueForKey:(NSString *)key {
    if ([key isEqualToString:@"provider"]) {
        self.locationProvider = [self.locationProviders valueForKey:[[NSNumber numberWithInt:self.provider] stringValue]];

        [self updatePositioning];
    } else if ([key isEqualToString:@"online"]) {
        [self updatePositioning];
    }
}

- (void)updatePositioning {
    if (self.online) {
        [self startPositioning];
    } else {
        [self stopPositioning];
    }
}

- (void)startPositioning {
    if (self.timer) return;

    self.timer = [NSTimer
                  scheduledTimerWithTimeInterval:3.0
                  target:self
                  selector:@selector(updatePositionTimed:)
                  userInfo:nil
                  repeats:YES];

    [[NSNotificationCenter defaultCenter]
      postNotificationName:LocationServicePositioningStartedNotification
                    object:self];

    [self updatePosition];
}

- (void)stopPositioning {
    [self.timer invalidate];
    self.timer = nil;

    [[NSNotificationCenter defaultCenter]
      postNotificationName:LocationServicePositioningStoppedNotification
                    object:self];
}

- (void)updatePositionTimed:(NSTimer *)timer {
    [self updatePosition];
}

- (void)updatePosition {
    self.position = [self.locationProvider location];

    [[NSNotificationCenter defaultCenter]
      postNotificationName:LocationServicePositionUpdatedNotification
                    object:self];
}

@end
