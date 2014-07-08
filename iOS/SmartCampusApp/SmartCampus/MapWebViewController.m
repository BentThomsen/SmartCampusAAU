//
//  MapWebViewController.m
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

#import "MapWebViewController.h"

#import "SBJson.h"
#import "NSObject+SBJson.h"

#import "MeasureViewController.h"
#import "MenuViewController.h"

#import "MapOnline.h"
#import "MapOffline.h"

#import "OData.h"
#import "PositionEstimate.h"
#import "Settings.h"

#import "LocationService.h"
#import "WifiLocationProvider.h"
#import "NoneLocationProvider.h"

@interface MapWebViewController()
@property (nonatomic, strong) MenuViewController *menuViewController;
@property (nonatomic, strong) UIPopoverController *menuPopover;
@property (nonatomic, strong) PositionEstimate *position;
@property (nonatomic, assign) int floor;
@property (nonatomic, assign) BOOL online;
@property (nonatomic, assign) BOOL tracking;
@end

@implementation MapWebViewController {
    SBJsonParser *jsonParser;
    UIGestureRecognizer* cancelGesture;
    MapOnline *mapOnline;
    MapOffline *mapOffline;
    id positionUpdatedObserver;
}

- (IBAction)settingsButtonTapped:(id)sender {
    if (self.menuPopover==nil) {
        [self performSegueWithIdentifier:@"menuPopover" sender:sender];
    }
}

- (void) backgroundTouched:(id)sender {
    [self.view endEditing:YES];
}

- (void)changeProvider:(ProviderType)provider {
    [LocationService shared].provider = provider;
}

- (void)changeTracking:(BOOL)tracking {
    self.tracking = tracking;
}

- (void)changeOnline:(BOOL)online {
    self.online = online;
    [LocationService shared].online = online;
    self.map = online ? mapOnline : mapOffline;
    [self.map setMapReady];
}

#pragma mark - UIView
- (void)viewDidLoad {
    jsonParser = [SBJsonParser new];
    mapOnline = [[MapOnline alloc] initWithMapWebView:self.mapWebView];
    mapOffline = [[MapOffline alloc] initWithMapWebView:self.mapWebView];

    self.searchBar.delegate = self;
    self.mapWebView.delegate = self;
    self.position = [PositionEstimate new];

    Settings *settings = [Settings sharedSettings];
    [self changeProvider:settings.provider];
    [self changeOnline:settings.online];
    [self changeTracking:settings.tracking];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];

    positionUpdatedObserver = [[NSNotificationCenter defaultCenter]
      addObserverForName:LocationServicePositionUpdatedNotification
                  object:nil
                   queue:nil
              usingBlock:^(NSNotification *notification) {
                  [self updatePosition:((LocationService *) notification.object).position];
              }];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];

    [[NSNotificationCenter defaultCenter] removeObserver:positionUpdatedObserver];
}

- (void)viewDidUnload {
    [self setMapWebView:nil];
    [self setSearchBar:nil];
    [super viewDidUnload];
}

