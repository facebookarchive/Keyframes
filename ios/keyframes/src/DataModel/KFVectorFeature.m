/**
* Copyright (c) 2016-present, Facebook, Inc.
* All rights reserved.
*
* This source code is licensed under the BSD-style license found in the
* LICENSE file in the root directory of this source tree. An additional grant
* of patent rights can be found in the PATENTS file in the same directory.
*/
/**
 * This file is generated using the remodel generation script.
 * The name of the input file is KFVectorFeature.value
 */

#if  ! __has_feature(objc_arc)
#error This file must be compiled with ARC. Use -fobjc-arc flag (or convert project to ARC).
#endif

#import <UIKit/UIGeometry.h>
#import "KFVectorFeature.h"

static __unsafe_unretained NSString * const kNameKey = @"NAME";
static __unsafe_unretained NSString * const kFeatureSizeKey = @"FEATURE_SIZE";
static __unsafe_unretained NSString * const kAnimationGroupIdKey = @"ANIMATION_GROUP_ID";
static __unsafe_unretained NSString * const kFromFrameKey = @"FROM_FRAME";
static __unsafe_unretained NSString * const kToFrameKey = @"TO_FRAME";
static __unsafe_unretained NSString * const kFillColorKey = @"FILL_COLOR";
static __unsafe_unretained NSString * const kStrokeColorKey = @"STROKE_COLOR";
static __unsafe_unretained NSString * const kStrokeWidthKey = @"STROKE_WIDTH";
static __unsafe_unretained NSString * const kStrokeLineCapKey = @"STROKE_LINE_CAP";
static __unsafe_unretained NSString * const kKeyFramesKey = @"KEY_FRAMES";
static __unsafe_unretained NSString * const kTimingCurvesKey = @"TIMING_CURVES";
static __unsafe_unretained NSString * const kFeatureAnimationsKey = @"FEATURE_ANIMATIONS";
static __unsafe_unretained NSString * const kBackedImageKey = @"BACKED_IMAGE";
static __unsafe_unretained NSString * const kGradientEffectKey = @"GRADIENT_EFFECT";

static NSUInteger HashFloat(float givenFloat) {
  union {
    float key;
    uint32_t bits;
  } u;
  u.key = givenFloat;
  NSUInteger h = (NSUInteger)u.bits;
#if !TARGET_RT_64_BIT
  h = ~h + (h << 15);
  h ^= (h >> 12);
  h += (h << 2);
  h ^= (h >> 4);
  h *= 2057;
  h ^= (h >> 16);
#else
  h += ~h + (h << 21);
  h ^= (h >> 24);
  h = (h + (h << 3)) + (h << 8);
  h ^= (h >> 14);
  h = (h + (h << 2)) + (h << 4);
  h ^= (h >> 28);
  h += (h << 31);
#endif
  return h;
}

static NSUInteger HashDouble(double givenDouble) {
  union {
    double key;
    uint64_t bits;
  } u;
  u.key = givenDouble;
  NSUInteger p = u.bits;
  p = (~p) + (p << 18);
  p ^= (p >> 31);
  p *=  21;
  p ^= (p >> 11);
  p += (p << 6);
  p ^= (p >> 22);
  return (NSUInteger) p;
}

static NSUInteger HashCGFloat(CGFloat givenCGFloat) {
#if CGFLOAT_IS_DOUBLE
    BOOL useDouble = YES;
#else
    BOOL useDouble = NO;
#endif
    if (useDouble) {
      return HashDouble(givenCGFloat);
    } else {
      return HashFloat(givenCGFloat);
    }
}

static BOOL CompareFloats(float givenFloat, float floatToCompare) {
  return fabsf(givenFloat - floatToCompare) < FLT_EPSILON * fabsf(givenFloat + floatToCompare) || fabsf(givenFloat - floatToCompare) < FLT_MIN;
}

static BOOL CompareDoubles(double givenDouble, double doubleToCompare) {
  return fabs(givenDouble - doubleToCompare) < DBL_EPSILON * fabs(givenDouble + doubleToCompare) || fabs(givenDouble - doubleToCompare) < DBL_MIN;
}

