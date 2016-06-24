// Copyright 2004-present Facebook. All Rights Reserved.

#import "KFVectorLayerHelper.h"

NSArray<CAMediaTimingFunction *> *KFVectorLayerMediaTimingFunction(NSArray<NSArray *> *timingCurves)
{
  return KFMapArray(timingCurves, ^id(NSArray *controlPoints) {
    CGPoint controlPoint1 = [controlPoints[0] CGPointValue];
    CGPoint controlPoint2 = [controlPoints[1] CGPointValue];
    return [CAMediaTimingFunction functionWithControlPoints:controlPoint1.x :controlPoint1.y :controlPoint2.x :controlPoint2.y];
  });
}
