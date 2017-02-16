/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "Compatibility.h"

#import <keyframes/KFUtilities.h>
#import "KFVectorAnimation.h"

KF_EXTERN_C_BEGIN

/// Helper method to build media timing functions from value graph.
NSArray<CAMediaTimingFunction *> *KFVectorLayerMediaTimingFunction(NSArray<NSArray *> *timingCurves);

/// Interpolate the y value with given x value.
CGFloat KFVectorTimingFunctionValueAtTime(KFVectorAnimation *animation, CGFloat time, NSUInteger frameRate);

KF_EXTERN_C_END
