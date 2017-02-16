/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "UIBezierPath+KFVectorLayer.h"

typedef void(^KFBezierPathEnumerationBlock)(const CGPathElement *element);
typedef struct KFBezierPathSubpath {
  CGPathElementType type;
  CGFloat length;
  CGPoint startPoint;
  CGPoint endPoint;
  CGPoint controlPoint1;
  CGPoint controlPoint2;
} KFBezierPathSubpath;

@implementation UIBezierPath (KFVectorLayer)

- (UIBezierPath *)pathTrimFrom:(CGFloat)start to:(CGFloat)end
{
  NSAssert(0.0 <= start && start <= 1.0, @"start should be from 0.0 to 1.0");
  NSAssert(0.0 <= end && end <= 1.0, @"end should be from 0.0 to 1.0");

  NSUInteger subpathCount = [self _subpathsCount];
  KFBezierPathSubpath subpaths[subpathCount];
  [self _extractAllSubpaths:subpaths withLength:YES];

  CGFloat length = 0.0f;
  for (NSUInteger i = 0; i < subpathCount; ++i) {
    length += subpaths[i].length;
  }

  NSUInteger startTargetSubpathIndex;
  CGFloat startTargetProgress;
  findTargetSubpath(start, subpaths, subpathCount,
                    &startTargetSubpathIndex, &startTargetProgress);

  NSUInteger endTargetSubpathIndex;
  CGFloat endTargetProgress;
  findTargetSubpath(end, subpaths, subpathCount,
                    &endTargetSubpathIndex, &endTargetProgress);

  UIBezierPath *trimmedPath = [UIBezierPath bezierPath];
  [trimmedPath moveToPoint:subpaths[0].startPoint];

  if (startTargetSubpathIndex == endTargetSubpathIndex) {
    // trim on same subpath
    [self _trimSubpath:&subpaths[endTargetSubpathIndex]
                  from:startTargetProgress
                    to:endTargetProgress
          appendToPath:trimmedPath];
  } else {
    // trim start path
    [self _trimSubpath:&subpaths[startTargetSubpathIndex]
                  from:startTargetProgress
                    to:1.0
          appendToPath:trimmedPath];

    // connect all parts between
    for (NSUInteger i = startTargetSubpathIndex + 1; i < endTargetSubpathIndex; ++i) {
      switch (subpaths[i].type) {
        case kCGPathElementMoveToPoint:
          [trimmedPath moveToPoint:subpaths[i].endPoint];
          break;
        case kCGPathElementAddLineToPoint:
          [trimmedPath addLineToPoint:subpaths[i].endPoint];
          break;
        case kCGPathElementAddQuadCurveToPoint:
          [trimmedPath addQuadCurveToPoint:subpaths[i].endPoint
                              controlPoint:subpaths[i].controlPoint1];
          break;
        case kCGPathElementAddCurveToPoint:
          [trimmedPath addCurveToPoint:subpaths[i].endPoint
                         controlPoint1:subpaths[i].controlPoint1
                         controlPoint2:subpaths[i].controlPoint2];
          break;
        case kCGPathElementCloseSubpath:
          [trimmedPath closePath];
          break;
      }
    }
    
    // trim end path
    [self _trimSubpath:&subpaths[endTargetSubpathIndex]
                  from:0.0
                    to:endTargetProgress
          appendToPath:trimmedPath];
  }

  return trimmedPath;
}

#pragma mark - private

static void findTargetSubpath(CGFloat progress, KFBezierPathSubpath *subpaths, NSUInteger count,
                              NSUInteger *targetSubpathIndex, CGFloat *targetProgress)
{
  CGFloat length = 0.0;
  for (NSUInteger i = 0; i < count; ++i) {
    length += subpaths[i].length;
  }

  CGFloat targetLength = length * progress;
  CGFloat currentLength = 0;
  for (NSUInteger i = 0; i < count; ++i) {
    if (currentLength + subpaths[i].length >= targetLength) {
      *targetSubpathIndex = i;
      break;
    } else {
      currentLength += subpaths[i].length;
    }
  }

  CGFloat lengthInSubpath = targetLength - currentLength;
  if (subpaths[*targetSubpathIndex].length == 0) {
    *targetProgress = 1.0;
  } else {
    *targetProgress = lengthInSubpath / subpaths[*targetSubpathIndex].length;
  }
}

