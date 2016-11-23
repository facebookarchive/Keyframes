/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 */
 
#import "KFVectorAnimationLayer.h"

#import "KFUtilities.h"
#import "KFVectorAnimation.h"
#import "KFVectorAnimationKeyValue.h"
#import "KFVectorLayerHelper.h"

@implementation KFVectorAnimationLayer
{
  CGPoint _scaleToCanvas;
  CGPoint _scaleToLayer;
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

- (void)setAnimations:(NSArray<KFVectorAnimation *> *)animations
        scaleToCanvas:(CGPoint)scaleToCanvas
         scaleToLayer:(CGPoint)scaleToLayer
{
  _scaleToCanvas = scaleToCanvas;
  _scaleToLayer = scaleToLayer;

  // TO DO: for backward capability, should be deprecated
  if (KFVersionLessThan(self.formatVersion, @"1.0")) {
    // Due to earlier bug with AE->json flow, some animation may not have anchors extracted properly.
    // Thus, loop through animations to find anchor points, and apply the non CGPointZero point if found.
    // Ideally, all animations for same layer has same anchor point from AE.
    [animations enumerateObjectsUsingBlock:^(KFVectorAnimation *_Nonnull animation, NSUInteger idx, BOOL *_Nonnull stop) {
      if (!CGPointEqualToPoint(animation.anchor, CGPointZero)) {
        *stop = YES;
        self.anchorPoint = animation.anchor;
        CGPoint position = CGPointMake(self.anchorPoint.x * CGRectGetWidth(self.bounds),
                                       self.anchorPoint.y * CGRectGetHeight(self.bounds));
        self.position = position;
      }
    }];
  }

  for (KFVectorAnimation *animation in animations) {
    if ([animation.property isEqualToString:@"STROKE_WIDTH"]) {
      [self _applyStrokeWidthAnimation:animation];
    } else if ([animation.property isEqualToString:@"SCALE"]) {
      [self _applyScaleAnimation:animation];
    } else if ([animation.property isEqualToString:@"ROTATION"]) {
      [self _applyRotationAnimation:animation];
    } else if ([animation.property isEqualToString:@"POSITION"] && KFVersionLessThan(self.formatVersion, @"1.0")) {
      // TO DO: for backward capability, should be deprecated
      [self _applyPositionAnimation:animation];
    } else if ([animation.property isEqualToString:@"ANCHOR_POINT"]) {
      [self _applyAnchorPointAnimation:animation];
    } else if ([animation.property isEqualToString:@"X_POSITION"]) {
      [self _applyXPositionAnimation:animation];
    } else if ([animation.property isEqualToString:@"Y_POSITION"]) {
      [self _applyYPositionAnimation:animation];
    } else if ([animation.property isEqualToString:@"OPACITY"]) {
      [self _applyOpacityAnimation:animation];
    }
  }
}

- (void)setLifespanFromFrame:(NSUInteger)fromFrame toFrom:(NSUInteger)toFrame
{
  if (fromFrame != 0 || toFrame != self.frameCount) {
    NSMutableArray *values = [NSMutableArray new];
    NSMutableArray *keyTimes = [NSMutableArray new];
    if (fromFrame != 0) {
      [values addObject:@YES];
      [values addObject:@NO];
      CGFloat time = fromFrame * 1.0 / self.frameCount;
      [keyTimes addObject:@(time)];
      [keyTimes addObject:@(time)];
    }
    if (toFrame != self.frameCount) {
      [values addObject:@NO];
      [values addObject:@YES];
      CGFloat time = toFrame * 1.0 / self.frameCount;
      [keyTimes addObject:@(time)];
      [keyTimes addObject:@(time)];
    }
    CAKeyframeAnimation *lifespanAnimation = [CAKeyframeAnimation animationWithKeyPath:@"hidden"];
    lifespanAnimation.duration = self.frameCount * 1.0 / self.frameRate;
    lifespanAnimation.repeatCount = self.repeatCount;
    lifespanAnimation.values = values;
    lifespanAnimation.keyTimes = keyTimes;
    lifespanAnimation.fillMode = kCAFillModeBoth;
    lifespanAnimation.removedOnCompletion = NO;
    [lifespanAnimation setValue:@"lifespan animation" forKey:@"animationKey"];
    [_animations addObject:lifespanAnimation];
  }
}

- (void)resetAnimations
{
  [self removeAllAnimations];
  for (CALayer *sublayer in self.sublayers) {
    if ([sublayer isKindOfClass:[KFVectorAnimationLayer class]]) {
      [(KFVectorAnimationLayer *)sublayer resetAnimations];
    }
  }
  for (CAPropertyAnimation *animation in _animations) {
    [self addAnimation:animation forKey:[animation valueForKey:@"animationKey"]];
  }
}


- (void)_applyScaleAnimation:(KFVectorAnimation *)scaleAnimation
{
  // Animate along the key frames, for our custom CGPoint property.
  CFTimeInterval duration = self.frameCount * 1.0 / self.frameRate;
  NSArray *xValues = KFMapArray(scaleAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    NSArray *value = keyValue.keyValue;
    return @([value[0] floatValue] / 100.0);
  });
  NSArray *yValues = KFMapArray(scaleAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    NSArray *value = keyValue.keyValue;
    return @([value[1] floatValue] / 100.0);
  });

  if (xValues.count > 1) {
    NSArray *keyTimes = KFMapArray(scaleAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyFrame) {
      return @(keyFrame.startFrame * 1.0 / self.frameCount);
    });
    NSArray *timingFunctions = KFVectorLayerMediaTimingFunction(scaleAnimation.timingCurves);

    CAKeyframeAnimation *scaleXAnim = [CAKeyframeAnimation animationWithKeyPath:@"transform.scale.x"];
    scaleXAnim.duration = duration;
    scaleXAnim.repeatCount = self.repeatCount;
    scaleXAnim.values = xValues;
    scaleXAnim.keyTimes = keyTimes;
    scaleXAnim.timingFunctions = timingFunctions;
    scaleXAnim.fillMode = kCAFillModeBoth;
    scaleXAnim.removedOnCompletion = NO;
    [scaleXAnim setValue:@"scale x animation" forKey:@"animationKey"];
    [_animations addObject:scaleXAnim];

    CAKeyframeAnimation *scaleYAnim = [CAKeyframeAnimation animationWithKeyPath:@"transform.scale.y"];
    scaleYAnim.duration = duration;
    scaleYAnim.repeatCount = self.repeatCount;
    scaleYAnim.values = yValues;
    scaleYAnim.keyTimes = keyTimes;
    scaleYAnim.timingFunctions = timingFunctions;
    scaleYAnim.fillMode = kCAFillModeBoth;
    scaleYAnim.removedOnCompletion = NO;
    [scaleYAnim setValue:@"scale y animation" forKey:@"animationKey"];
    [_animations addObject:scaleYAnim];
  }

  // When layer is initialized, and unanimated, layer renders without the first animation applied. This fixes it.
  self.transform = CATransform3DScale(self.transform,
                                      [[xValues firstObject] floatValue],
                                      [[yValues firstObject] floatValue],
                                      1.0);
}

