//
//  MFParser.m
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

#import "MFParser.h"

@interface MFParser() {
    NSString *currentElement;
    
    NSMutableDictionary *location;
    NSMutableDictionary *directionOfNorth;
    NSMutableArray *graphPaths;
}
@end

@implementation MFParser

@synthesize parser = _parser;
@synthesize location = _location;
@synthesize directionOfNorth = _directionOfNorth;
@synthesize units = _units;
@synthesize gmlPaths = _gmlPaths;

-(id)initWithData:(NSData*)data {
    if (self = [super init]) {
        _parser = [[NSXMLParser alloc] initWithData:data];
        self.parser.delegate = self;
    }
    return self;
}

-(BOOL)parse {
    _gmlPaths = [[NSMutableArray alloc] init];
    _units = MFParserUnitsUnknown;
    return [self.parser parse];
}

-(void)setUnitsFromString:(NSString*)units {
    _units = MFParserUnitsUnknown;
    if ([units isEqualToString:@"millimeters"])
        _units = MFParserUnitsMillimeters;
    else if ([units isEqualToString:@"centimeters"])
        _units = MFParserUnitsCentimeters;
    else if ([units isEqualToString:@"meters"])
        _units = MFParserUnitsMeters;
}

#pragma mark NSXMLParserDelegate

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict {
    
    currentElement = [elementName copy];
    if ([elementName isEqualToString:@"location"]) {
        location = [[NSMutableDictionary alloc] initWithDictionary:attributeDict];
    } else if ([elementName isEqualToString:@"direction-of-north"]) {
        directionOfNorth = [[NSMutableDictionary alloc] initWithDictionary:attributeDict];
    } else if ([elementName isEqualToString:@"model-units"]) {
        NSString *units = [attributeDict objectForKey:@"length-unit"];
        [self setUnitsFromString:units];
    } else if ([elementName isEqualToString:@"graphs"]) {
        graphPaths = [[NSMutableArray alloc] init];
    } else if ([elementName isEqualToString:@"graph"]) {
        NSString *graphPath = [attributeDict objectForKey:@"path"];
        if (graphPath)  {
            [graphPaths addObject:graphPath];
        }
    }

}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName {
    if ([elementName isEqualToString:@"location"]) {
        double alt = [[location objectForKey:@"alt"] doubleValue];
        double lat = [[location objectForKey:@"lat"] doubleValue];
        double lon = [[location objectForKey:@"lon"] doubleValue];
        _location = [[AbsoluteLocation alloc] initWithLatitude:lat longitude:lon altitude:alt];
    } else if ([elementName isEqualToString:@"direction-of-north"]) {
        double x = [[directionOfNorth objectForKey:@"x"] doubleValue];
        double y = [[directionOfNorth objectForKey:@"y"] doubleValue];
        _directionOfNorth = [[Vector2d alloc] initWithX:x y:y];
    } else if ([elementName isEqualToString:@"graphs"]) {
        for (NSString *path in graphPaths) {
            NSString *fileName = [path stringByDeletingPathExtension];
            NSString *fileExtension = [path pathExtension];
            
            NSString *bundlePath = [[NSBundle mainBundle] pathForResource:fileName ofType:fileExtension];
            
            if (bundlePath) {
                [self.gmlPaths addObject:bundlePath];
            }
        }
    }
    
    currentElement = nil;
}

@end
