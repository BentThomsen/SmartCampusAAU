//
//  SOOData.m
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
//SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import "SOOData.h"

#import "NSObject+SBJson.h"

@implementation SOOData

+ (NSData *)dataWithURL:(NSString *)url error:(NSError **)error {
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:url]];
    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];

    return [NSURLConnection sendSynchronousRequest:request returningResponse:nil error:error];
}

+ (NSDictionary *)jsonWithURL:(NSString *)url error:(NSError **)error {
    NSData *response = [self dataWithURL:url error:error];
    if (response) {
        NSString *json = [[NSString alloc] initWithData:response encoding:NSUTF8StringEncoding];
        return [[json JSONValue] valueForKey:@"d"];
    } else {
        return nil;
    }
}

+ (NSData *)dataFromPost:(NSDictionary *)data toURL:(NSString *)url error:(NSError **)error {
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:url]];
    [request setHTTPMethod:@"POST"];
    [request setHTTPBody:[[data JSONRepresentation] dataUsingEncoding:NSUTF8StringEncoding]];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];

    return [NSURLConnection sendSynchronousRequest:request returningResponse:nil error:error];
}

+ (NSDictionary *)jsonFromPost:(NSDictionary *)data toURL:(NSString *)url error:(NSError **)error {
    NSData *response = [self dataFromPost:data toURL:url error:error];
    if (response) {
        NSString *json = [[NSString alloc] initWithData:response encoding:NSUTF8StringEncoding];
        return [[json JSONValue] valueForKey:@"d"];
    } else {
        return nil;
    }
}

@end
