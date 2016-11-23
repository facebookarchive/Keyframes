/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import <XCTest/XCTest.h>

#import <keyframes/KFVector.h>
#import <keyframes/KFVectorLayer.h>
#import <keyframes/KFVectorParsingHelper.h>

@interface keyframesTests : XCTestCase

@end

@implementation keyframesTests

- (KFVector *)loadLogoVectorFromDisk
{
  static KFVector *logoVector;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    NSString *filePath = [[NSBundle bundleForClass:[self class]] pathForResource:@"sample_logo" ofType:@"json" inDirectory:nil];
    NSData *data = [NSData dataWithContentsOfFile:filePath];
    NSDictionary *logoVectorDictionary = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:nil];
    logoVector = KFVectorFromDictionary(logoVectorDictionary);
  });
  return logoVector;
}

- (void)setUp {
  [super setUp];
  // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
  // Put teardown code here. This method is called after the invocation of each test method in the class.
  [super tearDown];
}

- (void)testLogoVectorParsing {
  // Simple test to make sure we can parse deserialize and parse the json into our model.
  KFVector *vector = [self loadLogoVectorFromDisk];
  XCTAssertNotNil(vector, @"vector should not be nil");
  
  // vector should be a dictionary of attributes
  XCTAssertTrue(vector.canvasSize.height == 615.0 && vector.canvasSize.width == 615.0, @"vector canvas should be {615,615}");
  XCTAssertTrue([vector.name isEqualToString:@"KEYFRAMESLOGO"], @"vector name should be 'KEYFRAMESLOGO'");
  XCTAssertTrue(vector.frameRate == 24, @"animation frame rate should be 24");
  XCTAssertTrue(vector.animationFrameCount == 88, @"animation frame count should be 88");
  XCTAssertTrue([vector.features count] == 7, @"logo should contain 7 features (Circle, | - 4, | - 3, | - 2, | - 1, < - 2, < - 1");
  XCTAssertTrue([vector.animationGroups count] == 1, @"logo should contain 1 animation group");
  
  XCTAssertTrue([[vector.features[0] name] isEqualToString:@"Circle"], @"feature should have name 'Circle'");
  XCTAssertTrue([[vector.features[1] name] isEqualToString:@"| - 4"], @"feature should have name '| - 4'");
  XCTAssertTrue([[vector.features[2] name] isEqualToString:@"| - 3"], @"feature should have name '| - 3'");
  XCTAssertTrue([[vector.features[3] name] isEqualToString:@"| - 2"], @"feature should have name '| - 2'");
  XCTAssertTrue([[vector.features[4] name] isEqualToString:@"| - 1"], @"feature should have name '| - 1'");
  XCTAssertTrue([[vector.features[5] name] isEqualToString:@"< - 2"], @"feature should have name '< - 2'");
  XCTAssertTrue([[vector.features[6] name] isEqualToString:@"< - 1"], @"feature should have name '< - 1'");
}

- (void)testLogoVectorLayer {
  // Simple test to make sure layer is created with expected sublayers.
  KFVectorLayer *logoVectorLayer = [KFVectorLayer new];
  logoVectorLayer.frame = CGRectMake(0, 0, 615, 615);
  logoVectorLayer.faceModel = [self loadLogoVectorFromDisk];
  
  // layer should contain sublayers with logo vectors.
  XCTAssertTrue(logoVectorLayer.sublayers.count == 1, @"logo vector layer should contain one sublayer");
  NSArray *rootLayers = [logoVectorLayer.sublayers[0] sublayers];
  XCTAssertTrue(rootLayers.count == 2, @"logo vector's sublayer should contain two layers (Circle and an animation layer)");
  id bgLayer = [rootLayers objectAtIndex:0];
  XCTAssertTrue([[bgLayer name] isEqualToString:@"Circle"], @"first layer in root layer should be a 'Circle' layer");
  id masterAnimationLayer = [rootLayers objectAtIndex:1];
  XCTAssertTrue([[masterAnimationLayer name] isEqualToString:@"Parent 1"], @"second layer in root layer should be 'Parent 1' layer");
}

@end
