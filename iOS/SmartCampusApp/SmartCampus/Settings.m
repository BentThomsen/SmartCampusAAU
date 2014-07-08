//
//  Settings.m
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

#import "Settings.h"

@interface Settings()
@property (readonly, nonatomic, strong) NSUserDefaults *defaults;
@end

@implementation Settings

@synthesize defaults = _defaults;

static Settings* _sharedSettings = nil;

+ (Settings*)sharedSettings {
	@synchronized([Settings class]) {
		if (_sharedSettings == nil)
			_sharedSettings = [self new];

		return _sharedSettings;
	}

	return nil;
}

+ (id)alloc {
    @synchronized(self) {
        _sharedSettings = [super alloc];
        return _sharedSettings;
    }

    return nil;
}

- (NSUserDefaults *)defaults {
    if (_defaults == nil) {
        _defaults = [NSUserDefaults standardUserDefaults];
    }

    return _defaults;
}

- (ProviderType)provider {
    return [self.defaults integerForKey:DEFAULTS_PROVIDER];
}

- (void)setProvider:(ProviderType)provider {
    if (provider != self.provider) {
        [self.defaults setInteger:provider forKey:DEFAULTS_PROVIDER];
        [self sync];
    }
}

- (BOOL)online {
    return [self.defaults boolForKey:DEFAULTS_ONLINE];
}

- (void)setOnline:(BOOL)online {
    if (online != self.online) {
        [self.defaults setBool:online forKey:DEFAULTS_ONLINE];
        [self sync];
    }
}

- (BOOL)tracking {
    return [self.defaults boolForKey:DEFAULTS_TRACKING];
}

- (void)settracking:(BOOL)tracking {
    if (tracking != self.tracking) {
        [self.defaults setBool:tracking forKey:DEFAULTS_TRACKING];
        [self sync];
    }
}

- (void)sync {
    [self.defaults synchronize];
    [self.delegate settingsDidChange:self];
}

@end