static void subpathEnumerationCallback(void *info, const CGPathElement *element)
{
  KFBezierPathEnumerationBlock block = (__bridge KFBezierPathEnumerationBlock)info;
  if (block) {
    block(element);
  }
}

- (void)_enumerateElementsUsingBlock:(KFBezierPathEnumerationBlock)block
{
  CGPathApply(self.CGPath, (__bridge void *)block, subpathEnumerationCallback);
}

- (NSUInteger)_subpathsCount
{
  __block NSUInteger count = 0;
  [self _enumerateElementsUsingBlock:^(const CGPathElement *element) {
    if (element->type != kCGPathElementMoveToPoint) {
      count++;
    }
  }];
  return count;
}

- (void)_extractAllSubpaths:(KFBezierPathSubpath*)subpaths withLength:(BOOL)withLength
{
  __block CGPoint currentPoint = CGPointZero;
  __block NSUInteger i = 0;
  [self _enumerateElementsUsingBlock:^(const CGPathElement *element) {

    KFBezierPathSubpath subpath;
    subpath.type = element->type;
    subpath.startPoint = currentPoint;
    switch (element->type) {
      case kCGPathElementMoveToPoint:
        subpath.endPoint = element->points[0];
        if (withLength) {
          subpath.length = 0.0;
        }
        break;
      case kCGPathElementAddLineToPoint:
        subpath.endPoint = element->points[0];
        if (withLength) {
          subpath.length = linearLength(currentPoint, subpath.endPoint);
        }
        break;
      case kCGPathElementAddQuadCurveToPoint:
        subpath.endPoint = element->points[1];
        subpath.controlPoint1 = element->points[0];
        if (withLength) {
          subpath.length = quadraticLength(currentPoint, subpath.endPoint, subpath.controlPoint1);
        }
        break;
      case kCGPathElementAddCurveToPoint:
        subpath.endPoint = element->points[2];
        subpath.controlPoint1 = element->points[0];
        subpath.controlPoint2 = element->points[1];
        if (withLength) {
          subpath.length = cubicLength(currentPoint, subpath.endPoint, subpath.controlPoint1, subpath.controlPoint2);
        }
        break;
      case kCGPathElementCloseSubpath:
        break;
    }
    if (element->type != kCGPathElementMoveToPoint) {
      subpaths[i++] = subpath;
    }
    currentPoint = subpath.endPoint;
  }];
}

- (CGFloat)_lengthPercentageToParameter:(CGFloat)p ofSuboath:(const KFBezierPathSubpath *)path
{
  CGFloat left = 0;
  CGFloat right = 1.0;
  while (right - left > 1e-2) {
    CGFloat mid = (left + right) / 2;
    CGFloat length = [self _lengthAtT:mid ofSubpath:path];
    if (length / path->length > p) {
      right = mid;
    } else {
      left = mid;
    }
  }
  return (left + right) / 2;
}

- (CGFloat)_lengthAtT:(CGFloat)t ofSubpath:(const KFBezierPathSubpath *)subpath
{
  switch (subpath->type) {
    case kCGPathElementAddLineToPoint:
      return t;
    case kCGPathElementAddQuadCurveToPoint:
      return [self _quadraticLengthAtT:t ofSubpath:subpath];
    case kCGPathElementAddCurveToPoint:
      return [self _cubicLengthAtT:t ofSubpath:subpath];
    case kCGPathElementMoveToPoint:
    case kCGPathElementCloseSubpath:
      break;
  }
  return NAN;
}

- (CGFloat)_quadraticLengthAtT:(CGFloat)t ofSubpath:(const KFBezierPathSubpath *)subpath
{
  CGFloat u = 1.0 - t;

  CGPoint p1 = subpath->startPoint;
  CGPoint p2 = subpath->controlPoint1;
  CGPoint p3 = subpath->endPoint;

  CGPoint q1 = p1;
  CGPoint q2 = CGPointMake(
    u * p1.x + t * p2.x,
    u * p1.y + t * p2.y
  );
  CGPoint q3 = CGPointMake(
    u*u * p1.x + (u*t + t*u) * p2.x + t*t * p3.x,
    u*u * p1.y + (u*t + t*u) * p2.y + t*t * p3.y
  );

  return quadraticLength(q1, q2, q3);
}

