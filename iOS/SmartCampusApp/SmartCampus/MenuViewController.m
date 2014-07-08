//
//  MenuViewController.m
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

#import "MenuViewController.h"
#import "TablePickerController.h"
#import "MapWebViewController.h"

@implementation MenuViewController {
    CGFloat currentY;
    bool hasFloors;
    NSInteger floorCurrent;
    NSInteger floorMin;
    NSInteger floorMax;
    CGFloat contentHeight;
    CGFloat margin;
    CGFloat padding;
}

- (void)awakeFromNib {
    self.positionTop = YES;
    
    margin = 20;
    padding = 7;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if ([segue.identifier isEqualToString:@"nearby"]) {
        if ([sender isKindOfClass:[MapWebViewController class]]) {
            MapWebViewController *mapController = (MapWebViewController*)sender;
            mapController.nearbyPickerController = (TablePickerController*)segue.destinationViewController;
        }
    }
}

- (void)viewDidLoad
{
    _settings = [Settings sharedSettings];
    
    self.view.backgroundColor = [UIColor colorWithPatternImage: [UIImage imageNamed:@"SettingsBackground.png"]];
    
    [self load];

    [self.view layoutIfNeeded];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];

    if (hasFloors) {
        self.floorStepper.Current = floorCurrent;
        self.floorStepper.Minimum = floorMin;
        self.floorStepper.Maximum = floorMax;
    } else {
        self.floorStepper.enabled = NO;
        self.floorStepper.textField.placeholder = NSLocalizedString(@"No floors", nil);
    }
    
    
    [self.view layoutIfNeeded];
}

- (void)viewDidUnload {
    [self setModeControl:nil];
    [super viewDidUnload];
}

- (void)updateFromBuilding:(Building *)building {
    hasFloors = NO;
    floorCurrent = 0;
    floorMin = 0;
    floorMax = 0;

    if ([building hasFloors]) {
        hasFloors = YES;
        floorCurrent = [Building currentFloor];
        floorMin = [building minimumFloorNumber];
        floorMax = [building maximumFloorNumber];
    }
}

- (void) updateCurrentY:(CGRect)frame {
    currentY = frame.origin.y + frame.size.height+7;
}

- (void) load {    
    [self setupElements];
    
    self.floorStepper.delegate = self;
    self.floorStepper.textField.textAlignment = UITextAlignmentCenter;
    self.floorStepper.textField.textColor = [UIColor grayColor];

    self.modeControl.selectedSegmentIndex = [[NSNumber numberWithBool:self.settings.online] intValue];
    self.providerControl.selectedSegmentIndex = self.settings.provider;
}

- (void) reload {
    [self load];
}

