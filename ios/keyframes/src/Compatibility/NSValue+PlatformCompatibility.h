/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import <TargetConditionals.h>

#if TARGET_OS_OSX

#import <Foundation/Foundation.h>

@interface NSValue (NSValue_PlatformCompatibility)

@property (readonly) CGPoint CGPointValue;

+ (instancetype)valueWithCGPoint:(NSPoint)point;

@end

#endif
