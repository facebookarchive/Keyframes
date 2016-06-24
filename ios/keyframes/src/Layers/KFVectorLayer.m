// Copyright 2004-present Facebook. All Rights Reserved.

#import "KFVectorLayer.h"

#import "KFUtilities.h"
#import "KFVector.h"
#import "KFVectorAnimation.h"
#import "KFVectorAnimationGroup.h"
#import "KFVectorAnimationLayer.h"
#import "KFVectorFeature.h"
#import "KFVectorFeatureLayer.h"
#import "KFVectorGradientFeatureLayer.h"

@implementation KFVectorLayer {
  CALayer *_containerLayer;
}

- (void)setFaceModel:(KFVector *)faceModel
{
  NSAssert(_faceModel == nil, @"Do not call this method multiple times.");
  NSAssert(self.bounds.size.width > 0 && self.bounds.size.height > 0, @"Ensure layer has > 0 size.");

  _faceModel = faceModel;
  _containerLayer = [CALayer layer];
  _containerLayer.frame = self.bounds;
  self.speed = 0.0;
  [self addSublayer:_containerLayer];
  [self _setupFace:_faceModel];
}

- (void)_setupFace:(KFVector *)reactionVectorFace
{
  self.name = reactionVectorFace.name;

  // Feature layers are in one array, and animation groups are in another array.
  // Feature arrays can be nested within animation groups (as a sublayer).
  // 1) we need to create the groups,
  // 2) create feature layers.
  // 3) Add root level feature layers.
  // 4) Add animation groups to self.layer according to their dependency graph
  // 5) Add leaf level feature layers into appropriate animation groups.

  // 1) we need to create the groups,
  NSMutableDictionary<NSNumber *, KFVectorAnimationLayer *> *groupIdToLayerMap = [NSMutableDictionary dictionary];
  [reactionVectorFace.animationGroups enumerateObjectsUsingBlock:^(KFVectorAnimationGroup *animationGroup,
                                                                   NSUInteger idx,
                                                                   BOOL *stop) {
    KFVectorAnimationLayer *animationGroupLayer = [KFVectorAnimationLayer layer];
    animationGroupLayer.frame = self.bounds;
    animationGroupLayer.name = animationGroup.groupName;
    groupIdToLayerMap[@(animationGroup.groupId)] = animationGroupLayer;
  }];

  // 2) create feature layers.
  // 3) Add root level feature layers.
  NSArray<CAShapeLayer *> *featureLayers = KFMapArrayWithIndex(reactionVectorFace.features, ^id(KFVectorFeature *feature, NSUInteger index)
                                                               {
    CAShapeLayer<KFVectorFeatureLayerInterface> *featureLayer;

    if (feature.gradientEffect) {
      featureLayer = [KFVectorGradientFeatureLayer layer];
    } else {
      featureLayer = [KFVectorFeatureLayer layer];
    }

    featureLayer.frame = self.bounds;
    featureLayer.name = feature.name;
    [featureLayer setFeature:feature canvasSize:reactionVectorFace.canvasSize];

    CALayer *animatedFeatureLayer = featureLayer;
    if (feature.featureAnimations.count) {
      KFVectorAnimationLayer *animationLayer = [KFVectorAnimationLayer layer];
      animationLayer.frame = self.bounds;
      animationLayer.name = featureLayer.name;
      [animationLayer addSublayer:featureLayer];
      [animationLayer setAnimations:feature.featureAnimations canvasSize:reactionVectorFace.canvasSize];
      animatedFeatureLayer = animationLayer;
    }

    if (feature.animationGroupId == NSNotFound) {
      [self->_containerLayer addSublayer:animatedFeatureLayer];
    }
    return animatedFeatureLayer;
  });

  // 4) Add animation groups to self according to their dependency graph
  [reactionVectorFace.animationGroups enumerateObjectsUsingBlock:^(KFVectorAnimationGroup *animationGroup,
                                                                    NSUInteger idx,
                                                                    BOOL *stop) {
    CALayer *animationGroupLayer = groupIdToLayerMap[@(animationGroup.groupId)];
    if (groupIdToLayerMap[@(animationGroup.parentGroupId)]) {
      CALayer *parentGroupLayer = groupIdToLayerMap[@(animationGroup.parentGroupId)];
      [parentGroupLayer addSublayer:animationGroupLayer];
    } else {
      // parent is nil. add to the root layer
      [self->_containerLayer addSublayer:animationGroupLayer];
    }
  }];

  // 5) Add leaf level feature layers into appropriate animation groups.
  [featureLayers enumerateObjectsUsingBlock:^(CAShapeLayer *featureLayer, NSUInteger idx, BOOL *stop) {
    KFVectorFeature *feature = reactionVectorFace.features[idx];
    if (feature.animationGroupId != NSNotFound) {
      KFVectorAnimationLayer *animationGroupLayer = groupIdToLayerMap[@(feature.animationGroupId)];
      [animationGroupLayer addSublayer:featureLayer];
    }
  }];

  [reactionVectorFace.animationGroups enumerateObjectsUsingBlock:^(KFVectorAnimationGroup *animationGroup, NSUInteger idx, BOOL *stop) {
    // Apply all animation to animation layer for now
    KFVectorAnimationLayer *animationGroupLayer = groupIdToLayerMap[@(animationGroup.groupId)];
    [animationGroupLayer setAnimations:animationGroup.animations canvasSize:reactionVectorFace.canvasSize];
  }];
}

- (void)startAnimation
{
  if (self.speed > 0) {
    return;
  }

  CFTimeInterval pausedTime = [self timeOffset];
  self.speed = 1.0;
  self.timeOffset = 0.0;
  self.beginTime = 0.0;
  CFTimeInterval timeSincePause = [self convertTime:CACurrentMediaTime() fromLayer:nil] - pausedTime;
  self.beginTime = timeSincePause;
}

- (void)pauseAnimation
{
  CFTimeInterval pausedTime = [self convertTime:CACurrentMediaTime() fromLayer:nil];
  self.speed = 0.0;
  self.timeOffset = pausedTime;
}

- (void)layoutSublayers
{
  [super layoutSublayers];
  [CATransaction begin];
  [CATransaction setValue:(id)kCFBooleanTrue
                   forKey:kCATransactionDisableActions];
  _containerLayer.transform = CATransform3DMakeScale(CGRectGetWidth(self.bounds) / CGRectGetWidth(_containerLayer.bounds),
                                                     CGRectGetHeight(self.bounds) / CGRectGetHeight(_containerLayer.bounds),
                                                     1.0);
  _containerLayer.position = CGPointMake(CGRectGetMidX(self.bounds), CGRectGetMidY(self.bounds));
  [CATransaction commit];
}

@end
