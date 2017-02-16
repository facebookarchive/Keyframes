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
#import "UIBezierPath+KFVectorLayer.h"

@implementation KFVectorFeatureLayer
{
  NSArray *_keyFramePaths;
  NSArray *_keyTimes;
  NSArray *_timingFunctions;
  CAKeyframeAnimation *_pathAnimation;
}

- (void)setFeature:(KFVectorFeature *)feature canvasSize:(CGSize)canvasSize
{
  if (feature.pathTrim) {
    // path trimming is supported by precompute all paths
    NSAssert(feature.keyFrames.count == 1, @"does not support path trimming w/ shape morphing");

    CGFloat duration = 1.0 * self.frameCount / self.frameRate;
    NSMutableArray *keyFramePaths = [NSMutableArray new];
    NSMutableArray *keyTimes = [NSMutableArray new];
    KFVectorFeatureKeyFrame *firstKeyframe = [feature.keyFrames firstObject];
    UIBezierPath *path = KFVectorBezierPathsFromCommandList(firstKeyframe.paths, canvasSize, self.bounds.size);
    for (NSUInteger i = feature.fromFrame; i <= feature.toFrame; ++i) {
      CGFloat progress = 1.0 * i / self.frameCount;
      CGFloat offset = KFVectorTimingFunctionValueAtTime(feature.pathTrim.pathTrimOffset, progress * duration, self.frameRate);
      CGFloat start = KFVectorTimingFunctionValueAtTime(feature.pathTrim.pathTrimStart, progress * duration, self.frameRate) / 100.0;
      CGFloat end = KFVectorTimingFunctionValueAtTime(feature.pathTrim.pathTrimEnd, progress * duration, self.frameRate) / 100.0;
      offset = fmod(offset, 360) / 360;
      start = start + offset;
      end = end + offset;
      if (start < 0 || end < 0) {
        start += 1.0;
        end += 1.0;
      }
      if (start > 1.0 || end > 1.0) {
        start -= 1.0;
        end -= 1.0;
      }
      start = MAX(MIN(start, 1.0), 0.0);
      end = MAX(MIN(end, 1.0), 0.0);
      [keyFramePaths addObject:(__bridge id)[path pathTrimFrom:start to:end].CGPath];
      [keyTimes addObject:@(progress)];
    }

    _keyFramePaths = keyFramePaths;
    _keyTimes = keyTimes;
    _timingFunctions = nil;
  } else {
    _keyFramePaths = KFMapArray(feature.keyFrames, ^id(KFVectorFeatureKeyFrame *keyFrame) {
      return (__bridge id)KFVectorBezierPathsFromCommandList(keyFrame.paths, canvasSize, self.bounds.size).CGPath;
    });

    _keyTimes = KFMapArray(feature.keyFrames, ^id(KFVectorFeatureKeyFrame *keyFrame) {
      return @(keyFrame.startFrame * 1.0 / self.frameCount);
    });

    _timingFunctions = KFVectorLayerMediaTimingFunction(feature.timingCurves);
  }

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

  [self _addAnimations];
}

- (void)resetAnimations
{
  [super resetAnimations];
  [self addAnimation:_pathAnimation forKey:_pathAnimation.keyPath];
}

- (void)setRepeatCount:(float)repeatCount
{
  super.repeatCount = repeatCount;
  _pathAnimation.repeatCount = repeatCount;
}

- (void)_addAnimations
{
  // Apply animations.
  if (_keyFramePaths.count > 1) {
    _pathAnimation = [CAKeyframeAnimation animationWithKeyPath:@"path"];
    _pathAnimation.duration = self.frameCount * 1.0 / self.frameRate;
    _pathAnimation.repeatCount = self.repeatCount;
    _pathAnimation.values = _keyFramePaths;
    _pathAnimation.keyTimes = _keyTimes;
    _pathAnimation.timingFunctions = _timingFunctions;
    _pathAnimation.fillMode = kCAFillModeBoth;
    _pathAnimation.removedOnCompletion = NO;
  }

  self.path = (__bridge CGPathRef)[_keyFramePaths firstObject];
}

#pragma mark - NSCoding

- (instancetype)initWithCoder:(NSCoder *)coder
{
  self = [super initWithCoder:coder];
  if (self) {
    _pathAnimation = [coder decodeObjectForKey:@"KFVectorFeatureLayer*_pathAnimation"];
  }
  return self;
}

-(void)encodeWithCoder:(NSCoder *)aCoder
{
  [super encodeWithCoder:aCoder];
  [aCoder encodeObject:_pathAnimation forKey:@"KFVectorFeatureLayer*_pathAnimation"];
}

@end
