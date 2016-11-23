/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "KFVectorParsingHelper.h"

#import "KFVector.h"
#import "KFVectorAnimation.h"
#import "KFVectorAnimationGroup.h"
#import "KFVectorAnimationKeyValue.h"
#import "KFVectorBezierPathsHelper.h"
#import "KFVectorFeature.h"
#import "KFVectorFeatureKeyFrame.h"
#import "KFVectorGradientEffect.h"

#pragma mark - Internal structure helpers

static NSArray *_buildControlPoints(NSArray *points)
{
  NSArray *point1Array = points[0];
  NSArray *point2Array = points[1];

  CGPoint point1 = CGPointMake([point1Array[0] floatValue], [point1Array[1] floatValue]);
  CGPoint point2 = CGPointMake([point2Array[0] floatValue], [point2Array[1] floatValue]);
  return @[[NSValue valueWithCGPoint:point1], [NSValue valueWithCGPoint:point2]];
}

static KFVectorAnimation *_buildAnimationModelFromDictionary(NSDictionary *animationDictionary,
                                                             NSUInteger fromFrame,
                                                             NSUInteger toFrame,
                                                             CGSize canvasSize)
{
  if (animationDictionary == nil) {
    return nil;
  }
  CGPoint anchor = CGPointZero;
  if (animationDictionary[@"anchor"]) {
    CGFloat anchorX = [animationDictionary[@"anchor"][0] floatValue] / canvasSize.width;
    CGFloat anchorY = [animationDictionary[@"anchor"][1] floatValue] / canvasSize.height;
    anchor = CGPointMake(anchorX, anchorY);
  }

  NSMutableArray *keyValues = [NSMutableArray new];
  NSMutableArray *timingCurves = [NSMutableArray new];
  for (NSUInteger index = 0; index < [animationDictionary[@"key_values"] count]; ++index) {
    NSDictionary *keyFrameDictionary = animationDictionary[@"key_values"][index];
    NSUInteger startFrame = [keyFrameDictionary[@"start_frame"] unsignedIntegerValue];
    if (fromFrame <= startFrame && startFrame <= toFrame) {
      [keyValues addObject:[[KFVectorAnimationKeyValue alloc]
                            initWithKeyValue:keyFrameDictionary[@"data"]
                            startFrame:startFrame - fromFrame]];
      if (index > 0 && keyValues.count > 1) {
        NSArray *points = animationDictionary[@"timing_curves"][index - 1];
        [timingCurves addObject:_buildControlPoints(points)];
      }
    } else if ([animationDictionary[@"key_values"] count] == 1 && startFrame == 0) {
      // handle static value
      [keyValues addObject:[[KFVectorAnimationKeyValue alloc]
                            initWithKeyValue:keyFrameDictionary[@"data"]
                            startFrame:0]];
    }
  }

  return [[KFVectorAnimation alloc]
          initWithProperty:animationDictionary[@"property"]
          anchor:anchor
          keyValues:keyValues
          timingCurves:timingCurves];
}

static KFVectorGradientEffect *_buildGradientEffectsArrayFromArray(NSDictionary *effectsDictionary,
                                                                   NSUInteger fromFrame,
                                                                   NSUInteger toFrame,
                                                                   CGSize canvasSize)
{
  if (!effectsDictionary[@"gradient"]) {
    return nil;
  }

  NSDictionary *gradientEffectDictionary = effectsDictionary[@"gradient"];
  return [[KFVectorGradientEffect alloc]
          initWithGradientTypeString:gradientEffectDictionary[@"gradient_type"]
          colorStart:_buildAnimationModelFromDictionary(gradientEffectDictionary[@"color_start"], fromFrame, toFrame, canvasSize)
          colorEnd:_buildAnimationModelFromDictionary(gradientEffectDictionary[@"color_end"], fromFrame, toFrame, canvasSize)];
}

