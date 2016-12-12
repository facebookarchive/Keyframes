/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "Compatibility.h"

@class KFVector;

#if TARGET_OS_OSX
    @interface KFView: NSView
    @end
#else
    @interface KFView: UIView
    @end
#endif

@interface KFVectorView : KFView

- (instancetype)initWithFrame:(CGRect)frame faceVector:(KFVector *)faceVector NS_DESIGNATED_INITIALIZER;

- (instancetype)init __attribute__((unavailable("Must use designated initializer")));

- (instancetype)initWithFrame:(CGRect)frame __attribute__((unavailable("Must use designated initializer")));

- (instancetype)initWithCoder:(NSCoder *)aDecoder __attribute__((unavailable("Must use designated initializer")));

@end
