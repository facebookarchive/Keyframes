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
#import "KFVectorFeatureLayer.h"
#import "KFVectorFeatureKeyFrame.h"
#import "KFVectorGradientEffect.h"
#import "KFVectorLayerHelper.h"

@implementation KFVectorGradientFeatureLayer
{
  CAGradientLayer *_gradientLayer;
  KFVectorFeatureLayer *_gradientMaskLayer;
  CGSize _canvasSize;
  NSMutableArray *_animations;
}

- (instancetype)init
{
  self = [super init];
  if (self) {
    _animations = [NSMutableArray new];
  }
  return self;
}

- (void)setFeature:(KFVectorFeature *)feature canvasSize:(CGSize)canvasSize
{
  // Make sure feature has a gradient effect
  NSParameterAssert(feature && feature.gradientEffect);

  if ([feature.gradientEffect.gradientTypeString isEqualToString:@"linear"]) {
    _canvasSize = canvasSize;
    _gradientMaskLayer = [KFVectorFeatureLayer layer];
    _gradientMaskLayer.formatVersion = self.formatVersion;
    _gradientMaskLayer.name = [self.name stringByAppendingString:@"+maskLayer"];
    _gradientMaskLayer.frameRate = self.frameRate;
    _gradientMaskLayer.frameCount = self.frameCount;
    _gradientMaskLayer.frame = self.bounds;
    _gradientMaskLayer.repeatCount = self.repeatCount;
    [_gradientMaskLayer setFeature:feature canvasSize:canvasSize];

    _gradientLayer = [CAGradientLayer layer];
    _gradientLayer.frame = self.bounds;
    _gradientLayer.mask = _gradientMaskLayer;
    [self addSublayer:_gradientLayer];

    [self _addColorsAnimationsWithStartColor:feature.gradientEffect.colorStart endColor:feature.gradientEffect.colorEnd];
    if (feature.gradientEffect.rampStart) {
      [self _addRampStartPointAnimation:feature.gradientEffect.rampStart];
    }
    if (feature.gradientEffect.rampEnd) {
      [self _addRampEndPointAnimation:feature.gradientEffect.rampEnd];
    }
  } else {
    NSAssert(@"Unknown gradient type passed in: %@", feature.gradientEffect.gradientTypeString);
  }
}

- (void)resetAnimations
{
  [super resetAnimations];
  [_gradientMaskLayer resetAnimations];
  [_gradientLayer removeAllAnimations];
  for (CAPropertyAnimation *animation in _animations) {
    [_gradientLayer addAnimation:animation forKey:animation.keyPath];
  }
}

- (void)setRepeatCount:(float)repeatCount
{
  super.repeatCount = repeatCount;
  _gradientMaskLayer.repeatCount = repeatCount;
  _gradientLayer.repeatCount = repeatCount;
  
  for (CAPropertyAnimation *animation in _animations) {
    animation.repeatCount = repeatCount;
  }
}

- (void)setAnimations:(NSArray<KFVectorAnimation *> *)animations scaleToCanvas:(CGPoint)scaleToCanvas scaleToLayer:(CGPoint)scaleToLayer
{
  [_gradientMaskLayer setAnimations:animations scaleToCanvas:scaleToCanvas scaleToLayer:scaleToLayer];
}

- (void)_addColorsAnimationsWithStartColor:(KFVectorAnimation *)colorStart endColor:(KFVectorAnimation *)colorEnd
{
  NSMutableArray *colorsArray = [NSMutableArray array];
  for (int x = 0; x < MAX(colorStart.keyValues.count, colorEnd.keyValues.count); x++) {
    NSUInteger startColorIndex = x;
    if (colorStart.keyValues.count <= startColorIndex) {
      startColorIndex = colorStart.keyValues.count - 1;
    }
    UIColor *startColor = KFColorWithHexString([[[colorStart keyValues] objectAtIndex:startColorIndex] keyValue]);

    NSUInteger endColorIndex = x;
    if (colorEnd.keyValues.count <= endColorIndex) {
      endColorIndex = colorEnd.keyValues.count - 1;
    }
    UIColor *endColor = KFColorWithHexString([[[colorEnd keyValues] objectAtIndex:endColorIndex] keyValue]);

    [colorsArray addObject:@[(id)startColor.CGColor, (id)endColor.CGColor]];
  }
  if (colorsArray.count > 1) {
    CAKeyframeAnimation *colorAnimation = [CAKeyframeAnimation animationWithKeyPath:@"colors"];
    colorAnimation.duration = self.frameCount * 1.0 / self.frameRate;
    colorAnimation.repeatCount = self.repeatCount;
    colorAnimation.values = colorsArray;

    KFVectorAnimation *animationToUseForTimingCurve = colorStart;
    if (colorEnd.timingCurves.count == colorsArray.count - 1) {
      animationToUseForTimingCurve = colorEnd;
    }
    colorAnimation.timingFunctions = KFVectorLayerMediaTimingFunction(animationToUseForTimingCurve.timingCurves);
    colorAnimation.keyTimes = KFMapArray(animationToUseForTimingCurve.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
      return @(keyValue.startFrame * 1.0 / self.frameCount);
    });
    colorAnimation.removedOnCompletion = NO;
    [_animations addObject:colorAnimation];
  }

  // When layer is initialized, and unanimated, layer renders without the first animation applied. This fixes it.
  _gradientLayer.colors = [colorsArray firstObject];
}

