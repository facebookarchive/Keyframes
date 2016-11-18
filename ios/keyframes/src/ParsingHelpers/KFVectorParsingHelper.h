/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

#import <UIKit/UIKit.h>

#import <keyframes/KFUtilities.h>

@class KFVector;

KF_EXTERN_C_BEGIN

KFVector *KFVectorFromDictionary(NSDictionary *faceDictionary);
KFVector *KFVectorFromDictionaryInRange(NSDictionary *faceDictionary, NSUInteger fromFrame, NSUInteger toFrame);

KF_EXTERN_C_END