- (void)_applyPositionAnimation:(KFVectorAnimation *)positionAnimation
{
  NSArray *firstPosition = [[positionAnimation.keyValues firstObject] keyValue];
  CGPoint firstPoint = CGPointMake([firstPosition[0] floatValue] * _scaleToCanvas.x, [firstPosition[1] floatValue] * _scaleToCanvas.y);
  CGPoint offsetFromAnchor = CGPointMake(self.position.x - firstPoint.x,
                                         self.position.y - firstPoint.y);

  CAKeyframeAnimation *anim = [CAKeyframeAnimation animationWithKeyPath:@"position"];
  anim.duration = self.frameCount * 1.0 / self.frameRate;
  anim.repeatCount = self.repeatCount;
  anim.values = KFMapArray(positionAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    NSArray *value = keyValue.keyValue;
    CGPoint position = CGPointMake([value[0] floatValue] * self->_scaleToCanvas.x, [value[1] floatValue] * self->_scaleToCanvas.y);
    return [NSValue valueWithCGPoint:CGPointMake(position.x + offsetFromAnchor.x, position.y + offsetFromAnchor.y)];
  });
  anim.keyTimes = KFMapArray(positionAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyFrame) {
    return @(keyFrame.startFrame * 1.0 / self.frameCount);
  });
  anim.timingFunctions = KFVectorLayerMediaTimingFunction(positionAnimation.timingCurves);
  anim.fillMode = kCAFillModeBoth;
  anim.removedOnCompletion = NO;
  [anim setValue:@"position animation" forKey:@"animationKey"];
  [_animations addObject:anim];
}

