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
 * The name of the input file is KFVectorFeature.value
 */

#import <Foundation/Foundation.h>
#import <CoreGraphics/CGGeometry.h>
#import <UIKit/UIColor.h>
#import <CoreGraphics/CGBase.h>
#import <keyframes/KFVectorGradientEffect.h>

/**
 * Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 * 
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 * 
 */
@interface KFVectorFeature : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly, copy) NSString *name;
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
@property (nonatomic, readonly, copy) KFVectorGradientEffect *gradientEffect;

- (instancetype)initWithName:(NSString *)name featureSize:(CGSize)featureSize animationGroupId:(NSInteger)animationGroupId fromFrame:(NSUInteger)fromFrame toFrame:(NSUInteger)toFrame fillColor:(UIColor *)fillColor strokeColor:(UIColor *)strokeColor strokeWidth:(CGFloat)strokeWidth strokeLineCap:(NSString *)strokeLineCap keyFrames:(NSArray *)keyFrames timingCurves:(NSArray *)timingCurves featureAnimations:(NSArray *)featureAnimations backedImage:(NSString *)backedImage gradientEffect:(KFVectorGradientEffect *)gradientEffect;

@end

