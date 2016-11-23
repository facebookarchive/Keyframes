/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "KFVectorBitmapFeatureLayer.h"

@implementation KFVectorBitmapFeatureLayer

- (instancetype)initWithImage:(UIImage *)image
{
  if (self = [super init]) {
    self.contents = (__bridge id)image.CGImage;
  }
  return self;
}

- (void)setFeature:(KFVectorFeature *)feature canvasSize:(CGSize)canvasSize
{
  // empty
}

@end