- (void)_applyXPositionAnimation:(KFVectorAnimation *)xPositionAnimation
{
  NSArray *xValues = KFMapArray(xPositionAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    return @([[keyValue.keyValue firstObject] floatValue] * self->_scaleToCanvas.x);
  });
  if (xValues.count > 1) {
    CAKeyframeAnimation *anim = [CAKeyframeAnimation animationWithKeyPath:@"position.x"];
    anim.duration = self.frameCount * 1.0 / self.frameRate;
    anim.repeatCount = self.repeatCount;
    anim.values = xValues;
    anim.keyTimes = KFMapArray(xPositionAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyFrame) {
      return @(keyFrame.startFrame * 1.0 / self.frameCount);
    });
    anim.timingFunctions = KFVectorLayerMediaTimingFunction(xPositionAnimation.timingCurves);
    anim.fillMode = kCAFillModeBoth;
    anim.removedOnCompletion = NO;
    [anim setValue:@"x position animation" forKey:@"animationKey"];
    [_animations addObject:anim];
  }

  // When layer is initialized, and unanimated, layer renders without the first animation applied. This fixes it.
  self.position = CGPointMake([[xValues firstObject] floatValue], self.position.y);
}

- (void)_applyYPositionAnimation:(KFVectorAnimation *)yPositionAnimation
{
  NSArray *yValues = KFMapArray(yPositionAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    return @([[keyValue.keyValue firstObject] floatValue] * self->_scaleToCanvas.y);
  });
  if (yValues.count > 1) {
    CAKeyframeAnimation *anim = [CAKeyframeAnimation animationWithKeyPath:@"position.y"];
    anim.duration = self.frameCount * 1.0 / self.frameRate;
    anim.repeatCount = self.repeatCount;
    anim.values = yValues;
    anim.keyTimes = KFMapArray(yPositionAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyFrame) {
      return @(keyFrame.startFrame * 1.0 / self.frameCount);
    });
    anim.timingFunctions = KFVectorLayerMediaTimingFunction(yPositionAnimation.timingCurves);
    anim.fillMode = kCAFillModeBoth;
    anim.removedOnCompletion = NO;
    [anim setValue:@"y position animation" forKey:@"animationKey"];
    [_animations addObject:anim];
  }

  // When layer is initialized, and unanimated, layer renders without the first animation applied. This fixes it.
  self.position = CGPointMake(self.position.x, [[yValues firstObject] floatValue]);
}

- (void)_applyAnchorPointAnimation:(KFVectorAnimation *)anchorPointAnimation
{
  NSArray *anchorPointValues = KFMapArray(anchorPointAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    NSArray *value = keyValue.keyValue;
    CGPoint anchor = CGPointMake([value[0] floatValue] * self->_scaleToLayer.x, [value[1] floatValue] * self->_scaleToLayer.y);
    return [NSValue valueWithCGPoint:CGPointMake(anchor.x / CGRectGetWidth(self.bounds), anchor.y / CGRectGetHeight(self.bounds))];
  });
  if (anchorPointValues.count > 1) {
    CAKeyframeAnimation *anim = [CAKeyframeAnimation animationWithKeyPath:@"anchorPoint"];
    anim.duration = self.frameCount * 1.0 / self.frameRate;
    anim.values = anchorPointValues;
    anim.repeatCount = self.repeatCount;
    anim.keyTimes = KFMapArray(anchorPointAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyFrame) {
      return @(keyFrame.startFrame * 1.0 / self.frameCount);
    });
    anim.timingFunctions = KFVectorLayerMediaTimingFunction(anchorPointAnimation.timingCurves);
    anim.fillMode = kCAFillModeBoth;
    anim.removedOnCompletion = NO;
    [anim setValue:@"anchor point animation" forKey:@"animationKey"];
    [_animations addObject:anim];
  }

  // When layer is initialized, and unanimated, layer renders without the first animation applied. This fixes it.
  self.anchorPoint = [[anchorPointValues firstObject] CGPointValue];
}