static BOOL CompareCGFloats(CGFloat givenCGFloat, CGFloat cgFloatToCompare) {
#if CGFLOAT_IS_DOUBLE
    BOOL useDouble = YES;
#else
    BOOL useDouble = NO;
#endif
    if (useDouble) {
      return CompareDoubles(givenCGFloat, cgFloatToCompare);
    } else {
      return CompareFloats(givenCGFloat, cgFloatToCompare);
    }
}

@implementation KFVectorFeature

- (instancetype)initWithCoder:(NSCoder *)aDecoder
{
  if ((self = [super init])) {
    _name = [aDecoder decodeObjectForKey:kNameKey];
    _featureSize = CGSizeFromString([aDecoder decodeObjectForKey:kFeatureSizeKey]);
    _animationGroupId = [aDecoder decodeIntegerForKey:kAnimationGroupIdKey];
    _fromFrame = [aDecoder decodeIntegerForKey:kFromFrameKey];
    _toFrame = [aDecoder decodeIntegerForKey:kToFrameKey];
    _fillColor = [aDecoder decodeObjectForKey:kFillColorKey];
    _strokeColor = [aDecoder decodeObjectForKey:kStrokeColorKey];
    _strokeWidth = [aDecoder decodeFloatForKey:kStrokeWidthKey];
    _strokeLineCap = [aDecoder decodeObjectForKey:kStrokeLineCapKey];
    _keyFrames = [aDecoder decodeObjectForKey:kKeyFramesKey];
    _timingCurves = [aDecoder decodeObjectForKey:kTimingCurvesKey];
    _featureAnimations = [aDecoder decodeObjectForKey:kFeatureAnimationsKey];
    _backedImage = [aDecoder decodeObjectForKey:kBackedImageKey];
    _gradientEffect = [aDecoder decodeObjectForKey:kGradientEffectKey];
  }
  return self;
}

- (instancetype)initWithName:(NSString *)name featureSize:(CGSize)featureSize animationGroupId:(NSInteger)animationGroupId fromFrame:(NSUInteger)fromFrame toFrame:(NSUInteger)toFrame fillColor:(UIColor *)fillColor strokeColor:(UIColor *)strokeColor strokeWidth:(CGFloat)strokeWidth strokeLineCap:(NSString *)strokeLineCap keyFrames:(NSArray *)keyFrames timingCurves:(NSArray *)timingCurves featureAnimations:(NSArray *)featureAnimations backedImage:(NSString *)backedImage gradientEffect:(KFVectorGradientEffect *)gradientEffect
{
  if ((self = [super init])) {
    _name = [name copy];
    _featureSize = featureSize;
    _animationGroupId = animationGroupId;
    _fromFrame = fromFrame;
    _toFrame = toFrame;
    _fillColor = [fillColor copy];
    _strokeColor = [strokeColor copy];
    _strokeWidth = strokeWidth;
    _strokeLineCap = [strokeLineCap copy];
    _keyFrames = [keyFrames copy];
    _timingCurves = [timingCurves copy];
    _featureAnimations = [featureAnimations copy];
    _backedImage = [backedImage copy];
    _gradientEffect = [gradientEffect copy];
  }

  return self;
}

- (id)copyWithZone:(NSZone *)zone
{
  return self;
}

- (NSString *)description
{
  return [NSString stringWithFormat:@"%@ - \n\t name: %@; \n\t featureSize: %@; \n\t animationGroupId: %zd; \n\t fromFrame: %tu; \n\t toFrame: %tu; \n\t fillColor: %@; \n\t strokeColor: %@; \n\t strokeWidth: %f; \n\t strokeLineCap: %@; \n\t keyFrames: %@; \n\t timingCurves: %@; \n\t featureAnimations: %@; \n\t backedImage: %@; \n\t gradientEffect: %@; \n", [super description], _name, NSStringFromCGSize(_featureSize), _animationGroupId, _fromFrame, _toFrame, _fillColor, _strokeColor, _strokeWidth, _strokeLineCap, _keyFrames, _timingCurves, _featureAnimations, _backedImage, _gradientEffect];
}

