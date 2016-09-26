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
 * The name of the input file is KFVectorAnimation.value
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
@interface KFVectorAnimation : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly, copy) NSString *property;
@property (nonatomic, readonly) CGPoint anchor;
@property (nonatomic, readonly, copy) NSArray *keyValues;
@property (nonatomic, readonly, copy) NSArray *timingCurves;

- (instancetype)initWithProperty:(NSString *)property anchor:(CGPoint)anchor keyValues:(NSArray *)keyValues timingCurves:(NSArray *)timingCurves;

@end

