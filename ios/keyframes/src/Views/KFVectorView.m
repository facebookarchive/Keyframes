/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "KFVectorView.h"

#import "KFVector.h"
#import "KFVectorLayer.h"

// We need to initialize the layer with size greater than 0, because face layer is based on CAShapeLayer, which doesn't resize properly on
// bounds change. So when we resize the bounds, we force the redraw using scale transform.

static const CGFloat kInititialFaceSize = 64;

@implementation KFVectorView

+ (Class)layerClass
{
  return [KFVectorLayer class];
}

#pragma mark - lifecycle

- (instancetype)initWithFrame:(CGRect)frame
                   faceVector:(KFVector *)faceVector
{
  if (self = [super initWithFrame:frame]) {
    // It needs to be a non-zero size so we don't divide by zero in layoutSubviews.
    CGRect arbitraryNonZeroFrame = CGRectMake(0.0,
                                              0.0,
                                              kInititialFaceSize,
                                              kInititialFaceSize);
    [self _faceLayer].frame = arbitraryNonZeroFrame;
    [self _faceLayer].faceModel = faceVector;
    [self _faceLayer].frame = frame;
  }
  return self;
}

#pragma mark - helpers

- (KFVectorLayer *)_faceLayer
{
  return (KFVectorLayer *)self.layer;
}

@end
