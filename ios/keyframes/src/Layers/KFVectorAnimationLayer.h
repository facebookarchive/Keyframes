// Copyright 2004-present Facebook. All Rights Reserved.

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