- (CGFloat)_cubicLengthAtT:(CGFloat)t ofSubpath:(const KFBezierPathSubpath *)subpath
{
  CGFloat u = 1.0 - t;

  CGPoint p1 = subpath->startPoint;
  CGPoint p2 = subpath->controlPoint1;
  CGPoint p3 = subpath->controlPoint2;
  CGPoint p4 = subpath->endPoint;

  CGPoint q1 = p1;
  CGPoint q2 = CGPointMake(
    u * p1.x + t * p2.x,
    u * p1.y + t * p2.y
  );
  CGPoint q3 = CGPointMake(
    u*u * p1.x + (t*u + u*t) * p2.x + (t*t) * p3.x,
    u*u * p1.y + (t*u + u*t) * p2.y + (t*t) * p3.y
  );
  CGPoint q4 = CGPointMake(
    u*u*u * p1.x + (t*u*u + u*t*u + u*u*t) * p2.x + (t*t*u + u*t*t + t*u*t) * p3.x + t*t*t * p4.x,
    u*u*u * p1.y + (t*u*u + u*t*u + u*u*t) * p2.y + (t*t*u + u*t*t + t*u*t) * p3.y + t*t*t * p4.y
  );

  return cubicLength(q1, q4, q2, q3);
}

- (void)_trimSubpath:(const KFBezierPathSubpath *)subpath from:(CGFloat)start to:(CGFloat)end appendToPath:(UIBezierPath *)path
{
  CGFloat startT = [self _lengthPercentageToParameter:start ofSuboath:subpath];
  CGFloat endT = [self _lengthPercentageToParameter:end ofSuboath:subpath];
  switch (subpath->type) {
    case kCGPathElementAddLineToPoint:
      [self _trimLinearSubpath:subpath from:startT to:endT appendToPath:path];
      break;
    case kCGPathElementAddQuadCurveToPoint:
      [self _trimQuadraticSubpath:subpath from:startT to:endT appendToPath:path];
      break;
    case kCGPathElementAddCurveToPoint:
      [self _trimCubicSubpath:subpath from:startT to:endT appendToPath:path];
      break;
    case kCGPathElementMoveToPoint:
    case kCGPathElementCloseSubpath:
      break;
  }
}

- (void)_trimLinearSubpath:(const KFBezierPathSubpath *)subpath from:(CGFloat)start to:(CGFloat)end
              appendToPath:(UIBezierPath *)path
{
  // based on the formula
  //  Q1 = u0 P1 + t0 P2
  //  Q2 = u1 P1 + t1 P2

  CGFloat t0 = start;
  CGFloat t1 = end;
  CGFloat u0 = 1.0 - t0;
  CGFloat u1 = 1.0 - t1;

  CGPoint p1 = subpath->startPoint;
  CGPoint p2 = subpath->endPoint;

  CGPoint q1 = CGPointMake(
    u0 * p1.x + t0 * p2.x,
    u0 * p1.y + t0 * p2.y
  );
  CGPoint q2 = CGPointMake(
    u1 * p1.x + t1 * p2.x,
    u1 * p1.y + t1 * p2.y
  );

  if (start != 0.0) {
    [path moveToPoint:q1];
  }
  [path addLineToPoint:q2];
}

