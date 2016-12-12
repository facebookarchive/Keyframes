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
    #import <QuartzCore/QuartzCore.h>

    #define UIBezierPath NSBezierPath
    #define UIImage NSImage
    #define UIColor NSColor

    #define CGSizeFromString NSSizeFromString
    #define NSStringFromCGSize NSStringFromSize
    #define CGPointFromString NSPointFromString
    #define NSStringFromCGPoint NSStringFromPoint

    #import "NSBezierPath+PlatformCompatibility.h"
    #import "NSImage+PlatformCompatibility.h"
    #import "NSValue+PlatformCompatibility.h"
#else
    #import <UIKit/UIKit.h>
#endif
