//
//  GMLParser.m
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

#import "SmartBMTParser.h"

@implementation SmartBMTParser

@synthesize mf = _mf;
@synthesize gmls = _gmls;
@synthesize graphs = _graphs;

+(void)test {
    #warning sample code for parsing GML files
    // Parsing GML file
    NSString *path = [[NSBundle mainBundle] pathForResource:@"Cassiopeia" ofType:@"mf"];
    NSData *manifest = [NSData dataWithContentsOfFile:path];
    SmartBMTParser *parser = [[SmartBMTParser alloc] initWithManifest:manifest];
    
    NSArray *graphs = [parser graphs];
    for (Graph *graph in graphs) {
        NSLog(@"Graph with %d vertices and %d egdes", graph.numVertices, graph.numEdges);
    }
}

-(id)initWithManifest:(NSData*)manifest {
    if (self = [super init]) {
        _gmls = [[NSMutableArray alloc] init];
        _mf = [[MFParser alloc] initWithData:manifest];
        if ([self.mf parse]) {
            for (NSString *gmlPath in self.mf.gmlPaths) {
                NSData *gmlData = [NSData dataWithContentsOfFile:gmlPath];
                
                GMLParser *gml = [[GMLParser alloc] initWithData:gmlData manifest:self.mf];
                [gml parse];
                
                [self.gmls addObject:gml];
            }
        }
    }
    return self;
}

-(NSArray *)graphs {
    if (_graphs==nil) {
        NSMutableArray *graphs = [[NSMutableArray alloc] init];
        for (GMLParser *gml in self.gmls) {
            [graphs addObject:gml.graph];
        }
        _graphs = [graphs copy];
    }
    return _graphs;
}

@end
