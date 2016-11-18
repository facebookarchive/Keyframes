/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
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
