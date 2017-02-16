/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "CAMediaTimingFunction+KFVectorLayer.h"

// based on https://gist.github.com/raphaelschaad/6739676
@implementation CAMediaTimingFunction (KFVectorLayer)

- (CGFloat)valueAtX:(CGFloat)x inDuration:(CGFloat)duration
{
  float vec[4] = {0.};

  [self getControlPointAtIndex:1 values:&vec[0]];
  [self getControlPointAtIndex:2 values:&vec[2]];
  CGPoint p1 = CGPointMake(vec[0], vec[1]);
  CGPoint p2 = CGPointMake(vec[2], vec[3]);
  
  CGFloat cx = 3.0 * p1.x;
  CGFloat bx = 3.0 * (p2.x - p1.x) - cx;
  CGFloat ax = 1.0 - cx - bx;

  CGFloat cy = 3.0 * p1.y;
  CGFloat by = 3.0 * (p2.y - p1.y) - cy;
  CGFloat ay = 1.0 - cy - by;

  CGFloat epsilon = 1.0 / (200 * duration);
  CGFloat xSolved = [self solveCurveX:x epsilon:epsilon ax:ax bx:bx cx:cx];
  CGFloat y = [self sampleCurveY:xSolved ay:ay by:by cy:cy];
  return y;
}

#pragma mark - Beizer Maths

// Cubic Bezier math code is based on WebCore (WebKit)
// http://opensource.apple.com/source/WebCore/WebCore-955.66/platform/graphics/UnitBezier.h
// http://opensource.apple.com/source/WebCore/WebCore-955.66/page/animation/AnimationBase.cpp

/*
 * Copyright (C) 2007, 2008, 2009 Apple Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1.  Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 * 2.  Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 * 3.  Neither the name of Apple Computer, Inc. ("Apple") nor the names of
 *     its contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY APPLE AND ITS CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL APPLE OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

- (void)calculatePolynomialCoefficients
{
}


- (CGFloat)sampleCurveX:(CGFloat)t
                     ax:(CGFloat)ax bx:(CGFloat)bx cx:(CGFloat)cx
{
  return ((ax * t + bx) * t + cx) * t;
}

- (CGFloat)sampleCurveY:(CGFloat)t
                     ay:(CGFloat)ay by:(CGFloat)by cy:(CGFloat)cy
{
  return ((ay * t + by) * t + cy) * t;
}


- (CGFloat)sampleCurveDerivativeX:(CGFloat)t
                               ax:(CGFloat)ax bx:(CGFloat)bx cx:(CGFloat)cx
{
  return (3.0 * ax * t + 2.0 * bx) * t + cx;
}


// Given an x value, find a parametric value it came from.
- (CGFloat)solveCurveX:(CGFloat)x epsilon:(CGFloat)epsilon
                    ax:(CGFloat)ax bx:(CGFloat)bx cx:(CGFloat)cx
{
  CGFloat t0;
  CGFloat t1;
  CGFloat t2;
  CGFloat x2;
  CGFloat d2;
  NSUInteger i;

  // First try a few iterations of Newton's method -- normally very fast.
  for (t2 = x, i = 0; i < 8; i++) {
    x2 = [self sampleCurveX:t2 ax:ax bx:bx cx:cx] - x;
    if (fabs(x2) < epsilon) {
      return t2;
    }
    d2 = [self sampleCurveDerivativeX:t2 ax:ax bx:bx cx:cx];
    if (fabs(d2) < 1e-6) {
      break;
    }
    t2 = t2 - x2 / d2;
  }

  // Fall back to the bisection method for reliability.
  t0 = 0.0;
  t1 = 1.0;
  t2 = x;

  if (t2 < t0) {
    return t0;
  }
  if (t2 > t1) {
    return t1;
  }

  while (t0 < t1) {
    x2 = [self sampleCurveX:t2 ax:ax bx:bx cx:cx];
    if (fabs(x2 - x) < epsilon) {
      return t2;
    }
    if (x > x2) {
      t0 = t2;
    } else {
      t1 = t2;
    }
    t2 = (t1 - t0) * 0.5 + t0;
  }

  // Failure.
  return t2;
}


@end