- (void)encodeWithCoder:(NSCoder *)aCoder
{
  [aCoder encodeObject:_name forKey:kNameKey];
  [aCoder encodeObject:NSStringFromCGSize(_featureSize) forKey:kFeatureSizeKey];
  [aCoder encodeInteger:_animationGroupId forKey:kAnimationGroupIdKey];
  [aCoder encodeInteger:_fromFrame forKey:kFromFrameKey];
  [aCoder encodeInteger:_toFrame forKey:kToFrameKey];
  [aCoder encodeObject:_fillColor forKey:kFillColorKey];
  [aCoder encodeObject:_strokeColor forKey:kStrokeColorKey];
  [aCoder encodeFloat:_strokeWidth forKey:kStrokeWidthKey];
  [aCoder encodeObject:_strokeLineCap forKey:kStrokeLineCapKey];
  [aCoder encodeObject:_keyFrames forKey:kKeyFramesKey];
  [aCoder encodeObject:_timingCurves forKey:kTimingCurvesKey];
  [aCoder encodeObject:_featureAnimations forKey:kFeatureAnimationsKey];
  [aCoder encodeObject:_backedImage forKey:kBackedImageKey];
  [aCoder encodeObject:_gradientEffect forKey:kGradientEffectKey];
}

- (NSUInteger)hash
{
  NSUInteger subhashes[] = {[_name hash], HashCGFloat(_featureSize.width), HashCGFloat(_featureSize.height), ABS(_animationGroupId), _fromFrame, _toFrame, [_fillColor hash], [_strokeColor hash], HashCGFloat(_strokeWidth), [_strokeLineCap hash], [_keyFrames hash], [_timingCurves hash], [_featureAnimations hash], [_backedImage hash], [_gradientEffect hash]};
  NSUInteger result = subhashes[0];
  for (int ii = 1; ii < 15; ++ii) {
    unsigned long long base = (((unsigned long long)result) << 32 | subhashes[ii]);
    base = (~base) + (base << 18);
    base ^= (base >> 31);
    base *=  21;
    base ^= (base >> 11);
    base += (base << 6);
    base ^= (base >> 22);
    result = base;
  }
  return result;
}

- (BOOL)isEqual:(KFVectorFeature *)object
{
  if (self == object) {
    return YES;
  } else if (self == nil || object == nil || ![object isKindOfClass:[self class]]) {
    return NO;
  }
  return
    _animationGroupId == object->_animationGroupId &&
    _fromFrame == object->_fromFrame &&
    _toFrame == object->_toFrame &&
    CompareCGFloats(_strokeWidth, object->_strokeWidth) &&
    CGSizeEqualToSize(_featureSize, object->_featureSize) &&
    (_fillColor == object->_fillColor ? YES : [_fillColor isEqual:object->_fillColor]) &&
    (_strokeColor == object->_strokeColor ? YES : [_strokeColor isEqual:object->_strokeColor]) &&
    (_name == object->_name ? YES : [_name isEqual:object->_name]) &&
    (_strokeLineCap == object->_strokeLineCap ? YES : [_strokeLineCap isEqual:object->_strokeLineCap]) &&
    (_keyFrames == object->_keyFrames ? YES : [_keyFrames isEqual:object->_keyFrames]) &&
    (_timingCurves == object->_timingCurves ? YES : [_timingCurves isEqual:object->_timingCurves]) &&
    (_featureAnimations == object->_featureAnimations ? YES : [_featureAnimations isEqual:object->_featureAnimations]) &&
    (_backedImage == object->_backedImage ? YES : [_backedImage isEqual:object->_backedImage]) &&
    (_gradientEffect == object->_gradientEffect ? YES : [_gradientEffect isEqual:object->_gradientEffect]);
}

@end

