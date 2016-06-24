// Copyright 2004-present Facebook. All Rights Reserved.

#import "KFVectorAnimationLayer.h"

#import "KFUtilities.h"
#import "KFVectorAnimation.h"
#import "KFVectorAnimationKeyValue.h"
#import "KFVectorLayerHelper.h"

@implementation KFVectorAnimationLayer
{
  CGSize _canvasSize;
}

- (void)setAnimations:(NSArray<KFVectorAnimation *> *)animations canvasSize:(CGSize)canvasSize;
{
  _canvasSize = canvasSize;

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

  for (KFVectorAnimation *animation in animations) {
    if ([animation.property isEqualToString:@"STROKE_WIDTH"]) {
      [self _applyStrokeWidthAnimation:animation];
    } else {
      if ([animation.property isEqualToString:@"SCALE"]) {
        [self _applyScaleAnimation:animation];
      } else if ([animation.property isEqualToString:@"ROTATION"]) {
        [self _applyRotationAnimation:animation];
      } else if ([animation.property isEqualToString:@"POSITION"]) {
        [self _applyPositionAnimation:animation];
      }
    }
  }
}

- (void)_applyScaleAnimation:(KFVectorAnimation *)scaleAnimation
{
  // Animate along the key frames, for our custom CGPoint property.
  CFTimeInterval duration = scaleAnimation.animationFrameCount * 1.0 / scaleAnimation.frameRate;
  float repeatCount = HUGE_VALF;
  NSArray *xValues = KFMapArray(scaleAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    NSArray *value = keyValue.keyValue;
    return @([value[0] floatValue] / 100.0);
  });
  NSArray *yValues = KFMapArray(scaleAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    NSArray *value = keyValue.keyValue;
    return @([value[1] floatValue] / 100.0);
  });

  NSArray *keyTimes = KFMapArray(scaleAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyFrame) {
    return @(keyFrame.startFrame * 1.0 / scaleAnimation.animationFrameCount);
  });
  NSArray *timingFunctions = KFVectorLayerMediaTimingFunction(scaleAnimation.timingCurves);

  CAKeyframeAnimation *scaleXAnim = [CAKeyframeAnimation animationWithKeyPath:@"transform.scale.x"];
  scaleXAnim.duration = duration;
  scaleXAnim.repeatCount = repeatCount;
  scaleXAnim.values = xValues;
  scaleXAnim.keyTimes = keyTimes;
  scaleXAnim.timingFunctions = timingFunctions;
  [self addAnimation:scaleXAnim forKey:@"scale x animation"];

  CAKeyframeAnimation *scaleYAnim = [CAKeyframeAnimation animationWithKeyPath:@"transform.scale.y"];
  scaleYAnim.duration = duration;
  scaleYAnim.repeatCount = repeatCount;
  scaleYAnim.values = yValues;
  scaleYAnim.keyTimes = keyTimes;
  scaleYAnim.timingFunctions = timingFunctions;
  [self addAnimation:scaleYAnim forKey:@"scale y animation"];
}

- (void)_applyPositionAnimation:(KFVectorAnimation *)positionAnimation
{
  NSArray *firstPosition = [[positionAnimation.keyValues firstObject] keyValue];
  CGPoint firstPoint = _pointFromAnimationCoordinateToLayer(CGPointMake([firstPosition[0] floatValue], [firstPosition[1] floatValue]), _canvasSize, self.bounds.size);
  CGPoint offsetFromAnchor = CGPointMake(self.position.x - firstPoint.x,
                                         self.position.y - firstPoint.y);

  CAKeyframeAnimation *anim = [CAKeyframeAnimation animationWithKeyPath:@"position"];
  anim.duration = positionAnimation.animationFrameCount * 1.0 / positionAnimation.frameRate;
  anim.repeatCount = HUGE_VALF;
  anim.values = KFMapArray(positionAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    NSArray *value = keyValue.keyValue;
    CGPoint position = _pointFromAnimationCoordinateToLayer(CGPointMake([value[0] floatValue], [value[1] floatValue]), self->_canvasSize, self.bounds.size);
    return [NSValue valueWithCGPoint:CGPointMake(position.x + offsetFromAnchor.x, position.y + offsetFromAnchor.y)];
  });
  anim.keyTimes = KFMapArray(positionAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyFrame) {
    return @(keyFrame.startFrame * 1.0 / positionAnimation.animationFrameCount);
  });
  anim.timingFunctions = KFVectorLayerMediaTimingFunction(positionAnimation.timingCurves);
  [self addAnimation:anim forKey:@"position animation"];
}

- (void)_applyRotationAnimation:(KFVectorAnimation *)rotationAnimation
{
  // Rotate along the key value, for our custom CGFloat property.
  CAKeyframeAnimation *anim = [CAKeyframeAnimation animationWithKeyPath:@"transform.rotation"];
  anim.duration = rotationAnimation.animationFrameCount * 1.0 / rotationAnimation.frameRate;
  anim.repeatCount = HUGE_VALF;
  anim.values = KFMapArray(rotationAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    return @([[keyValue.keyValue firstObject] floatValue] * M_PI/180);
  });
  anim.keyTimes = KFMapArray(rotationAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyFrame) {
    return @(keyFrame.startFrame * 1.0 / rotationAnimation.animationFrameCount);
  });
  anim.timingFunctions = KFVectorLayerMediaTimingFunction(rotationAnimation.timingCurves);
  [self addAnimation:anim forKey:@"rotation animation"];
}

- (void)_applyStrokeWidthAnimation:(KFVectorAnimation *)strokeWidthAnimation
{
  CAKeyframeAnimation *anim = [CAKeyframeAnimation animationWithKeyPath:@"lineWidth"];
  anim.duration = strokeWidthAnimation.animationFrameCount * 1.0 / strokeWidthAnimation.frameRate;
  anim.repeatCount = HUGE_VALF;
  anim.values = KFMapArray(strokeWidthAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyValue) {
    return @([[keyValue.keyValue firstObject] floatValue] * self.bounds.size.width / self->_canvasSize.width);
  });
  anim.keyTimes = KFMapArray(strokeWidthAnimation.keyValues, ^id(KFVectorAnimationKeyValue *keyFrame) {
    return @(keyFrame.startFrame * 1.0 / strokeWidthAnimation.animationFrameCount);
  });
  anim.timingFunctions = KFVectorLayerMediaTimingFunction(strokeWidthAnimation.timingCurves);
  [self addAnimation:anim forKey:@"stroke width animation"];
}

#pragma mark - Helpers

static CGPoint _pointFromAnimationCoordinateToLayer(CGPoint point, CGSize canvasSize, CGSize layerSize)
{
  return CGPointMake(point.x * layerSize.width / canvasSize.width, point.y * layerSize.height / canvasSize.height);
}

@end
