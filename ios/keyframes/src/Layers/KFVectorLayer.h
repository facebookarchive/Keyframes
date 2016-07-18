// Copyright 2004-present Facebook. All Rights Reserved.

#import <UIKit/UIKit.h>

@class KFVector;

/**
 * KFVectorFeatureLayer uses vector data to draw and animate reaction faces.
 * Since this is bezier path drawing, reaction type change
 * can be animated. Used for original single face input.
 */
@interface KFVectorLayer : CALayer

@property (strong, nonatomic) KFVector *faceModel;

/**
 * Path based face view initially starts off with frame stuck at 0.
 * Call this method to kick off animation.
 */
- (void)startAnimation;

/**
 * Call this method to pause vector animation. To resume, call startAnimation again.
 */
- (void)pauseAnimation;

/**
 * Call this method seek the animation to a given progress, progress is in range of [0, 1].
 */
- (void)seekToProgress:(CGFloat)progress;

@end
