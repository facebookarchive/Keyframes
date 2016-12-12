/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "NSBezierPath+PlatformCompatibility.h"

#if TARGET_OS_OSX

@implementation NSBezierPath (PlatformCompatibility)

- (void)addLineToPoint:(NSPoint)point
{
    [self lineToPoint:point];
}

- (void)addCurveToPoint:(NSPoint)point controlPoint1:(NSPoint)controlPoint1 controlPoint2:(NSPoint)controlPoint2
{
    [self curveToPoint:point controlPoint1:controlPoint1 controlPoint2:controlPoint2];
}

- (void)addQuadCurveToPoint:(NSPoint)point controlPoint:(NSPoint)controlPoint
{
    // See http://fontforge.sourceforge.net/bezier.html
    
    CGPoint QP0 = [self currentPoint];
    CGPoint CP3 = point;
    
    CGPoint CP1 = CGPointMake(
                              //  QP0   +   2   / 3    * (QP1   - QP0  )
                              QP0.x + ((2.0 / 3.0) * (controlPoint.x - QP0.x)),
                              QP0.y + ((2.0 / 3.0) * (controlPoint.y - QP0.y))
                              );
    
    CGPoint CP2 = CGPointMake(
                              //  QP2   +  2   / 3    * (QP1   - QP2)
                              point.x + (2.0 / 3.0) * (controlPoint.x - point.x),
                              point.y + (2.0 / 3.0) * (controlPoint.y - point.y)
                              );
    
    [self curveToPoint:CP3 controlPoint1:CP1 controlPoint2:CP2];
}

- (CGPathRef)CGPath
{
    NSInteger numberOfElements = [self elementCount];
    
    if (!numberOfElements) {
        return NULL;
    }
    
    CGMutablePathRef path = CGPathCreateMutable();
    NSPoint points[3];
    
    for (int i = 0; i < numberOfElements; i++) {
        switch ([self elementAtIndex:i associatedPoints:points]) {
            case NSMoveToBezierPathElement:
                CGPathMoveToPoint(path, NULL, points[0].x, points[0].y);
                break;
                
            case NSLineToBezierPathElement:
                CGPathAddLineToPoint(path, NULL, points[0].x, points[0].y);
                break;
                
            case NSCurveToBezierPathElement:
                CGPathAddCurveToPoint(path, NULL, points[0].x, points[0].y,
                                      points[1].x, points[1].y,
                                      points[2].x, points[2].y);
                break;
                
            case NSClosePathBezierPathElement:
                CGPathCloseSubpath(path);
                break;
        }
    }
    
    CGPathRef immutablePath = CGPathCreateCopy(path);
    CGPathRelease(path);
    
    return immutablePath;
}

@end

#endif