- (void)_applyRotationAnimation:(KFVectorAnimation *)rotationAnimation
{
  NSArray *rotationValues = KFMapArray(rotationAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    return @([[keyValue.keyValue firstObject] floatValue] * M_PI / 180);
  });
  if (rotationValues.count > 1) {
    // Rotate along the key value, for our custom CGFloat property.
    CAKeyframeAnimation *anim = [CAKeyframeAnimation animationWithKeyPath:@"transform.rotation"];
    anim.duration = self.frameCount * 1.0 / self.frameRate;
    anim.values = rotationValues;
    anim.repeatCount = self.repeatCount;
    anim.keyTimes = KFMapArray(rotationAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyFrame) {
      return @(keyFrame.startFrame * 1.0 / self.frameCount);
    });
    anim.timingFunctions = KFVectorLayerMediaTimingFunction(rotationAnimation.timingCurves);
    anim.fillMode = kCAFillModeBoth;
    anim.removedOnCompletion = NO;
    [anim setValue:@"rotation animation" forKey:@"animationKey"];
    [_animations addObject:anim];
  }

  // When layer is initialized, and unanimated, layer renders without the first animation applied. This fixes it.
  self.transform = CATransform3DRotate(self.transform,
                                       [[rotationValues firstObject] floatValue],
                                       0.0,
                                       0.0,
                                       1.0);
}

- (void)_applyOpacityAnimation:(KFVectorAnimation *)opacityAnimation
{
  NSArray *opacityValues = KFMapArray(opacityAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    return @([[keyValue.keyValue firstObject] floatValue] / 100.0);
  });
  if (opacityValues.count > 1) {
    // Rotate along the key value, for our custom CGFloat property.
    CAKeyframeAnimation *anim = [CAKeyframeAnimation animationWithKeyPath:@"opacity"];
    anim.duration = self.frameCount * 1.0 / self.frameRate;
    anim.values = opacityValues;
    anim.repeatCount = self.repeatCount;
    anim.keyTimes = KFMapArray(opacityAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyFrame) {
      return @(keyFrame.startFrame * 1.0 / self.frameCount);
    });
    anim.timingFunctions = KFVectorLayerMediaTimingFunction(opacityAnimation.timingCurves);
    anim.fillMode = kCAFillModeBoth;
    anim.removedOnCompletion = NO;
    [anim setValue:@"opacity animation" forKey:@"animationKey"];
    [_animations addObject:anim];
  }

  // When layer is initialized, and unanimated, layer renders without the first animation applied. This fixes it.
  self.opacity = [[opacityValues firstObject] floatValue];
}

- (void)_applyStrokeWidthAnimation:(KFVectorAnimation *)strokeWidthAnimation
{
  NSArray *strokeWidthValues = KFMapArray(strokeWidthAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    return @([[keyValue.keyValue firstObject] floatValue] * self->_scaleToCanvas.x);
  });
  if (strokeWidthValues.count > 1) {
    CAKeyframeAnimation *anim = [CAKeyframeAnimation animationWithKeyPath:@"lineWidth"];
    anim.duration = self.frameCount * 1.0 / self.frameRate;
    anim.values = strokeWidthValues;
    anim.repeatCount = self.repeatCount;
    anim.keyTimes = KFMapArray(strokeWidthAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyFrame) {
      return @(keyFrame.startFrame * 1.0 / self.frameCount);
    });
    anim.timingFunctions = KFVectorLayerMediaTimingFunction(strokeWidthAnimation.timingCurves);
    anim.fillMode = kCAFillModeBoth;
    anim.removedOnCompletion = NO;
    [anim setValue:@"stroke width animation" forKey:@"animationKey"];
    [_animations addObject:anim];
  }

  // When layer is initialized, and unanimated, layer renders without the first animation applied. This fixes it.
  self.lineWidth = [[strokeWidthValues firstObject] floatValue];
}

@end
