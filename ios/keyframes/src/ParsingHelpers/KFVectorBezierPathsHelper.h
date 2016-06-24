// Copyright 2004-present Facebook. All Rights Reserved.

#import <UIKit/UIKit.h>

#import <keyframes/KFUtilities.h>

KF_EXTERN_C_BEGIN

// Given array of command list, return UIBezierPath to use for drawing
UIBezierPath *KFVectorBezierPathsFromCommandList(NSArray *commandList, CGSize canvasSize, CGSize viewSize);

KF_EXTERN_C_END
