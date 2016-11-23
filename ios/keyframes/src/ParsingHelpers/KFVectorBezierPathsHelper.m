/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "KFVectorBezierPathsHelper.h"

static CGPoint _pointFromNormalizedSpaceToCoordinateSize(CGPoint normalizedPoint, CGSize coordinateSize);
static CGFloat _normalizedFloatFromArrayAtIndex(NSArray *floats, NSUInteger index, CGFloat coordinateSpace);
static void _generateAndAddToPathFromACommand(UIBezierPath *path, NSString *singleCommand, CGSize canvasSize, CGSize viewSize);

#pragma mark - Public functions

UIBezierPath *KFVectorBezierPathsFromCommandList(NSArray *commandList, CGSize canvasSize, CGSize viewSize)
{
  UIBezierPath *path = [UIBezierPath bezierPath];
  for (NSString *command in commandList) {
    _generateAndAddToPathFromACommand(path, command, canvasSize, viewSize);
  }
  return path;
}

#pragma mark - Helpers

static CGPoint _pointFromNormalizedSpaceToCoordinateSize(CGPoint normalizedPoint, CGSize coordinateSize)
{
  return CGPointMake(normalizedPoint.x * coordinateSize.width, normalizedPoint.y * coordinateSize.height);
}

static CGFloat _normalizedFloatFromArrayAtIndex(NSArray *floats, NSUInteger index, CGFloat coordinateSpace)
{
  return [floats[index] floatValue] / coordinateSpace;
}

// Given a command, and a path, append proper curve to the path.
static void _generateAndAddToPathFromACommand(UIBezierPath *path, NSString *singleCommand, CGSize canvasSize, CGSize viewSize)
{
  NSString *command = [singleCommand substringToIndex:1];
  NSArray *points = [[singleCommand substringWithRange:NSMakeRange(1, [singleCommand length] - 2)] componentsSeparatedByString:@","];

  if ([command isEqualToString:@"M"]) {
    // Move with absolute coordinate
    CGPoint toPoint = CGPointMake(_normalizedFloatFromArrayAtIndex(points, 0, canvasSize.width), _normalizedFloatFromArrayAtIndex(points, 1, canvasSize.height));
    [path moveToPoint:_pointFromNormalizedSpaceToCoordinateSize(toPoint, viewSize)];
  } else if ([command isEqualToString:@"L"]) {
    // Line with absolute coordinate
    CGPoint toPoint = CGPointMake(_normalizedFloatFromArrayAtIndex(points, 0, canvasSize.width), _normalizedFloatFromArrayAtIndex(points, 1, canvasSize.height));
    [path addLineToPoint:_pointFromNormalizedSpaceToCoordinateSize(toPoint, viewSize)];
  } else if ([command isEqualToString:@"C"]) {
    // Cubic curve with absolute coordinate
    // for cubic curve, we are given 6 points. (p0,p1) is control 1, (p2,p3) is control 2, (p4, p5) is to point.
    CGPoint controlPoint1 = CGPointMake(_normalizedFloatFromArrayAtIndex(points, 0, canvasSize.width), _normalizedFloatFromArrayAtIndex(points, 1, canvasSize.height));
    CGPoint controlPoint2 = CGPointMake(_normalizedFloatFromArrayAtIndex(points, 2, canvasSize.width), _normalizedFloatFromArrayAtIndex(points, 3, canvasSize.height));
    CGPoint toPoint = CGPointMake(_normalizedFloatFromArrayAtIndex(points, 4, canvasSize.width), _normalizedFloatFromArrayAtIndex(points, 5, canvasSize.height));
    [path addCurveToPoint:_pointFromNormalizedSpaceToCoordinateSize(toPoint, viewSize)
            controlPoint1:_pointFromNormalizedSpaceToCoordinateSize(controlPoint1, viewSize)
            controlPoint2:_pointFromNormalizedSpaceToCoordinateSize(controlPoint2, viewSize)];
  } else if ([command isEqualToString:@"Q"]) {
    // Quadratic curve with absolute coordinate
    // for quad curve, we are given 4 points. (p0,p1) is control, (p2,p3) is to point.
    CGPoint controlPoint = CGPointMake(_normalizedFloatFromArrayAtIndex(points, 0, canvasSize.width), _normalizedFloatFromArrayAtIndex(points, 1, canvasSize.height));
    CGPoint toPoint = CGPointMake(_normalizedFloatFromArrayAtIndex(points, 2, canvasSize.width), _normalizedFloatFromArrayAtIndex(points, 3, canvasSize.height));
    [path addQuadCurveToPoint:_pointFromNormalizedSpaceToCoordinateSize(toPoint, viewSize)
                 controlPoint:_pointFromNormalizedSpaceToCoordinateSize(controlPoint, viewSize)];
  }
}
