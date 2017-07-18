/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import <keyframes/KFUtilities.h>

@class KFVector;
@class UIImage;

KF_EXTERN_C_BEGIN

KFVector *KFVectorFromDictionary(NSDictionary *faceDictionary);
KFVector *KFVectorFromDictionaryInRange(NSDictionary *faceDictionary, NSUInteger fromFrame, NSUInteger toFrame);
KFVector *KFVectorFromBitmapReplacement(KFVector *vector, NSString *key, UIImage *bitmap);

KF_EXTERN_C_END
