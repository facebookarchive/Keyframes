/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "NSImage+PlatformCompatibility.h"

#if TARGET_OS_OSX

@implementation NSImage (PlatformCompatibility)

- (CGImageRef)CGImage
{
  CGImageSourceRef source = CGImageSourceCreateWithData((CFDataRef)[self TIFFRepresentation], NULL);

  return CGImageSourceCreateImageAtIndex(source, 0, NULL);
}

+ (NSImage *)imageWithData:(NSData *)data
{
  return [[NSImage alloc] initWithData:data];
}

@end

#endif
