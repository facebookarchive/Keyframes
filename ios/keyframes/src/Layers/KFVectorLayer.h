/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

#import <UIKit/UIKit.h>

@class KFVector;

/**
 * KFVectorFeatureLayer uses vector data to draw and animate reaction faces.
 * Since this is bezier path drawing, reaction type change
 * can be animated. Used for original single face input.
 */
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 100000
@interface KFVectorLayer : CALayer <CAAnimationDelegate>
#else
@interface KFVectorLayer : CALayer
#endif

@property (strong, nonatomic) KFVector *faceModel;
@property (copy, nonatomic) NSDictionary<NSString *, UIImage *> *imageAssets;
@property (copy, nonatomic) void (^animationDidStopBlock)(void);

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