static KFVectorFeature *_buildFeatureModelFromDictionary(NSDictionary *featureDictionary,
                                                         NSUInteger fromFrame,
                                                         NSUInteger toFrame,
                                                         CGSize canvasSize)
{
  NSUInteger featureFromFrame = (featureDictionary[@"from_frame"] ? [featureDictionary[@"from_frame"] unsignedIntegerValue] : fromFrame);
  NSUInteger featureToFrame = (featureDictionary[@"to_frame"] ? [featureDictionary[@"to_frame"] unsignedIntegerValue] : toFrame);
  if (featureFromFrame > toFrame || featureToFrame < fromFrame) {
    return nil;
  }

  NSMutableArray *keyFrames = [NSMutableArray new];
  NSMutableArray *timingCurves = [NSMutableArray new];
  for (NSUInteger index = 0; index < [featureDictionary[@"key_frames"] count]; ++index) {
    NSDictionary *keyFrameDictionary = featureDictionary[@"key_frames"][index];
    NSUInteger startFrame = [keyFrameDictionary[@"start_frame"] unsignedIntegerValue];
    if (fromFrame <= startFrame && startFrame <= toFrame) {
      [keyFrames addObject:[[KFVectorFeatureKeyFrame alloc]
                            initWithType:keyFrameDictionary[@"type"]
                            paths:keyFrameDictionary[@"data"]
                            startFrame:startFrame - fromFrame]];
      if (index > 0 && keyFrames.count > 1) {
        NSArray *points = featureDictionary[@"timing_curves"][index - 1];
        [timingCurves addObject:_buildControlPoints(points)];
      }
    } else if ([featureDictionary[@"key_frames"] count] == 1 && startFrame == 0) {
      // handle static value
      [keyFrames addObject:[[KFVectorFeatureKeyFrame alloc]
                            initWithType:keyFrameDictionary[@"type"]
                            paths:keyFrameDictionary[@"data"]
                            startFrame:0]];
    }
  }
  NSArray *featureAnimations = KFMapArray(featureDictionary[@"feature_animations"], ^id(NSDictionary *featureAnimationDictionary) {
    return _buildAnimationModelFromDictionary(featureAnimationDictionary, fromFrame, toFrame, canvasSize);
  });
  CGSize featureSize = featureDictionary[@"size"] ? CGSizeMake([featureDictionary[@"size"][0] floatValue], [featureDictionary[@"size"][1] floatValue]) : canvasSize;

  return [[KFVectorFeature alloc]
          initWithName:featureDictionary[@"name"]
          featureSize:featureSize
          animationGroupId:featureDictionary[@"animation_group"] ? [featureDictionary[@"animation_group"] unsignedIntegerValue] : NSNotFound
          fromFrame:featureFromFrame < fromFrame ? 0 : featureFromFrame - fromFrame
          toFrame:featureToFrame - fromFrame
          fillColor:featureDictionary[@"fill_color"] ? KFColorWithHexString(featureDictionary[@"fill_color"]) : nil
          strokeColor:featureDictionary[@"stroke_color"] ? KFColorWithHexString(featureDictionary[@"stroke_color"]) : nil
          strokeWidth:[featureDictionary[@"stroke_width"] floatValue] / MIN(canvasSize.width, canvasSize.height)
          strokeLineCap:featureDictionary[@"stroke_line_cap"]
          keyFrames:keyFrames
          timingCurves:timingCurves
          featureAnimations:featureAnimations
          backedImage:featureDictionary[@"backed_image"]
          gradientEffect:_buildGradientEffectsArrayFromArray(featureDictionary[@"effects"], fromFrame, toFrame, canvasSize)];
}

static KFVectorAnimationGroup *_buildAnimationGroupModelFromDictionary(NSDictionary *animationGroupDictionary,
                                                                       NSUInteger fromFrame,
                                                                       NSUInteger toFrame,
                                                                       CGSize canvasSize)
{
  return
  [[KFVectorAnimationGroup alloc]
   initWithGroupName:animationGroupDictionary[@"group_name"]
   groupId:[animationGroupDictionary[@"group_id"] integerValue]
   parentGroupId:animationGroupDictionary[@"parent_group"] ? [animationGroupDictionary[@"parent_group"] integerValue] : NSNotFound
   animations:KFMapArray(animationGroupDictionary[@"animations"], ^id(NSDictionary *animationDictionary) {
    return _buildAnimationModelFromDictionary(animationDictionary, fromFrame, toFrame, canvasSize);
  })];
}

#pragma mark - Public method

KFVector *KFVectorFromDictionary(NSDictionary *faceDictionary)
{
  NSUInteger animationFrameCount = [faceDictionary[@"animation_frame_count"] unsignedIntegerValue];
  return KFVectorFromDictionaryInRange(faceDictionary, 0, animationFrameCount);
}

KFVector *KFVectorFromDictionaryInRange(NSDictionary *faceDictionary, NSUInteger fromFrame, NSUInteger toFrame)
{
  NSCAssert(fromFrame <= toFrame, @"From frame should be less than to frame.");
  NSCAssert(fromFrame >= 0, @"From frame should be greater or equal than zero.");
  NSCAssert(toFrame <= [faceDictionary[@"animation_frame_count"] unsignedIntegerValue], @"To frame should be less than frame count.");

  CGSize canvasSize = CGSizeMake([faceDictionary[@"canvas_size"][0] floatValue], [faceDictionary[@"canvas_size"][1] floatValue]);

  NSUInteger frameRate = [faceDictionary[@"frame_rate"] unsignedIntegerValue];
  NSMutableArray *featuresArray = [NSMutableArray new];
  for (NSDictionary *featureDictionary in faceDictionary[@"features"]) {
    KFVectorFeature *feature = _buildFeatureModelFromDictionary(featureDictionary, fromFrame, toFrame, canvasSize);
    if (feature) {
      [featuresArray addObject:feature];
    }
  }

  NSArray *animationGroups = KFMapArray(faceDictionary[@"animation_groups"], ^id(NSDictionary *animationGroupDictionary) {
    return _buildAnimationGroupModelFromDictionary(animationGroupDictionary, fromFrame, toFrame, canvasSize);
  });

  return
  [[KFVector alloc]
   initWithCanvasSize:canvasSize
   name:faceDictionary[@"name"]
   formatVersion:faceDictionary[@"formatVersion"] ? faceDictionary[@"formatVersion"] : @"0.0"
   key:[faceDictionary[@"key"] integerValue]
   frameRate:frameRate
   animationFrameCount:toFrame - fromFrame
   features:featuresArray
   animationGroups:animationGroups];
}
