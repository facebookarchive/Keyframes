/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import <QuartzCore/QuartzCore.h>

@class KFVector;

/**
 * KFVectorFeatureLayer uses vector data to draw and animate reaction faces.
 * Since this is bezier path drawing, reaction type change
 * can be animated. Used for original single face input.
 */
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 100000 || KEYFRAMES_CALAYER_CAANIMATIONDELEGATE_PROTOCOL
@interface KFVectorLayer : CALayer <CAAnimationDelegate>
#else
@interface KFVectorLayer : CALayer
#endif

/**
 * Setup layers based on the vector model.
 */
- (void)setFaceModel:(KFVector *)faceModel;

@property (copy, nonatomic) void (^animationDidStopBlock)(BOOL finished);

/**
 * Call this method to reset the vector animation.
 */
- (void)resetAnimations;

/**
 * Path based face view initially starts off with frame stuck at 0.
 * Call this method to kick off animation.
 */
- (void)startAnimation;

/**
 * Call this method to pause vector animation. To resume, call resumeAnimation.
 */
- (void)pauseAnimation;

/**
 * Call this method to resume vector animation.
 */
- (void)resumeAnimation;

/**
 * Call this method seek the animation to a given progress, progress is in range of [0, 1].
 */
- (void)seekToProgress:(CGFloat)progress;

@end
