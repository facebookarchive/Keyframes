/**
* Copyright (c) 2016-present, Facebook, Inc.
* All rights reserved.
*
* This source code is licensed under the BSD-style license found in the
* LICENSE file in the root directory of this source tree. An additional grant
* of patent rights can be found in the PATENTS file in the same directory.
*/
/**
 * This file is generated using the remodel generation script.
 * The name of the input file is KFVectorAnimation.value
 */

#import <Foundation/Foundation.h>
#import <CoreGraphics/CGGeometry.h>

/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 * 
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
@interface KFVectorAnimation : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly, copy) NSString *property;
@property (nonatomic, readonly) CGPoint anchor;
@property (nonatomic, readonly, copy) NSArray *keyValues;
@property (nonatomic, readonly, copy) NSArray *timingCurves;

- (instancetype)initWithProperty:(NSString *)property anchor:(CGPoint)anchor keyValues:(NSArray *)keyValues timingCurves:(NSArray *)timingCurves;

@end

