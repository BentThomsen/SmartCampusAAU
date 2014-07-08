//
//  SymbolicLocation.m
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

#import "SymbolicLocation.h"

@implementation SymbolicLocation

+ (SymbolicLocation *)locationWithJson:(NSDictionary *)data {
    SymbolicLocation *location = [SymbolicLocation new];

    location.id = [[data valueForKey:@"ID"] intValue];
    location.vertexId = [[data valueForKey:@"Vertex_ID"] intValue];
    location.type = [[data valueForKey:@"info_type"] intValue];

    if ([data valueForKey:@"title"] != nil) {
        location.title = [data valueForKey:@"title"];
    } else {
        location.title = @"null";
    }

    location.description = [data valueForKey:@"description"];
    location.url = [data valueForKey:@"url"];
    location.entrance = [[data valueForKey:@"is_entrance"] boolValue];

    return location;
}

- (NSDictionary *)toJSON {
    NSDictionary *data = [[NSDictionary alloc] initWithObjectsAndKeys:
                          [[NSNumber numberWithInt:self.id] stringValue], @"ID",
                          [[NSNumber numberWithInt:self.vertexId] stringValue], @"Vertex_ID",
                          [[NSNumber numberWithInt:self.type] stringValue], @"info_type",
                          self.title, @"title",
                          self.description, @"description",
                          self.url, @"url",
                          self.entrance, @"is_entrance",
                          nil];

    return data;
}

- (id)initWithTitle:(NSString *)title description:(NSString *)description url:(NSString *)url {
    return [self initWithId:0 title:title description:description url:url];
}

- (id)initWithId:(int)id title:(NSString *)title description:(NSString *)description url:(NSString *)url {
    if (self = [super init]) {
        self.id = id;
        self.title = title;
        self.description = description;
        self.url = url;

        return self;
    } else {
        return nil;
    }
}

/* We should really use L10N instead */
- (NSString *)prettyPrint:(InfoType)type {
    switch (type) {
        case DEFIBRILLATOR:
            return @"Defibrillator";
        case FIRE_EXTINGUISHER:
            return @"Fire Extinguisher";
        case OFFICE:
            return @"Office";
        case FIRST_AID_KIT:
            return @"First Aid Kit";
        case TOILET:
            return @"Toilet";
        case FOOD:
            return @"Food";
        case LECTURE_ROOM:
            return @"Lecture Room";
        default:
            return @"NONE";
    }
}

@end
