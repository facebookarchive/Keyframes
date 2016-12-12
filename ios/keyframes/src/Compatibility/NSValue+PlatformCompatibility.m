/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "NSValue+PlatformCompatibility.h"

#if TARGET_OS_OSX

@implementation NSValue (NSValue_PlatformCompatibility)

+ (instancetype)valueWithCGPoint:(NSPoint)point
{
    return [self valueWithPoint:point];
}

- (CGPoint)CGPointValue
{
    return [self pointValue];
}

@end

#endif
