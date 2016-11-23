/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 */

@class KFVectorFeature;

@protocol KFVectorFeatureLayerInterface

/**
 * Setup the face with a feature, and initialize to frame 0.
 */
- (void)setFeature:(KFVectorFeature *)feature canvasSize:(CGSize)canvasSize;

@end
