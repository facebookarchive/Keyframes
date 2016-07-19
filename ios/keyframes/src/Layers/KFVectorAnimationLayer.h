/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
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

/// Setting KFVectorAnimation will setup the layer at frame 0 with animation specified.
- (void)setAnimations:(NSArray<KFVectorAnimation *> *)animations canvasSize:(CGSize)canvasSize;

@end
