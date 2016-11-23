/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "KFVectorFeatureLayer.h"

#import "KFVectorAnimation.h"
#import "KFVectorAnimationKeyValue.h"
#import "KFVectorBezierPathsHelper.h"
#import "KFVectorFeature.h"
#import "KFVectorFeatureKeyFrame.h"
#import "KFVectorGradientEffect.h"
#import "KFVectorLayerHelper.h"
#import "KFUtilities.h"

@implementation KFVectorFeatureLayer
{
  KFVectorFeature *_feature;
  NSArray *_keyFramePaths;
  NSArray *_keyTimes;
  CAKeyframeAnimation *_pathAnimation;
}

- (void)setFeature:(KFVectorFeature *)feature canvasSize:(CGSize)canvasSize
{
  // Shape layer does not support gradient effect feature. Use KFVectorGradientFeatureLayer instead
  NSParameterAssert(feature && feature.gradientEffect == nil);

  _feature = feature;

  // Initialize to the first key frame.
  _keyFramePaths = KFMapArray(feature.keyFrames, ^id(KFVectorFeatureKeyFrame *keyFrame) {
    return (__bridge id)KFVectorBezierPathsFromCommandList(keyFrame.paths, canvasSize, self.bounds.size).CGPath;
  });

  _keyTimes = KFMapArray(feature.keyFrames, ^id(KFVectorFeatureKeyFrame *keyFrame) {
    return @(keyFrame.startFrame * 1.0 / self.frameCount);
  });

  self.fillColor = feature.fillColor.CGColor;
  self.strokeColor = feature.strokeColor.CGColor;
  self.lineWidth = feature.strokeWidth * MIN(CGRectGetWidth(self.bounds), CGRectGetHeight(self.bounds));

  // TO DO: for backward capability, should be deprecated
  if (KFVersionLessThan(self.formatVersion, @"1.0")) {
    if (feature.strokeColor) {
      self.lineCap = kCALineCapRound;
    }
  }

  if (!KFVersionLessThan(self.formatVersion, @"1.0")) {
    if ([feature.strokeLineCap isEqualToString:@"butt"]) {
      self.lineCap = kCALineCapButt;
    } else if ([feature.strokeLineCap isEqualToString:@"round"]) {
      self.lineCap = kCALineCapRound;
    } else if ([feature.strokeLineCap isEqualToString:@"square"]) {
      self.lineCap = kCALineCapSquare;
    }
  }

  self.path = (__bridge CGPathRef)[_keyFramePaths firstObject];

  [self _addAnimations];
}

- (void)resetAnimations
{
  [super resetAnimations];
  [self addAnimation:_pathAnimation forKey:[_pathAnimation valueForKey:@"animationKey"]];
}

- (void)_addAnimations
{
  // Apply animations.
  if (_feature.keyFrames.count > 1) {
    _pathAnimation = [CAKeyframeAnimation animationWithKeyPath:@"path"];
    _pathAnimation.duration = self.frameCount * 1.0 / self.frameRate;
    _pathAnimation.repeatCount = self.repeatCount;
    _pathAnimation.values = _keyFramePaths;
    _pathAnimation.keyTimes = _keyTimes;
    _pathAnimation.timingFunctions = KFVectorLayerMediaTimingFunction(_feature.timingCurves);
    _pathAnimation.fillMode = kCAFillModeBoth;
    _pathAnimation.removedOnCompletion = NO;
    [_pathAnimation setValue:@"path key frame animation" forKey:@"animationKey"];
  }
}

@end