- (void) setupElements {    
    self.contentView = [[UIView alloc] initWithFrame:CGRectInset(self.view.frame, margin, margin)];
    
    if (self.positionTop) {
        self.contentView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleBottomMargin;
    } else {
        self.contentView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin;
    }
    
    [self.view addSubview:self.contentView];
    
    if (self.settings.online) {
        NSArray *providerItems = [NSArray arrayWithObjects: NSLocalizedString(@"GPS", nil), NSLocalizedString(@"Wi-Fi", nil), NSLocalizedString(@"None", nil), nil];
        self.providerControl = [[UISegmentedControl alloc] initWithItems:providerItems];
        self.providerControl.frame = CGRectMake(0, currentY, self.contentView.frame.size.width, 35);
        self.providerControl.autoresizingMask = UIViewAutoresizingFlexibleWidth;
        self.providerControl.segmentedControlStyle = UISegmentedControlStylePlain;
        [self.providerControl addTarget:self action:@selector(providerChanged:) forControlEvents:UIControlEventValueChanged];
        [self.contentView addSubview:self.providerControl];
        [self updateCurrentY:self.providerControl.frame];
    }

    CGRect floorStepperFrame = CGRectMake(0, currentY, self.contentView.frame.size.width, 35);
    self.floorStepper = [[UITextStepperField alloc] initWithFrame:floorStepperFrame];
    self.floorStepper.autoresizingMask = UIViewAutoresizingFlexibleWidth;
    [self.contentView addSubview:self.floorStepper];
    [self updateCurrentY:self.floorStepper.frame];
    
    self.nearbyButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    self.nearbyButton.autoresizingMask = UIViewAutoresizingFlexibleWidth;
    [self.nearbyButton setTitle:NSLocalizedString(@"What's nearby?", nil) forState:UIControlStateNormal];
    [self.nearbyButton addTarget:self action:@selector(nearbyTapped:) forControlEvents:UIControlEventTouchUpInside];
    [self.contentView addSubview:self.nearbyButton];
    
    CGFloat columnWidth = (self.contentView.frame.size.width / 2)-(padding/2);
    
    if (self.settings.online) {
        self.clearRouteButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
        self.clearRouteButton.frame = CGRectMake(0, currentY, columnWidth, 35);
        self.clearRouteButton.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleRightMargin;
        [self.clearRouteButton setTitle:NSLocalizedString(@"Clear route", nil) forState:UIControlStateNormal];
        [self.clearRouteButton addTarget:self action:@selector(clearRouteTapped:) forControlEvents:UIControlEventTouchUpInside];
        [self.contentView addSubview:self.clearRouteButton];  
        
        self.nearbyButton.frame = CGRectMake(columnWidth + padding, currentY, columnWidth, 35);
        self.nearbyButton.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleLeftMargin;
        [self updateCurrentY:self.nearbyButton.frame];
    } else {
        self.measureButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
        self.measureButton.frame = CGRectMake(0, currentY, columnWidth, 35);
        self.measureButton.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleRightMargin;
        [self.measureButton setTitle:NSLocalizedString(@"Measure", nil) forState:UIControlStateNormal];
        [self.measureButton addTarget:self action:@selector(measureTapped:) forControlEvents:UIControlEventTouchUpInside];
        [self.contentView addSubview:self.measureButton];  
        
        self.editGraphButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
        self.editGraphButton.frame = CGRectMake(columnWidth + padding, currentY, columnWidth, 35);
        self.editGraphButton.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleLeftMargin;
        [self.editGraphButton setTitle:NSLocalizedString(@"Edit graph", nil) forState:UIControlStateNormal];
        [self.editGraphButton addTarget:self action:@selector(editGraphTapped:) forControlEvents:UIControlEventTouchUpInside];
        [self.contentView addSubview:self.editGraphButton];          
        
        [self updateCurrentY:self.editGraphButton.frame];
        
        self.nearbyButton.frame = CGRectMake(0, currentY, self.contentView.frame.size.width, 35);
        [self updateCurrentY:self.nearbyButton.frame];
    }
    
    NSArray *modeItems = [NSArray arrayWithObjects: NSLocalizedString(@"Offline mode", nil), NSLocalizedString(@"Online mode", nil), nil];
    self.modeControl = [[UISegmentedControl alloc] initWithItems:modeItems];
    self.modeControl.frame = CGRectMake(0, currentY, self.contentView.frame.size.width, 35);
    self.modeControl.autoresizingMask = UIViewAutoresizingFlexibleWidth;
    self.modeControl.segmentedControlStyle = UISegmentedControlStylePlain;
    [self.modeControl addTarget:self action:@selector(onlineChanged:) forControlEvents:UIControlEventValueChanged];
    [self.contentView addSubview:self.modeControl];
    [self updateCurrentY:self.modeControl.frame];
    
    contentHeight = self.modeControl.frame.origin.y + self.modeControl.frame.size.height;
    CGRect contentViewFrame = self.contentView.frame;

    if (self.positionTop) {
        contentViewFrame.origin.y = margin;
        contentViewFrame.origin.x = margin;
        contentViewFrame.size.height = contentHeight;
    } else {
        contentViewFrame.origin.y += contentViewFrame.size.height-contentHeight-20;
        contentViewFrame.size.height = contentHeight;
    }
    
    self.contentView.frame = contentViewFrame;
}


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return YES;
}

- (CGSize)contentSizeForViewInPopover {
    return CGSizeMake(320, contentHeight+margin*2);
}

- (void) onlineChanged:(UISegmentedControl *)sender {
    BOOL online = [[NSNumber numberWithInt:sender.selectedSegmentIndex] boolValue];
    self.settings.online = online;
    [self.delegate menuViewController:self didChangeOnline:online];
}

- (void) providerChanged:(UISegmentedControl *)sender {
    self.settings.provider = sender.selectedSegmentIndex;
    [self.delegate menuViewController:self didChangeProvider:sender.selectedSegmentIndex];
}

- (void) clearRouteTapped:(UIButton *)sender {
    [self.delegate menuViewControllerClearRoute:self];
}

- (void) measureTapped:(UIButton *)sender {
    [self.delegate menuViewControllerMeasure:self];}

- (void) editGraphTapped:(UIButton *)sender {
    [self.delegate menuViewControllerEditGraph:self];
}

- (void) nearbyTapped:(UIButton *)sender {
    [self.delegate menuViewControllerShowNearby:self];
}

#pragma mark - UITextStepperFieldDelegate
- (NSString *)textStepperField:(UITextStepperField *)textStepperField textForValue:(float)value {
    return [NSString stringWithFormat:@"%@ %d %@ %d", NSLocalizedString(@"Floor", nil), (NSInteger)value, NSLocalizedString(@"of", nil), floorMax];
}

- (void)textStepperField:(UITextStepperField *)textStepperField valueDidChange:(float)value {
    [self.delegate menuViewController:self didChangeFloor:value];
}

@end
