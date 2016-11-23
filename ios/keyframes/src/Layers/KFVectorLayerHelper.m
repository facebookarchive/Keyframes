/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "KFVectorLayerHelper.h"

NSArray<CAMediaTimingFunction *> *KFVectorLayerMediaTimingFunction(NSArray<NSArray *> *timingCurves)
{
  return KFMapArray(timingCurves, ^id(NSArray *controlPoints) {
    CGPoint controlPoint1 = [controlPoints[0] CGPointValue];
    CGPoint controlPoint2 = [controlPoints[1] CGPointValue];
    return [CAMediaTimingFunction functionWithControlPoints:controlPoint1.x :controlPoint1.y :controlPoint2.x :controlPoint2.y];
  });
}
