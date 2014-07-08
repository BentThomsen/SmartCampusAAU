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

#import <math.h>
#import "GMLParser.h"
#import "DictionaryGraph.h"
#import "Vertex.h"
#import "Edge.h"

@interface GMLParser() {
    MFParser *manifest;
    NSString *currentElement;
    
    NSMutableDictionary *vertexDic;
	
    NSMutableArray *nodes;
    NSMutableArray *edges;
    
    NSMutableDictionary *node;
    NSMutableDictionary *location;
    NSMutableDictionary *point;
    NSMutableArray *metadata;
    NSMutableDictionary *meta;
    NSMutableString *currentMetaName, *currentMetaValue;
    NSMutableDictionary *edge;
}
@end

@implementation GMLParser

@synthesize parser = _parser;
@synthesize graph = _graph;

-(id)initWithData:(NSData*)data manifest:(MFParser*)mf {
    if (self = [super init]) {
        _parser = [[NSXMLParser alloc] initWithData:data];
        self.parser.delegate = self;
        manifest = mf;
    }
    return self;
}

-(BOOL)parse {
    _graph = [[DictionaryGraph alloc] init];
    nodes = [[NSMutableArray alloc] init];
    edges = [[NSMutableArray alloc] init];
    
    if ([self.parser parse]) {
        vertexDic = [[NSMutableDictionary alloc] init];
        int n = 0;
        for (NSDictionary *nodeDic in nodes) {
            NSString *id = [nodeDic objectForKey:@"id"];
            
            AbsoluteLocation *nodeLocation;
            NSDictionary *locationDic = [nodeDic objectForKey:@"location"];
            if (locationDic) {
                double lat = [[locationDic objectForKey:@"lat"] doubleValue];
                double lon = [[locationDic objectForKey:@"lon"] doubleValue];
                double alt = [[locationDic objectForKey:@"alt"] doubleValue];
                nodeLocation = [[AbsoluteLocation alloc] initWithLatitude:lat longitude:lon altitude:alt];
            } else {
                nodeLocation = [self locationFromPoint:[nodeDic objectForKey:@"point"]];
            }
            
            if (nodeLocation) {
                Vertex *vertex = [[Vertex alloc] initWithId:n++ andAbsoluteLocation:nodeLocation];
                [vertexDic setObject:vertex forKey:id]; 
            }
        }
        
        for (NSDictionary *edgeDic in edges) {
            Vertex *orig = [vertexDic objectForKey:[edgeDic objectForKey:@"from"]];
            Vertex *dest = [vertexDic objectForKey:[edgeDic objectForKey:@"to"]];
            if (orig && dest) {
                int dist = [self convertWeightToDist:[[edgeDic objectForKey:@"weight"] doubleValue]]; 
                Edge *tmpEdge = [[Edge alloc] initWithOrigin:orig andDestination:dest distance:dist];
                
                [self.graph addUndirectionalEdge:tmpEdge];            
            }
        }
        return true;
    }
    return false;
}

-(int)convertWeightToDist:(double)weight {
    //weight converted to meters
    if (manifest.units == MFParserUnitsMillimeters)
        weight = weight / 1000;
    if (manifest.units == MFParserUnitsCentimeters)
        weight = weight / 100;
    
    return (int)weight;
}

-(AbsoluteLocation*)locationFromPoint:(NSDictionary*)p {
    
    AbsoluteLocation *referencePoints = manifest.location;
    Vector2d *directionOfNorth = manifest.directionOfNorth;
    
    double x = [[p objectForKey:@"x"] doubleValue];
    double y = [[p objectForKey:@"y"] doubleValue];
    
    Vector2d *pointVector = [[Vector2d alloc] initWithX:x y:y];
    
    double angleToVector = [directionOfNorth clockwiseAngleToVector:pointVector];
    return [self offsetLocation:referencePoints angle:angleToVector distance:[pointVector length]];
}

-(AbsoluteLocation*)offsetLocation:(AbsoluteLocation*)loc angle:(double)angle distance:(double)distance {
    distance = distance / 1000.0; //convert to meters
    double lat1 = [self radiansFromDegrees:loc.latitude];
    double lon1 = [self radiansFromDegrees:loc.longitude];

    double radius = [self radiusOfEarth];
    double lat2 = asin(sin(lat1) * cos(distance / radius) + cos(lat1) * sin(distance / radius) * cos(angle));
    
    double x = sin(angle) * sin(distance / radius) * cos(lat1);
    double y = cos(distance / radius) - sin(lat1) * sin(lat2);
    double lon2 = lon1 + atan2(x, y);
    
    return [[AbsoluteLocation alloc] initWithLatitude:[self degreesFromRadians:lat2] longitude:[self degreesFromRadians:lon2] altitude:0];
}

- (double) degreesFromRadians:(double)radians {
    return radians * 180.0 / M_PI;
}

- (double) radiansFromDegrees:(double)degrees {
    return degrees * M_PI / 180.0;
}

-(double) radiusOfEarth {
    return 6378137.0;
}

#pragma mark NSXMLParserDelegate

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict {
    
    currentElement = [elementName copy];
    if ([elementName isEqualToString:@"node"]) {
        node = [[NSMutableDictionary alloc] initWithDictionary:attributeDict];
    } else if ([elementName isEqualToString:@"location"]) {
        location = [[NSMutableDictionary alloc] initWithDictionary:attributeDict];
    } else if ([elementName isEqualToString:@"point"]) {
        point = [[NSMutableDictionary alloc] initWithDictionary:attributeDict];
    } else if ([elementName isEqualToString:@"metadata"]) {
        metadata = [[NSMutableArray alloc] init];
    } else if ([elementName isEqualToString:@"meta"]) {
        meta = [[NSMutableDictionary alloc] init];
        currentMetaName = [attributeDict objectForKey:@"name"];
		currentMetaValue = [[NSMutableString alloc] init];        
    }else if ([elementName isEqualToString:@"edge"]) {
        edge = [[NSMutableDictionary alloc] initWithDictionary:attributeDict];
    }
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName {
    if ([elementName isEqualToString:@"node"]) {
        if (point)
            [node setObject:point forKey:@"point"];
        
        [nodes addObject:[node copy]];
    } else if ([elementName isEqualToString:@"metadata"]) {
        [node setObject:metadata forKey:@"metadata"];
    } else if ([elementName isEqualToString:@"meta"]) {
		[meta setObject:currentMetaName forKey:@"name"];
		[meta setObject:currentMetaValue forKey:@"value"];
		[metadata addObject:[meta copy]];        
    } else if ([elementName isEqualToString:@"edge"]) {
        [edges addObject:[edge copy]];
    }
    
    currentElement = nil;
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string {
    if ([currentElement isEqualToString:@"meta"]) {
        [currentMetaValue appendString:string];
    }
}

@end
