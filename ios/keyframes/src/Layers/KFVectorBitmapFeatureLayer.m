/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
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
