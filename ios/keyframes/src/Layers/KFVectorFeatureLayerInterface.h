// Copyright 2004-present Facebook. All Rights Reserved.

@class KFVectorFeature;

@protocol KFVectorFeatureLayerInterface

/**
 * Setup the face with a feature, and initialize to frame 0.
 */
- (void)setFeature:(KFVectorFeature *)feature canvasSize:(CGSize)canvasSize;

@end
