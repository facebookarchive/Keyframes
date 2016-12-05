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
 * The name of the input file is KFVectorGradientEffect.value
 */

#import <Foundation/Foundation.h>
#import <keyframes/KFVectorAnimation.h>
#import "KFVectorAnimation.h"

/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 * 
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
@interface KFVectorGradientEffect : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly, copy) NSString *gradientTypeString;
@property (nonatomic, readonly, copy) KFVectorAnimation *colorStart;
@property (nonatomic, readonly, copy) KFVectorAnimation *colorEnd;

- (instancetype)initWithGradientTypeString:(NSString *)gradientTypeString colorStart:(KFVectorAnimation *)colorStart colorEnd:(KFVectorAnimation *)colorEnd;

@end

