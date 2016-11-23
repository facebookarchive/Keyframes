/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "KFVectorGradientFeatureLayer.h"

#import "KFUtilities.h"
#import "KFVectorAnimation.h"
#import "KFVectorAnimationKeyValue.h"
#import "KFVectorBezierPathsHelper.h"
#import "KFVectorFeature.h"
#import "KFVectorFeatureKeyFrame.h"
#import "KFVectorGradientEffect.h"
#import "KFVectorLayerHelper.h"

@implementation KFVectorGradientFeatureLayer
{
  KFVectorFeature *_feature;
  NSArray *_keyFramePaths;
  NSArray *_keyTimes;
  CAGradientLayer *_gradientLayer;
  CAKeyframeAnimation *_colorAnimation;
}

static void setupGradientLayerWithEffect(CAGradientLayer *gradientLayer, KFVectorGradientEffect *gradientEffect, CGSize canvasSize)
{
  // Colors key frame animation
  // Build color start/end pairs
  UIColor *startColor = KFColorWithHexString([[[[gradientEffect colorStart] keyValues] firstObject] keyValue]);
  UIColor *endColor = KFColorWithHexString([[[[gradientEffect colorEnd] keyValues] firstObject] keyValue]);
  gradientLayer.colors = @[(id)startColor.CGColor, (id)endColor.CGColor];
}

- (void)setFeature:(KFVectorFeature *)feature canvasSize:(CGSize)canvasSize
{
  // Make sure feature has a gradient effect
  NSParameterAssert(feature && feature.gradientEffect);

  if ([feature.gradientEffect.gradientTypeString isEqualToString:@"linear"]) {
    _feature = feature;
    CAShapeLayer *gradientMaskLayer = [CAShapeLayer layer];
    gradientMaskLayer.frame = self.bounds;
    gradientMaskLayer.fillColor = feature.fillColor.CGColor;
    gradientMaskLayer.strokeColor = feature.strokeColor.CGColor;
    gradientMaskLayer.lineWidth = feature.strokeWidth * MIN(CGRectGetWidth(self.bounds), CGRectGetHeight(self.bounds));
    if (feature.strokeColor) {
      gradientMaskLayer.lineCap = kCALineCapRound;
    }
    gradientMaskLayer.path = KFVectorBezierPathsFromCommandList([[feature.keyFrames firstObject] paths], canvasSize, self.bounds.size).CGPath;

    _gradientLayer = [CAGradientLayer layer];
    _gradientLayer.frame = self.bounds;
    setupGradientLayerWithEffect(_gradientLayer, feature.gradientEffect, canvasSize);
    _gradientLayer.mask = gradientMaskLayer;
    [self addSublayer:_gradientLayer];
    [self _addAnimations];
  } else {
    NSAssert(@"Unknown gradient type passed in: %@", feature.gradientEffect.gradientTypeString);
  }
}

- (void)resetAnimations
{
  [super resetAnimations];
  [_gradientLayer removeAllAnimations];
  [_gradientLayer addAnimation:_colorAnimation forKey:[_colorAnimation valueForKey:@"animationKey"]];
}

- (void)_addAnimations
{
  NSMutableArray *colorsArray = [NSMutableArray array];
  for (int x = 0; x < MAX(_feature.gradientEffect.colorStart.keyValues.count, _feature.gradientEffect.colorEnd.keyValues.count); x++) {
    NSUInteger startColorIndex = x;
    if (_feature.gradientEffect.colorStart.keyValues.count <= startColorIndex) {
      startColorIndex = _feature.gradientEffect.colorStart.keyValues.count - 1;
    }
    UIColor *startColor = KFColorWithHexString([[[[_feature.gradientEffect colorStart] keyValues] objectAtIndex:startColorIndex] keyValue]);

    NSUInteger endColorIndex = x;
    if (_feature.gradientEffect.colorEnd.keyValues.count <= endColorIndex) {
      endColorIndex = _feature.gradientEffect.colorEnd.keyValues.count - 1;
    }
    UIColor *endColor = KFColorWithHexString([[[[_feature.gradientEffect colorEnd] keyValues] objectAtIndex:endColorIndex] keyValue]);

    [colorsArray addObject:@[(id)startColor.CGColor, (id)endColor.CGColor]];
  }

  _colorAnimation = [CAKeyframeAnimation animationWithKeyPath:@"colors"];
  _colorAnimation.duration = self.frameCount * 1.0 / self.frameRate;
  _colorAnimation.repeatCount = self.repeatCount;
  _colorAnimation.values = colorsArray;

  KFVectorAnimation *animationToUseForTimingCurve = _feature.gradientEffect.colorStart;
  if (_feature.gradientEffect.colorEnd.timingCurves.count == colorsArray.count - 1) {
    animationToUseForTimingCurve = _feature.gradientEffect.colorEnd;
  }
  _colorAnimation.timingFunctions = KFVectorLayerMediaTimingFunction(animationToUseForTimingCurve.timingCurves);
  _colorAnimation.keyTimes = KFMapArray(animationToUseForTimingCurve.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    return @(keyValue.startFrame * 1.0 / self.frameCount);
  });
  _colorAnimation.removedOnCompletion = NO;
  [_colorAnimation setValue:@"gradient color animation" forKey:@"animationKey"];
}

@end
