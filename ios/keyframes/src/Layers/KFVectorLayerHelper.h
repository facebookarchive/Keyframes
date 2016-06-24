// Copyright 2004-present Facebook. All Rights Reserved.

#import <UIKit/UIKit.h>

#import <keyframes/KFUtilities.h>

KF_EXTERN_C_BEGIN

/// Helper method to build media timing functions from value graph.
NSArray<CAMediaTimingFunction *> *KFVectorLayerMediaTimingFunction(NSArray<NSArray *> *timingCurves);

KF_EXTERN_C_END
