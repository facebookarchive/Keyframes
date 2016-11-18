/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

#import <UIKit/UIKit.h>

#import <keyframes/KFVectorAnimationLayer.h>
#import <keyframes/KFVectorFeatureLayerInterface.h>

/**
 * @discussion Use this class to draw each feature of the face.
 *
 * Animates the keyframes of KFVectorFeature.
 */
@interface KFVectorGradientFeatureLayer : KFVectorAnimationLayer <KFVectorFeatureLayerInterface>

@end
