//
//  AggregateLocation.m
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

#import "AggregateLocation.h"

@implementation AggregateLocation

- (id)initWithAbsolute:(AbsoluteLocation *)absolute {
    return [self initWithAbsolute:absolute symbolic:nil pixel:nil relative:nil];
}

- (id)initWithAbsolute:(AbsoluteLocation *)absolute
              symbolic:(SymbolicLocation *)symbolic {
    return [self initWithAbsolute:absolute symbolic:symbolic pixel:nil relative:nil];
}

- (id)initWithAbsolute:(AbsoluteLocation *)absolute
              symbolic:(SymbolicLocation *)symbolic
                 pixel:(PixelLocation *)pixel {
    return [self initWithAbsolute:absolute symbolic:symbolic pixel:pixel relative:nil];
}

- (id)initWithAbsolute:(AbsoluteLocation *)absolute
              symbolic:(SymbolicLocation *)symbolic
                 pixel:(PixelLocation *)pixel
              relative:(RelativeLocation *)relative {
    if (self = [super init]) {
        self.absolute = absolute;
        self.symbolic = symbolic;
        self.pixel = pixel;
        self.relative = relative;
    }

    return self;
}

- (BOOL)isEqual:(id)object {
    if(object == self) return YES;
    if(!object || ![object isKindOfClass:[self class]]) return NO;

    AggregateLocation *other = (AggregateLocation*) object;
    return [self.absolute isEqual:other.absolute];
}

- (int)hashcode {
    return [self.absolute hashCode];
}

#pragma mark NSCopying protocol
- (id)copyWithZone:(NSZone*)zone {
    AggregateLocation *copy = [[[self class] alloc] init];

    copy.absolute = self.absolute;
    copy.symbolic = self.symbolic;
    copy.pixel = self.pixel;
    copy.relative = self.relative;

    return copy;
}

@end
