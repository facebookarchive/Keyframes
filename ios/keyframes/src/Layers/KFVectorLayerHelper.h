/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

#import <UIKit/UIKit.h>

#import <keyframes/KFUtilities.h>

KF_EXTERN_C_BEGIN

/// Helper method to build media timing functions from value graph.
NSArray<CAMediaTimingFunction *> *KFVectorLayerMediaTimingFunction(NSArray<NSArray *> *timingCurves);

KF_EXTERN_C_END
