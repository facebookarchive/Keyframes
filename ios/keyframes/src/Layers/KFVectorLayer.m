/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "KFVectorLayer.h"

#import "KFUtilities.h"
#import "KFVector.h"
#import "KFVectorAnimation.h"
#import "KFVectorAnimationGroup.h"
#import "KFVectorAnimationLayer.h"
#import "KFVectorFeature.h"
#import "KFVectorBitmapFeatureLayer.h"
#import "KFVectorFeatureLayer.h"
#import "KFVectorGradientFeatureLayer.h"

@implementation KFVectorLayer
{
  CALayer *_containerLayer;
  CABasicAnimation *_mockAnimation;
  CGFloat _duration;
}

- (instancetype)init
{
  self = [super init];
  if (self) {
    self.repeatCount = HUGE_VALF;
  }
  return self;
}

- (void)setFaceModel:(KFVector *)faceModel
{
  NSAssert(self.bounds.size.width > 0 && self.bounds.size.height > 0, @"Ensure layer has > 0 size.");

  _containerLayer = [CALayer layer];
  _containerLayer.frame = self.bounds;
  self.speed = 0.0;
  [self addSublayer:_containerLayer];
  [self _setupFace:faceModel];
}

- (void)_setupFace:(KFVector *)vector
{
  self.name = vector.name;
  _duration = vector.animationFrameCount * 1.0 / vector.frameRate;

  // Feature layers are in one array, and animation groups are in another array.
  // Feature arrays can be nested within animation groups (as a sublayer).
  // 1) we need to create the groups,
  // 2) create feature layers.
  // 3) Add root level feature layers.
  // 4) Add animation groups to self.layer according to their dependency graph
  // 5) Add leaf level feature layers into appropriate animation groups.
  // 6) Add a mock animation for invoking stop callback.

  // 1) we need to create the groups,
  NSMutableDictionary<NSNumber *, KFVectorAnimationLayer *> *groupIdToLayerMap = [NSMutableDictionary dictionary];
  [vector.animationGroups enumerateObjectsUsingBlock:^(KFVectorAnimationGroup *animationGroup,
                                                                   NSUInteger idx,
                                                                   BOOL *stop) {
    KFVectorAnimationLayer *animationGroupLayer = [KFVectorAnimationLayer layer];
    animationGroupLayer.formatVersion = vector.formatVersion;
    animationGroupLayer.name = animationGroup.groupName;
    animationGroupLayer.frameRate = vector.frameRate;
    animationGroupLayer.frameCount = vector.animationFrameCount;
    animationGroupLayer.frame = self.bounds;
    animationGroupLayer.repeatCount = self.repeatCount;
    groupIdToLayerMap[@(animationGroup.groupId)] = animationGroupLayer;
  }];

  // 2) create feature layers.
  // 3) Add root level feature layers.
  NSArray<CAShapeLayer *> *featureLayers = KFMapArrayWithIndex(vector.features, ^id(KFVectorFeature *feature, NSUInteger index)
                                                               {
    KFVectorAnimationLayer<KFVectorFeatureLayerInterface> *featureLayer;

    if (feature.backedImage) {
      NSAssert(vector.bitmaps[feature.backedImage] != nil, @"Image asset does not exist");
      featureLayer = [[KFVectorBitmapFeatureLayer alloc] initWithImage:vector.bitmaps[feature.backedImage]];
    } else if (feature.gradientEffect) {
      featureLayer = [KFVectorGradientFeatureLayer layer];
    } else {
      featureLayer = [KFVectorFeatureLayer layer];
    }
    featureLayer.formatVersion = vector.formatVersion;
    featureLayer.name = feature.name;
    featureLayer.frameRate = vector.frameRate;
    featureLayer.frameCount = vector.animationFrameCount;
    featureLayer.frame = CGRectMake(0, 0,
                                    CGRectGetWidth(self.bounds) * feature.featureSize.width / vector.canvasSize.width,
                                    CGRectGetHeight(self.bounds) * feature.featureSize.height / vector.canvasSize.height);
    featureLayer.repeatCount = self.repeatCount;
    [featureLayer setFeature:feature canvasSize:vector.canvasSize];

    if (!KFVersionLessThan(vector.formatVersion, @"1.0")) {
      [featureLayer setLifespanFromFrame:feature.fromFrame toFrom:feature.toFrame];
    }

    KFVectorAnimationLayer *animatedFeatureLayer = featureLayer;
    if (KFVersionLessThan(vector.formatVersion, @"1.0")) {
      // TO DO: for backward capability, should be deprecated
      if (feature.featureAnimations.count) {
        KFVectorAnimationLayer *animationLayer = [KFVectorAnimationLayer layer];
        animationLayer.formatVersion = vector.formatVersion;
        animationLayer.name = featureLayer.name;
        animationLayer.frameRate = vector.frameRate;
        animationLayer.frameCount = vector.animationFrameCount;
        animationLayer.frame = self.bounds;
        animationLayer.repeatCount = self.repeatCount;
        [animationLayer addSublayer:featureLayer];
        [animationLayer setAnimations:feature.featureAnimations
                        scaleToCanvas:[self _scaleFromSize:vector.canvasSize toSize:self.bounds.size]
                         scaleToLayer:[self _scaleFromSize:feature.featureSize toSize:animationLayer.bounds.size]];
        animatedFeatureLayer = animationLayer;
      }
    }

    if (!KFVersionLessThan(vector.formatVersion, @"1.0")) {
      [animatedFeatureLayer setAnimations:feature.featureAnimations
                            scaleToCanvas:[self _scaleFromSize:vector.canvasSize toSize:self.bounds.size]
                             scaleToLayer:[self _scaleFromSize:feature.featureSize toSize:animatedFeatureLayer.bounds.size]];

      if (feature.masking) {
        // handle masking
        KFVectorFeatureLayer *maskLayer = [KFVectorFeatureLayer layer];
        maskLayer.formatVersion = vector.formatVersion;
        maskLayer.name = feature.name;
        maskLayer.frameRate = vector.frameRate;
        maskLayer.frameCount = vector.animationFrameCount;
        maskLayer.frame = self.bounds;
        maskLayer.repeatCount = self.repeatCount;
        [maskLayer setFeature:feature.masking canvasSize:vector.canvasSize];
        [maskLayer setLifespanFromFrame:feature.masking.fromFrame toFrom:feature.masking.toFrame];
        [maskLayer setAnimations:feature.masking.featureAnimations
                   scaleToCanvas:[self _scaleFromSize:vector.canvasSize toSize:self.bounds.size]
                    scaleToLayer:[self _scaleFromSize:feature.masking.featureSize toSize:maskLayer.bounds.size]];

        if (feature.masking.animationGroupId == feature.featureId) {
          // masking under feature layer transforms
          animatedFeatureLayer.mask = maskLayer;
        } else {
          // masking not under feature layer transforms
          KFVectorAnimationLayer *parentLayer = [KFVectorAnimationLayer layer];
          parentLayer.formatVersion = vector.formatVersion;
          parentLayer.name = animatedFeatureLayer.name;
          parentLayer.frameRate = vector.frameRate;
          parentLayer.frameCount = vector.animationFrameCount;
          parentLayer.frame = self.bounds;
          parentLayer.repeatCount = self.repeatCount;
          parentLayer.mask = maskLayer;
          [parentLayer addSublayer:animatedFeatureLayer];
          animatedFeatureLayer = parentLayer;
        }
      }
    }

    if (feature.animationGroupId == NSNotFound) {
      [self->_containerLayer addSublayer:animatedFeatureLayer];
    }
    return animatedFeatureLayer;
  });

  // 4) Add animation groups to self according to their dependency graph
  [vector.animationGroups enumerateObjectsUsingBlock:^(KFVectorAnimationGroup *animationGroup,
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
    KFVectorFeature *feature = vector.features[idx];
    if (feature.animationGroupId != NSNotFound) {
      KFVectorAnimationLayer *animationGroupLayer = groupIdToLayerMap[@(feature.animationGroupId)];
      [animationGroupLayer addSublayer:featureLayer];
    }
  }];

  [vector.animationGroups enumerateObjectsUsingBlock:^(KFVectorAnimationGroup *animationGroup, NSUInteger idx, BOOL *stop) {
    // Apply all animation to animation layer for now
    KFVectorAnimationLayer *animationGroupLayer = groupIdToLayerMap[@(animationGroup.groupId)];
    [animationGroupLayer setAnimations:animationGroup.animations
                         scaleToCanvas:[self _scaleFromSize:vector.canvasSize toSize:self.bounds.size]
                          scaleToLayer:[self _scaleFromSize:vector.canvasSize toSize:animationGroupLayer.bounds.size]];
  }];

  // 6) Add a mock animation for invoking stop callback.
  _mockAnimation = [self _createMockAnimation];

  [self _resetAnimations];
}