- (void)_addRampStartPointAnimation:(KFVectorAnimation *)rampStart
{
  NSArray *rampStartValues = KFMapArray(rampStart.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    NSArray *value = keyValue.keyValue;
    CGPoint point = CGPointMake([value[0] floatValue] / self->_canvasSize.width, [value[1] floatValue] / self->_canvasSize.height);
    return [NSValue valueWithCGPoint:CGPointMake(point.x, point.y)];
  });
  if (rampStartValues.count > 1) {
    CAKeyframeAnimation *rampStartAnimation = [CAKeyframeAnimation animationWithKeyPath:@"startPoint"];
    rampStartAnimation.duration = self.frameCount * 1.0 / self.frameRate;
    rampStartAnimation.repeatCount = self.repeatCount;
    rampStartAnimation.values = rampStartValues;
    rampStartAnimation.keyTimes = KFMapArray(rampStart.keyValues, ^id(KFVectorAnimationKeyValue *keyFrame) {
      return @(keyFrame.startFrame * 1.0 / self.frameCount);
    });
    rampStartAnimation.timingFunctions = KFVectorLayerMediaTimingFunction(rampStart.timingCurves);
    rampStartAnimation.fillMode = kCAFillModeBoth;
    rampStartAnimation.removedOnCompletion = NO;
    [_animations addObject:rampStartAnimation];
  }
  
  // When layer is initialized, and unanimated, layer renders without the first animation applied. This fixes it.
  _gradientLayer.startPoint = [[rampStartValues firstObject] CGPointValue];
}

- (void)_addRampEndPointAnimation:(KFVectorAnimation *)rampEnd
{
  NSArray *rampEndValues = KFMapArray(rampEnd.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    NSArray *value = keyValue.keyValue;
    CGPoint point = CGPointMake([value[0] floatValue] / self->_canvasSize.width, [value[1] floatValue] / self->_canvasSize.height);
    return [NSValue valueWithCGPoint:CGPointMake(point.x, point.y)];
  });
  if (rampEndValues.count > 1) {
    CAKeyframeAnimation *rampEndAnimation = [CAKeyframeAnimation animationWithKeyPath:@"endPoint"];
    rampEndAnimation.duration = self.frameCount * 1.0 / self.frameRate;
    rampEndAnimation.repeatCount = self.repeatCount;
    rampEndAnimation.values = rampEndValues;
    rampEndAnimation.keyTimes = KFMapArray(rampEnd.keyValues, ^id(KFVectorAnimationKeyValue *keyFrame) {
      return @(keyFrame.startFrame * 1.0 / self.frameCount);
    });
    rampEndAnimation.timingFunctions = KFVectorLayerMediaTimingFunction(rampEnd.timingCurves);
    rampEndAnimation.fillMode = kCAFillModeBoth;
    rampEndAnimation.removedOnCompletion = NO;
    [_animations addObject:rampEndAnimation];
  }
  
  // When layer is initialized, and unanimated, layer renders without the first animation applied. This fixes it.
  _gradientLayer.endPoint = [[rampEndValues firstObject] CGPointValue];
}

#pragma mark - NSCoding

- (instancetype)initWithCoder:(NSCoder *)coder
{
  self = [super initWithCoder:coder];
  if (self) {
    _animations = [coder decodeObjectForKey:@"KFVectorGradientFeatureLayer*_animations"];
    _gradientLayer = (CAGradientLayer *)[[self sublayers] firstObject];
    _gradientMaskLayer = (KFVectorFeatureLayer *)_gradientLayer.mask;
  }
  return self;
}

-(void)encodeWithCoder:(NSCoder *)aCoder
{
  [super encodeWithCoder:aCoder];
  [aCoder encodeObject:_animations forKey:@"KFVectorGradientFeatureLayer*_animations"];
}

@end