- (void)_trimQuadraticSubpath:(const KFBezierPathSubpath *)subpath from:(CGFloat)start to:(CGFloat)end
                 appendToPath:(UIBezierPath *)path
{
  // based on the formula
  //  Q1 = u0u0 P1 + (u0t0 + t0u0) P2 + t0t0 P3
  //  Q2 = u0u1 P1 + (u0t1 + t0u1) P2 + t0t1 P3
  //  Q3 = u1u1 P1 + (u1t1 + t1u1) P2 + t1t1 P3

  CGFloat t0 = start;
  CGFloat t1 = end;
  CGFloat u0 = 1.0 - t0;
  CGFloat u1 = 1.0 - t1;

  CGPoint p1 = subpath->startPoint;
  CGPoint p2 = subpath->controlPoint1;
  CGPoint p3 = subpath->endPoint;

  CGPoint q1 = CGPointMake(
    u0*u0 * p1.x + (u0*t0 + t0*u0) * p2.x + t0*t0 * p3.x,
    u0*u0 * p1.y + (u0*t0 + t0*u0) * p2.y + t0*t0 * p3.y
  );
  CGPoint q2 = CGPointMake(
    u0*u1 * p1.x + (u0*t1 + t0*u1) * p2.x + t0*t1 * p3.x,
    u0*u1 * p1.y + (u0*t1 + t0*u1) * p2.y + t0*t1 * p3.y
  );
  CGPoint q3 = CGPointMake(
    u1*u1 * p1.x + (u1*t1 + t1*u1) * p2.x + t1*t1 * p3.x,
    u1*u1 * p1.y + (u1*t1 + t1*u1) * p2.y + t1*t1 * p3.y
  );

  if (start != 0.0) {
    [path moveToPoint:q1];
  }
  [path addQuadCurveToPoint:q3 controlPoint:q2];
}

- (void)_trimCubicSubpath:(const KFBezierPathSubpath *)subpath from:(CGFloat)start to:(CGFloat)end
             appendToPath:(UIBezierPath *)path
{
  // based on the formula
  //  Q1 = u0u0u0 P1 + (t0u0u0 + u0t0u0 + u0u0t0) P2 + (t0t0u0 + u0t0t0 + t0u0t0) P3 + t0t0t0 P4
  //  Q2 = u0u0u1 P1 + (t0u0u1 + u0t0u1 + u0u0t1) P2 + (t0t0u1 + u0t0t1 + t0u0t1) P3 + t0t0t1 P4
  //  Q3 = u0u1u1 P1 + (t0u1u1 + u0t1u1 + u0u1t1) P2 + (t0t1u1 + u0t1t1 + t0u1t1) P3 + t0t1t1 P4
  //  Q4 = u1u1u1 P1 + (t1u1u1 + u1t1u1 + u1u1t1) P2 + (t1t1u1 + u1t1t1 + t1u1t1) P3 + t1t1t1 P4

  CGFloat t0 = start;
  CGFloat t1 = end;
  CGFloat u0 = 1.0 - t0;
  CGFloat u1 = 1.0 - t1;

  CGPoint p1 = subpath->startPoint;
  CGPoint p2 = subpath->controlPoint1;
  CGPoint p3 = subpath->controlPoint2;
  CGPoint p4 = subpath->endPoint;

  CGPoint q1 = CGPointMake(
    u0*u0*u0 * p1.x + (t0*u0*u0 + u0*t0*u0 + u0*u0*t0) * p2.x + (t0*t0*u0 + u0*t0*t0 + t0*u0*t0) * p3.x + t0*t0*t0 * p4.x,
    u0*u0*u0 * p1.y + (t0*u0*u0 + u0*t0*u0 + u0*u0*t0) * p2.y + (t0*t0*u0 + u0*t0*t0 + t0*u0*t0) * p3.y + t0*t0*t0 * p4.y
  );
  CGPoint q2 = CGPointMake(
    u0*u0*u1 * p1.x + (t0*u0*u1 + u0*t0*u1 + u0*u0*t1) * p2.x + (t0*t0*u1 + u0*t0*t1 + t0*u0*t1) * p3.x + t0*t0*t1 * p4.x,
    u0*u0*u1 * p1.y + (t0*u0*u1 + u0*t0*u1 + u0*u0*t1) * p2.y + (t0*t0*u1 + u0*t0*t1 + t0*u0*t1) * p3.y + t0*t0*t1 * p4.y
  );
  CGPoint q3 = CGPointMake(
    u0*u1*u1 * p1.x + (t0*u1*u1 + u0*t1*u1 + u0*u1*t1) * p2.x + (t0*t1*u1 + u0*t1*t1 + t0*u1*t1) * p3.x + t0*t1*t1 * p4.x,
    u0*u1*u1 * p1.y + (t0*u1*u1 + u0*t1*u1 + u0*u1*t1) * p2.y + (t0*t1*u1 + u0*t1*t1 + t0*u1*t1) * p3.y + t0*t1*t1 * p4.y
  );
  CGPoint q4 = CGPointMake(
    u1*u1*u1 * p1.x + (t1*u1*u1 + u1*t1*u1 + u1*u1*t1) * p2.x + (t1*t1*u1 + u1*t1*t1 + t1*u1*t1) * p3.x + t1*t1*t1 * p4.x,
    u1*u1*u1 * p1.y + (t1*u1*u1 + u1*t1*u1 + u1*u1*t1) * p2.y + (t1*t1*u1 + u1*t1*t1 + t1*u1*t1) * p3.y + t1*t1*t1 * p4.y
  );

  if (start != 0.0) {
    [path moveToPoint:q1];
  }
  [path addCurveToPoint:q4 controlPoint1:q2 controlPoint2:q3];
}

