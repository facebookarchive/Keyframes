/**
 * Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 *
 */
/**
 * This file is generated using the remodel generation script.
 * The name of the input file is KFVector.value
 */

#import <Foundation/Foundation.h>
#import <CoreGraphics/CGGeometry.h>

/**
 * Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 * 
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 * 
 */
@interface KFVector : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly) CGSize canvasSize;
@property (nonatomic, readonly, copy) NSString *name;
@property (nonatomic, readonly, copy) NSString *formatVersion;
@property (nonatomic, readonly) NSInteger key;
@property (nonatomic, readonly) NSUInteger frameRate;
@property (nonatomic, readonly) NSUInteger animationFrameCount;
@property (nonatomic, readonly, copy) NSArray *features;
@property (nonatomic, readonly, copy) NSArray *animationGroups;

- (instancetype)initWithCanvasSize:(CGSize)canvasSize name:(NSString *)name formatVersion:(NSString *)formatVersion key:(NSInteger)key frameRate:(NSUInteger)frameRate animationFrameCount:(NSUInteger)animationFrameCount features:(NSArray *)features animationGroups:(NSArray *)animationGroups;

@end

