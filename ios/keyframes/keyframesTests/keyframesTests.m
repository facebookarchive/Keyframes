/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

#import <XCTest/XCTest.h>

#import <keyframes/KFVector.h>
#import <keyframes/KFVectorLayer.h>
#import <keyframes/KFVectorParsingHelper.h>

@interface keyframesTests : XCTestCase

@end

@implementation keyframesTests

- (KFVector *)loadLikeVectorFromDisk
{
  static KFVector *likeVector;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    NSString *filePath = [[NSBundle bundleForClass:[self class]] pathForResource:@"sample_like" ofType:@"json" inDirectory:nil];
    NSData *data = [NSData dataWithContentsOfFile:filePath];
    NSDictionary *likeVectorDictionary = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:nil];
    likeVector = KFVectorFromDictionary(likeVectorDictionary);
  });
  return likeVector;
}

- (void)setUp {
  [super setUp];
  // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
  // Put teardown code here. This method is called after the invocation of each test method in the class.
  [super tearDown];
}

- (void)testLikeVectorParsing {
  // Simple test to make sure we can parse deserialize and parse the json into our model.
  KFVector *vector = [self loadLikeVectorFromDisk];
  XCTAssertNotNil(vector, @"vector should not be nil");
  
  // vector should be a dictionary of attributes
  XCTAssertTrue(vector.canvasSize.height == 177.0 && vector.canvasSize.width == 177.0, @"vector canvas should be {177,177}");
  XCTAssertTrue([vector.name isEqualToString:@"LIKE"], @"vector name should be 'LIKE'");
  XCTAssertTrue(vector.frameRate == 24, @"animation frame rate should be 24");
  XCTAssertTrue(vector.animationFrameCount == 85, @"animation frame count should be 85");
  XCTAssertTrue([vector.features count] == 3, @"like should contain 3 features (hand, cuff, background");
  XCTAssertTrue([vector.animationGroups count] == 1, @"like should contain 1 animation group");
  
  XCTAssertTrue([[vector.features[0] name] isEqualToString:@"BG"], @"first feature should have name 'BG'");
  XCTAssertTrue([[vector.features[1] name] isEqualToString:@"Cuff"], @"first feature should have name 'Cuff'");
  XCTAssertTrue([[vector.features[2] name] isEqualToString:@"Hand"], @"first feature should have name 'Hand'");
}

- (void)testLikeVectorLayer {
  // Simple test to make sure layer is created with expected sublayers.
  KFVectorLayer *likeVectorLayer = [KFVectorLayer new];
  likeVectorLayer.frame = CGRectMake(0, 0, 177, 177);
  likeVectorLayer.faceModel = [self loadLikeVectorFromDisk];
  
  // layer should contain sublayers with like vectors.
  XCTAssertTrue(likeVectorLayer.sublayers.count == 1, @"like vector layer should contain one sublayer");
  NSArray *rootLayers = [likeVectorLayer.sublayers[0] sublayers];
  XCTAssertTrue(rootLayers.count == 2, @"like vector's sublayer should contain two layers (bg and an animation layer)");
  id bgLayer = [rootLayers objectAtIndex:0];
  XCTAssertTrue([[bgLayer name] isEqualToString:@"BG"], @"first layer in root layer should be a bg layer");
  id masterAnimationLayer = [rootLayers objectAtIndex:1];
  XCTAssertTrue([[masterAnimationLayer name] isEqualToString:@"Master Animation"], @"second layer in root layer should be 'Master Animation' layer");
}

@end
