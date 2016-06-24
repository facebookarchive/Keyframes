// Copyright 2004-present Facebook. All Rights Reserved.

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
    return @(keyFrame.startFrame * 1.0 / feature.animationFrameCount);
  });

  self.fillColor = feature.fillColor.CGColor;
  self.strokeColor = feature.strokeColor.CGColor;
  self.lineWidth = feature.strokeWidth * MIN(CGRectGetWidth(self.bounds), CGRectGetHeight(self.bounds));
  if (feature.strokeColor) {
    self.lineCap = kCALineCapRound;
  }

  self.path = (__bridge CGPathRef)[_keyFramePaths firstObject];

  [self _addAnimations];
}

- (void)_addAnimations
{
  // Apply animations.
  if (_feature.keyFrames.count > 1) {
    CAKeyframeAnimation *keyFrameAnimation = [CAKeyframeAnimation animationWithKeyPath:@"path"];
    keyFrameAnimation.duration = _feature.animationFrameCount * 1.0 / _feature.frameRate;
    keyFrameAnimation.repeatCount = HUGE_VALF;
    keyFrameAnimation.values = _keyFramePaths;
    keyFrameAnimation.keyTimes = _keyTimes;
    keyFrameAnimation.timingFunctions = KFVectorLayerMediaTimingFunction(_feature.timingCurves);
    [self addAnimation:keyFrameAnimation forKey:@"path key frame animation"];
  }
}

@end
