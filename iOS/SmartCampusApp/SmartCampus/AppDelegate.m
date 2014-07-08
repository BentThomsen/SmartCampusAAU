//
//  AppDelegate.m
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

#import "AppDelegate.h"

#import "SBJson.h"
#import "NSObject+SBJson.h"

#import "LocationService.h"
#import "WifiLocationProvider.h"
#import "WifiPositioning.h"

#import "Building.h"

@implementation AppDelegate {
    id positionUpdatedObserver;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{       
    // Override point for customization after application launch.
    Building *building = [Building new];
    building.name = @"FV28";
    building.address = @"Februarvej 28";
    building.postalCode = @"8210";
    building.country = @"Denmark";
    building.latitude = 56.177501;
    building.longitude = 10.162160;
    building.url = @"http://www.filipadamsen.dk/";
    building.ifcUrl = @"file://localhost/Users/fsa/Downloads/sl300/aalborg.html";

    NSDictionary *data = [building toJSON];
    NSLog(@"%@", [data JSONRepresentation]);

    /*
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:@"http://smartcampusaau.cs.aau.dk/RadioMapService3/RadioMapService.svc/Buildings"]];
    [request setHTTPMethod:@"POST"];
    [request setHTTPBody:[[data JSONRepresentation] dataUsingEncoding:NSUTF8StringEncoding]];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];

    NSError *error;
    NSData *response = [NSURLConnection sendSynchronousRequest:request returningResponse:nil error:&error];
    if (response) {
        NSLog(@"%@", [[NSString alloc] initWithData:response encoding:NSUTF8StringEncoding]);
    } else {
        NSLog(@"%@", error);
    }
     */

    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    [LocationService shared].online = NO;

    [self.positionManager stopUpdatingPosition];

    [[NSNotificationCenter defaultCenter] removeObserver:positionUpdatedObserver];
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    [LocationService shared].provider = [Settings sharedSettings].provider;
    [LocationService shared].online = [Settings sharedSettings].online;

    if (self.positionManager == nil) {
        self.positionManager = [SPPositionManager new];
        self.positionManager.accelerationThreshold = 0.25;
        self.positionManager.immobilityLockTime = 3.0;
        self.positionManager.positionTimeDelta = 10.0;
        self.positionManager.positionAccuracyDelta = 100.0;
        self.positionManager.wiFiUpdateInterval = 3.0;
    }

    positionUpdatedObserver = [[NSNotificationCenter defaultCenter]
                               addObserverForName:kSPPositionUpdatedNotification
                               object:nil
                               queue:[NSOperationQueue mainQueue]
                               usingBlock:^(NSNotification *notification) {
                                   NSLog(@"%@", ((SPPositionManager *) notification.object).position);
                               }];

    [self.positionManager startUpdatingPosition];
}

@end