- (void)setNearbyPickerController:(TablePickerController *)nearbyPickerController {
    if (nearbyPickerController!=_nearbyPickerController) {
        _nearbyPickerController = nearbyPickerController;
        self.nearbyPickerController.delegate = self;
        [self.map setupNearbyController:self.nearbyPickerController];
    }
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if ([segue.identifier isEqualToString:@"menu"]) {
        _menuViewController = segue.destinationViewController;
        self.menuViewController.positionTop = NO;
    }

    if ([segue.identifier isEqualToString:@"menuPopover"]) {
        _menuViewController = (MenuViewController*)[segue.destinationViewController topViewController];
        self.menuViewController.view.backgroundColor = [UIColor colorWithRed:244.0/255.0 green:244.0/255.0 blue:244.0/255.0 alpha:1.0];
        self.menuPopover = [(UIStoryboardPopoverSegue *)segue popoverController];
        self.menuPopover.delegate = self;
    }

    if (self.menuViewController) {
        self.menuViewController.delegate = self;
        if ([Building hasActiveBuilding])
            [self.menuViewController updateFromBuilding:[Building activeBuilding]];
    }

    if ([segue.identifier isEqualToString:@"nearby"]) {
        self.nearbyPickerController = (TablePickerController*)[segue.destinationViewController topViewController];        
    }
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation {
    return YES;
}

- (void) clearOverlay {
    [self.mapWebView clearOverlays]; 
}

- (void) addEdgeOverlay:(NSInteger)floor {
}

- (void) addGraphOverlay:(NSInteger)floor {
    NSArray *vertices = [self.map getVisibleVertices:[[Building activeBuilding].graph verticesForFloor:floor]];
    [self.mapWebView showVertices:vertices onFloor:floor isOnline:[self.map isOnline]];
}

#pragma mark - MenuViewControllerDelegate
- (void)menuViewController:(MenuViewController *)controller didChangeProvider:(ProviderType)provider {
    [self changeProvider:provider];
}

- (void)menuViewController:(MenuViewController *)controller didChangeOnline:(BOOL)online {
    [self changeOnline:online];
}

- (void)menuViewController:(MenuViewController *)controller didChangeFloor:(int)floor {
    self.floor = floor;
    [self.mapWebView clearOverlays];
    [self addEdgeOverlay:floor];
    [self addGraphOverlay:floor];
}

- (void)menuViewControllerClearRoute:(MenuViewController *)controller {
    [self dismissMenuViewController];
    [self clearOverlay];
}

- (void)menuViewControllerMeasure:(MenuViewController *)controller {
    if (self.menuPopover) {
        [controller performSegueWithIdentifier:@"measure" sender:self];
    } else {
        [self.menuViewController dismissViewControllerAnimated:NO completion:^{
            [self performSegueWithIdentifier:@"measure" sender:self];
        }];
    }
}

- (void)menuViewControllerEditGraph:(MenuViewController *)controller {
    [self dismissMenuViewControllerOrPopover];
}

- (void)menuViewControllerShowNearby:(MenuViewController *)controller {
    if (self.menuPopover) {
        [controller performSegueWithIdentifier:@"nearby" sender:self];
    } else {
        [self.menuViewController dismissViewControllerAnimated:NO completion:^{
            [self performSegueWithIdentifier:@"nearby" sender:nil];
        }];
    }
}

- (void) dismissMenuViewControllerOrPopover {
    if (self.menuPopover) {
        [self.menuPopover dismissPopoverAnimated:YES];
    } else {
        [self.menuViewController dismissModalViewControllerAnimated:YES];
    }

    self.menuPopover = nil;
    self.menuViewController = nil;
}

- (void) dismissMenuViewController {
    [self.menuViewController dismissModalViewControllerAnimated:YES];
    self.menuViewController = nil;
}

#pragma mark - UIPopoverControllerDelegate
- (void)popoverControllerDidDismissPopover:(UIPopoverController *)popoverController {
    self.menuPopover = nil;
}

#pragma mark - UISearchBarDelegate
- (void)searchBarSearchButtonClicked:(UISearchBar *)sender {
    if (![sender.text isEqualToString:@""])
        [self.mapWebView search:sender.text];

    [sender resignFirstResponder];
}

- (void)searchBarCancelButtonClicked:(UISearchBar *)sender {
    [sender resignFirstResponder];
}

- (UIView *)overlayView {
    if (_overlayView==nil) {
        _overlayView = [[UIView alloc] initWithFrame:self.mapWebView.frame];
        self.overlayView.backgroundColor = [UIColor blackColor];
        self.overlayView.alpha = 0.6;
        self.overlayView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    }

    return _overlayView;
}

-(void)searchBarTextDidBeginEditing:(UISearchBar *)searchBar {
    [self.view insertSubview:self.overlayView aboveSubview:self.mapWebView];

    cancelGesture = [UITapGestureRecognizer new];
    [cancelGesture addTarget:self action:@selector(backgroundTouched:)];
    [self.overlayView addGestureRecognizer:cancelGesture];
}

-(void)searchBarTextDidEndEditing:(UISearchBar *)searchBar {
    [self.overlayView removeFromSuperview];
    _overlayView = nil;

    if (cancelGesture) {
        [self.overlayView removeGestureRecognizer:cancelGesture];
        cancelGesture = nil;
    }
}

#pragma mark - UIWebViewDelegate
- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request
 navigationType:(UIWebViewNavigationType)navigationType {
    NSString *requestString = [[request URL] absoluteString];

    if ([requestString hasPrefix:@"js-frame:"]) {
        NSArray *components = [requestString componentsSeparatedByString:@":"];

        NSString *function = (NSString*)[components objectAtIndex:1];
		int callbackId = [((NSString*)[components objectAtIndex:2]) intValue];
        NSString *argsAsString = [(NSString*)[components objectAtIndex:3] 
                                  stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        NSArray *args = (NSArray*)[jsonParser objectWithString:argsAsString error:nil];

        NSLog(@"%@, %i, %@", function, callbackId, args);
        [self handleCall:function callbackId:callbackId args:args];

        return NO;
    }

    return YES;
}

- (void)returnResult:(int)callbackId args:(id)arg, ...;
{
    if (callbackId==0) return;

    va_list argsList;
    NSMutableArray *resultArray = [[NSMutableArray alloc] init];

    if(arg != nil){
        [resultArray addObject:arg];
        va_start(argsList, arg);
        while((arg = va_arg(argsList, id)) != nil)
            [resultArray addObject:arg];
        va_end(argsList);
    }

    // TODO: Test if this is working as expected
    NSString *resultArrayString = [resultArray JSONRepresentation];

    [self performSelector:@selector(returnResultAfterDelay:) withObject:[NSString stringWithFormat:@"NativeBridge.resultForCallback(%d,%@);",callbackId,resultArrayString] afterDelay:0];
}

- (void)handleCall:(NSString*)functionName callbackId:(int)callbackId args:(NSArray*)args {
    if ([functionName isEqualToString:@"debugAlert"]) {
        if ([args count]!=1)
            return;

        NSString *msg = (NSString*)[args objectAtIndex:0];

        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Message", nil)
                                                        message:msg 
                                                       delegate:nil 
                                              cancelButtonTitle:NSLocalizedString(@"OK", nil)
                                              otherButtonTitles:nil];

        [alert show];
    } else if ([functionName isEqualToString:@"onTap"]) {
        if ([args count]!=3)
            return;

        //BOOL isOnline = (BOOL)[args objectAtIndex:0];
        NSInteger floorNum = (NSInteger)[args objectAtIndex:1];
        NSInteger vertexId = (NSInteger)[args objectAtIndex:2];
        
        [self.map onTap:floorNum vertexId:vertexId];
    } else if ([functionName isEqualToString:@"setSelectedLocation"]) {
        if ([args count]!=4)
            return;

        BOOL isOnline = (BOOL)[args objectAtIndex:0];
        NSInteger floorNum = (NSInteger)[args objectAtIndex:1];
        NSNumber *lat = (NSNumber*)[args objectAtIndex:2];
        NSNumber *lng = (NSNumber*)[args objectAtIndex:3];

        [self.map setSelectedLocation:isOnline floor:floorNum latitude:lat longitude:lng];
    } else if ([functionName isEqualToString:@"removeLink"]) {
        if ([args count]!=2)
            return;

        //NSInteger originId = (NSInteger)[args objectAtIndex:0];
        //NSInteger destinationId = (NSInteger)[args objectAtIndex:1];
    } else if ([functionName isEqualToString:@"setMapReady"]) {
        [self.map setMapReady];
    } else {
        NSLog(@"Unimplemented method '%@'",functionName);
        return;
    }

    [self returnResult:callbackId args:nil];
}

#pragma mark TablePickerViewControllerDelegate
- (void)tablePickerViewController:(TablePickerController *)controller didSelectItem:(id<TablePickerItem>)item {
    if (controller==self.nearbyPickerController) {
        if ([item isKindOfClass:[Vertex class]]) {
            Vertex *vertex = (Vertex*)item;
            AbsoluteLocation *absLoc = vertex.location.absolute;
            [self.mapWebView setCenter:absLoc.toLocationCoordinate];
            [self dismissMenuViewControllerOrPopover];
        }
    }
}

#pragma mark Positioning
- (void)updatePosition:(PositionEstimate *)position {
    self.position = position;

    if (self.position.buildingId != [Building activeBuilding].id) {
        [self loadBuilding:self.position.buildingId];

        NSURL *url = [NSURL URLWithString:[Building activeBuilding].ifcUrl];
        if (self.position.buildingId == 16) {
            url = [NSURL URLWithString:@"file://localhost/Users/fsa/Downloads/sl300/aalborg.html"];
        }

        [self.mapWebView loadURL:url];
    }

    //if (self.position.floor != self.floor) {
        [self updateFloor:self.position.floor];
    //}

    [self.mapWebView updatePosition:self.position];
}

- (void)updateFloor:(int)floor {
    self.floor = floor;
    [Building setCurrentFloor:floor];

    NSArray *vertices = [self.map getVisibleVertices:[[Building activeBuilding].graph verticesForFloor:self.floor]];

    [self.mapWebView clearOverlays];
    [self.mapWebView showVertices:vertices onFloor:self.floor isOnline:self.online];
}

- (void)loadBuilding:(int)buildingId {
    NSDictionary *data = [OData jsonWithURL:[NSString stringWithFormat:RADIO_MAP_BUILDING_URL, buildingId]];
    if (data) {
        [Building setActiveBuilding:[Building buildingWithJson:data]];
    }
}

@end
