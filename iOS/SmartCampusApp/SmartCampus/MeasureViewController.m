//
//  MeasureViewController.m
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

#import "MeasureViewController.h"
#import "MapWebViewController.h"

#import "SmartCampusPositioning.h"

@interface MeasureViewController ()

@property (weak, nonatomic) IBOutlet UIButton *measuringButton;
@property (weak, nonatomic) IBOutlet UIButton *saveButton;

@property (weak, nonatomic) MapWebView *mapWebView;
@property (assign, nonatomic) BOOL measuring;

@end

@implementation MeasureViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    UIBarButtonItem *closeButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel target:self action:@selector(closeButtonTapped:)];
    self.navigationItem.leftBarButtonItem = closeButton;

    self.title = NSLocalizedString(@"Wi-Fi Measure", nil);
}

- (void)viewDidUnload {
    [self setMeasuringButton:nil];
    [self setSaveButton:nil];

    [super viewDidUnload];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    self.mapWebView = ((MapWebViewController *) sender).mapWebView;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];

    [self stopMeasuring];
}

- (void)viewDidDisappear:(BOOL)animated {
    [self stopMeasuring];

    [super viewDidDisappear:animated];
}

- (IBAction)measuringTapped:(id)sender {
    if (self.measuring) {
        [self stopMeasuring];
    } else {
        [self startMeasuring];
    }
}

- (IBAction)saveTapped:(id)sender {
    [self dismissModalViewControllerAnimated:YES];
}

- (void)closeButtonTapped:(id)sender  {
    [self dismissModalViewControllerAnimated:YES];
}

- (void)startMeasuring {
    self.measuring = YES;
    [self.measuringButton setTitle:@"Stop measuring" forState:UIControlStateNormal];
    self.saveButton.enabled = NO;
    [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;

    [SPMeasurementManager startMeasuringAt:self.mapWebView.selectedVertex];
}

- (void)stopMeasuring {
    [SPMeasurementManager stopMeasuring];

    self.measuring = NO;
    [self.measuringButton setTitle:@"Start measuring" forState:UIControlStateNormal];
    self.saveButton.enabled = YES;
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
}

@end
