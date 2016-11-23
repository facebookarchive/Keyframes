/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import <UIKit/UIKit.h>

@class KFVectorAnimation;
@class KFVectorFeature;

/**
 * @discussion Use this class to apply layer animation
 *
 * Animates the keyframes of KFVectorAnimation.
 */
@interface KFVectorAnimationLayer : CAShapeLayer

@property (nonatomic, assign, readwrite) NSUInteger frameRate;
@property (nonatomic, assign, readwrite) NSUInteger frameCount;
@property (nonatomic, copy, readwrite) NSString *formatVersion;

/// Setting KFVectorAnimation will setup the layer at frame 0 with animation specified.
- (void)setAnimations:(NSArray<KFVectorAnimation *> *)animations
        scaleToCanvas:(CGPoint)scaleToCanvas
         scaleToLayer:(CGPoint)scaleToLayer;

/// Sets the lifespan of the layer from certain frame to certain frame.
- (void)setLifespanFromFrame:(NSUInteger)fromFrame toFrom:(NSUInteger)toFrame;

/// Reset animations to the beginning.
- (void)resetAnimations;

@end