#pragma mark - Math Helpers

static CGFloat linearLength(CGPoint fromPoint, CGPoint toPoint)
{
  return sqrtf(powf(toPoint.x - fromPoint.x, 2) + powf(toPoint.y - fromPoint.y, 2));
}

static CGFloat quadraticLength(CGPoint fromPoint, CGPoint toPoint, CGPoint controlPoint)
{
  CGFloat length = 0;

  int iterations = 20;
  for (int i = 0; i < iterations; ++i) {
    float s = i * (1.0 / iterations);
    float t = s + (1.0 / iterations);

    CGPoint p = quadraticPoint(s, fromPoint, controlPoint, toPoint);
    CGPoint q = quadraticPoint(t, fromPoint, controlPoint, toPoint);

    length += linearLength(p, q);
  }

  return length;
}

static CGFloat cubicLength(CGPoint fromPoint, CGPoint toPoint, CGPoint controlPoint1, CGPoint controlPoint2)
{
  CGFloat length = 0;

  int iterations = 20;
  for (int i = 0; i < iterations; ++i) {
    float s = i * (1.0 / iterations);
    float t = s + (1.0 / iterations);

    CGPoint p = cubicPoint(s, fromPoint, controlPoint1, controlPoint2, toPoint);
    CGPoint q = cubicPoint(t, fromPoint, controlPoint1, controlPoint2, toPoint);

    length += linearLength(p, q);
  }
  return length;
}

static CGPoint quadraticPoint(float t, CGPoint start, CGPoint c1, CGPoint end)
{
  CGFloat x = quadraticBezier(t, start.x, c1.x, end.x);
  CGFloat y = quadraticBezier(t, start.y, c1.y, end.y);

  return CGPointMake(x, y);
}

static CGPoint cubicPoint(float t, CGPoint start, CGPoint c1, CGPoint c2, CGPoint end)
{
  CGFloat x = cubicBezier(t, start.x, c1.x, c2.x, end.x);
  CGFloat y = cubicBezier(t, start.y, c1.y, c2.y, end.y);

  return CGPointMake(x, y);
}

// formula base on:
// http://ericasadun.com/2013/03/25/calculating-bezier-points/
static float cubicBezier(float t, float start, float c1, float c2, float end)
{
  CGFloat t_ = (1.0 - t);
  CGFloat tt_ = t_ * t_;
  CGFloat ttt_ = t_ * t_ * t_;
  CGFloat tt = t * t;
  CGFloat ttt = t * t * t;

  return start * ttt_
  + 3.0 *  c1 * tt_ * t
  + 3.0 *  c2 * t_ * tt
  + end * ttt;
}

// formula base on:
// http://ericasadun.com/2013/03/25/calculating-bezier-points/
static float quadraticBezier(float t, float start, float c1, float end)
{
  CGFloat t_ = (1.0 - t);
  CGFloat tt_ = t_ * t_;
  CGFloat tt = t * t;

  return start * tt_
  + 2.0 *  c1 * t_ * t
  + end * tt;
}

@end
