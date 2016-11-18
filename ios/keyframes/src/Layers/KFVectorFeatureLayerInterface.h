/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

@class KFVectorFeature;

@protocol KFVectorFeatureLayerInterface

/**
 * Setup the face with a feature, and initialize to frame 0.
 */
- (void)setFeature:(KFVectorFeature *)feature canvasSize:(CGSize)canvasSize;

@end
