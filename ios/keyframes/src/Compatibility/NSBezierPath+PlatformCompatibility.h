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

#import <Cocoa/Cocoa.h>

@interface NSBezierPath (PlatformCompatibility)

@property (readonly) CGPathRef CGPath;

- (void)addLineToPoint:(NSPoint)point;
- (void)addCurveToPoint:(NSPoint)point controlPoint1:(NSPoint)controlPoint1 controlPoint2:(NSPoint)controlPoint2;
- (void)addQuadCurveToPoint:(NSPoint)point controlPoint:(NSPoint)controlPoint;

@end

#endif
