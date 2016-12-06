/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import <UIKit/UIKit.h>

#import "KFUtilities.h"

@class KFVector;

KF_EXTERN_C_BEGIN

KFVector *KFVectorFromDictionary(NSDictionary *faceDictionary);
KFVector *KFVectorFromDictionaryInRange(NSDictionary *faceDictionary, NSUInteger fromFrame, NSUInteger toFrame);

KF_EXTERN_C_END
