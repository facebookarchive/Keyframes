/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

#import <UIKit/UIKit.h>

#import <keyframes/KFUtilities.h>

KF_EXTERN_C_BEGIN

// Given array of command list, return UIBezierPath to use for drawing
UIBezierPath *KFVectorBezierPathsFromCommandList(NSArray *commandList, CGSize canvasSize, CGSize viewSize);

KF_EXTERN_C_END