- (CABasicAnimation *)_createMockAnimation
{
  CABasicAnimation *mockAnimation = [CABasicAnimation animationWithKeyPath:@"hidden"];
  mockAnimation.fromValue = @(NO);
  mockAnimation.toValue = @(NO);
  mockAnimation.duration = _duration;
  mockAnimation.repeatCount = 1;
  mockAnimation.delegate = self;
  return mockAnimation;
}

- (void)_resetAnimations
{
  self.speed = 0;
  [self removeAllAnimations];
  for (KFVectorAnimationLayer *sublayer in _containerLayer.sublayers) {
    [sublayer resetAnimations];
  }
  [self addAnimation:_mockAnimation forKey:_mockAnimation.keyPath];
}

- (void)startAnimation
{
  [self _resetAnimations];

  self.speed = 1.0;
  self.timeOffset = 0.0;
  self.beginTime = 0.0;
}

- (void)resumeAnimation
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

- (void)seekToProgress:(CGFloat)progress
{
  self.timeOffset = progress * _duration;
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

#pragma mark - NSCoding

- (instancetype)initWithCoder:(NSCoder *)coder
{
  self = [super initWithCoder:coder];
  if (self) {
    _containerLayer = [[self sublayers] firstObject];
    _mockAnimation = [coder decodeObjectForKey:@"KFVectorLayer*_mockAnimation"];
    _mockAnimation.delegate = self;
    _duration = [[coder decodeObjectForKey:@"KFVectorLayer*_duration"] doubleValue];
  }
  return self;
}

-(void)encodeWithCoder:(NSCoder *)aCoder
{
  [super encodeWithCoder:aCoder];
  [aCoder encodeObject:_mockAnimation forKey:@"KFVectorLayer*_mockAnimation"];
  [aCoder encodeObject:@(_duration) forKey:@"KFVectorLayer*_duration"];
}

#pragma mark - CAAnimationDelegate

- (void)animationDidStop:(CAAnimation *)anim finished:(BOOL)finished
{
  if (_animationDidStopBlock &&
      [anim isKindOfClass:[CABasicAnimation class]] &&
      [((CABasicAnimation *)anim).keyPath isEqualToString:_mockAnimation.keyPath]) {
    _animationDidStopBlock(finished);
  }
  if (finished) {
    // Recreating mock animation for invoking stop callback again
    _mockAnimation = [self _createMockAnimation];
    [self addAnimation:_mockAnimation forKey:_mockAnimation.keyPath];
  }
}

#pragma mark - Private

- (CGPoint)_scaleFromSize:(CGSize)sizeA toSize:(CGSize)sizeB
{
  return CGPointMake(sizeB.width / sizeA.width, sizeB.height / sizeA.height);
}

@end
