/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "KFVectorLayerHelper.h"

#import <keyframes/CAMediaTimingFunction+KFVectorLayer.h>
#import "KFVectorAnimationKeyValue.h"

NSArray<CAMediaTimingFunction *> *KFVectorLayerMediaTimingFunction(NSArray<NSArray *> *timingCurves)
{
  return KFMapArray(timingCurves, ^id(NSArray *controlPoints) {
    CGPoint controlPoint1 = [controlPoints[0] CGPointValue];
    CGPoint controlPoint2 = [controlPoints[1] CGPointValue];
    return [CAMediaTimingFunction functionWithControlPoints:controlPoint1.x :controlPoint1.y :controlPoint2.x :controlPoint2.y];
  });
}

CGFloat KFVectorTimingFunctionValueAtTime(KFVectorAnimation *animation, CGFloat time, NSUInteger frameRate)
{
  KFVectorAnimationKeyValue *firstKeyframe = [animation.keyValues firstObject];
  CGFloat lastX = firstKeyframe.startFrame * 1.0 / frameRate;;
  CGFloat lastY = [[firstKeyframe.keyValue firstObject] floatValue];

  NSArray *timingFunctions = KFVectorLayerMediaTimingFunction(animation.timingCurves);

  if (time < lastX) {
    return lastY;
  }

  for (int i = 1; i < animation.keyValues.count; ++i) {
    KFVectorAnimationKeyValue *keyframe = animation.keyValues[i];
    CGFloat x = keyframe.startFrame * 1.0 / frameRate;
    CGFloat y = [[keyframe.keyValue firstObject] floatValue];
    if (lastX <= time && time <= x) {
      CAMediaTimingFunction *timingFunction = timingFunctions[i - 1];
      CGFloat yRatio = [timingFunction valueAtX:(time - lastX) / (x - lastX) inDuration:x - lastX];
      return (y - lastY) * yRatio + lastY;
    }
    lastX = x;
    lastY = y;
  }

  KFVectorAnimationKeyValue *lastKeyframe = [animation.keyValues lastObject];
  return [[lastKeyframe.keyValue firstObject] floatValue];
}
