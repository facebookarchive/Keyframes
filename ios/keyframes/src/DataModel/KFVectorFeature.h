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
 * The name of the input file is KFVectorFeature.value
 */

#import "Compatibility.h"
#import "KFVectorGradientEffect.h"
#import "KFVectorPathTrim.h"

@class KFVectorFeature;

/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 * 
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
@interface KFVectorFeature : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly, copy) NSString *name;
@property (nonatomic, readonly) NSInteger featureId;
@property (nonatomic, readonly) CGSize featureSize;
@property (nonatomic, readonly) NSInteger animationGroupId;
@property (nonatomic, readonly) NSUInteger fromFrame;
@property (nonatomic, readonly) NSUInteger toFrame;
@property (nonatomic, readonly, copy) UIColor *fillColor;
@property (nonatomic, readonly, copy) UIColor *strokeColor;
@property (nonatomic, readonly) CGFloat strokeWidth;
@property (nonatomic, readonly, copy) NSString *strokeLineCap;
@property (nonatomic, readonly, copy) NSArray *keyFrames;
@property (nonatomic, readonly, copy) NSArray *timingCurves;
@property (nonatomic, readonly, copy) NSArray *featureAnimations;
@property (nonatomic, readonly, copy) NSString *backedImage;
@property (nonatomic, readonly, copy) KFVectorFeature *masking;
@property (nonatomic, readonly, copy) KFVectorGradientEffect *gradientEffect;
@property (nonatomic, readonly, copy) KFVectorPathTrim *pathTrim;

- (instancetype)initWithName:(NSString *)name featureId:(NSInteger)featureId featureSize:(CGSize)featureSize animationGroupId:(NSInteger)animationGroupId fromFrame:(NSUInteger)fromFrame toFrame:(NSUInteger)toFrame fillColor:(UIColor *)fillColor strokeColor:(UIColor *)strokeColor strokeWidth:(CGFloat)strokeWidth strokeLineCap:(NSString *)strokeLineCap keyFrames:(NSArray *)keyFrames timingCurves:(NSArray *)timingCurves featureAnimations:(NSArray *)featureAnimations backedImage:(NSString *)backedImage masking:(KFVectorFeature *)masking gradientEffect:(KFVectorGradientEffect *)gradientEffect pathTrim:(KFVectorPathTrim *)pathTrim;

@end

