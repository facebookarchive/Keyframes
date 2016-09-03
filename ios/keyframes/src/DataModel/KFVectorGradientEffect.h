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
 * The name of the input file is KFVectorGradientEffect.value
 */

#import <Foundation/Foundation.h>
#import <keyframes/KFVectorAnimation.h>
#import "KFVectorAnimation.h"

/**
 * Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 * 
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 * 
 */
@interface KFVectorGradientEffect : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly, copy) NSString *gradientTypeString;
@property (nonatomic, readonly, copy) KFVectorAnimation *colorStart;
@property (nonatomic, readonly, copy) KFVectorAnimation *colorEnd;

- (instancetype)initWithGradientTypeString:(NSString *)gradientTypeString colorStart:(KFVectorAnimation *)colorStart colorEnd:(KFVectorAnimation *)colorEnd;

@end

