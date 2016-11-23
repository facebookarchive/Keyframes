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
 * The name of the input file is KFVector.value
 */

#if  ! __has_feature(objc_arc)
#error This file must be compiled with ARC. Use -fobjc-arc flag (or convert project to ARC).
#endif

#import <UIKit/UIGeometry.h>
#import "KFVector.h"

static __unsafe_unretained NSString * const kCanvasSizeKey = @"CANVAS_SIZE";
static __unsafe_unretained NSString * const kNameKey = @"NAME";
static __unsafe_unretained NSString * const kFormatVersionKey = @"FORMAT_VERSION";
static __unsafe_unretained NSString * const kKeyKey = @"KEY";
static __unsafe_unretained NSString * const kFrameRateKey = @"FRAME_RATE";
static __unsafe_unretained NSString * const kAnimationFrameCountKey = @"ANIMATION_FRAME_COUNT";
static __unsafe_unretained NSString * const kFeaturesKey = @"FEATURES";
static __unsafe_unretained NSString * const kAnimationGroupsKey = @"ANIMATION_GROUPS";

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

@implementation KFVector

- (instancetype)initWithCanvasSize:(CGSize)canvasSize name:(NSString *)name formatVersion:(NSString *)formatVersion key:(NSInteger)key frameRate:(NSUInteger)frameRate animationFrameCount:(NSUInteger)animationFrameCount features:(NSArray *)features animationGroups:(NSArray *)animationGroups
{
  if ((self = [super init])) {
    _canvasSize = canvasSize;
    _name = [name copy];
    _formatVersion = [formatVersion copy];
    _key = key;
    _frameRate = frameRate;
    _animationFrameCount = animationFrameCount;
    _features = [features copy];
    _animationGroups = [animationGroups copy];
  }

  return self;
}

- (instancetype)initWithCoder:(NSCoder *)aDecoder
{
  if ((self = [super init])) {
    _canvasSize = CGSizeFromString([aDecoder decodeObjectForKey:kCanvasSizeKey]);
    _name = [aDecoder decodeObjectForKey:kNameKey];
    _formatVersion = [aDecoder decodeObjectForKey:kFormatVersionKey];
    _key = [aDecoder decodeIntegerForKey:kKeyKey];
    _frameRate = [aDecoder decodeIntegerForKey:kFrameRateKey];
    _animationFrameCount = [aDecoder decodeIntegerForKey:kAnimationFrameCountKey];
    _features = [aDecoder decodeObjectForKey:kFeaturesKey];
    _animationGroups = [aDecoder decodeObjectForKey:kAnimationGroupsKey];
  }
  return self;
}

- (id)copyWithZone:(NSZone *)zone
{
  return self;
}

- (NSString *)description
{
  return [NSString stringWithFormat:@"%@ - \n\t canvasSize: %@; \n\t name: %@; \n\t formatVersion: %@; \n\t key: %zd; \n\t frameRate: %tu; \n\t animationFrameCount: %tu; \n\t features: %@; \n\t animationGroups: %@; \n", [super description], NSStringFromCGSize(_canvasSize), _name, _formatVersion, _key, _frameRate, _animationFrameCount, _features, _animationGroups];
}

- (void)encodeWithCoder:(NSCoder *)aCoder
{
  [aCoder encodeObject:NSStringFromCGSize(_canvasSize) forKey:kCanvasSizeKey];
  [aCoder encodeObject:_name forKey:kNameKey];
  [aCoder encodeObject:_formatVersion forKey:kFormatVersionKey];
  [aCoder encodeInteger:_key forKey:kKeyKey];
  [aCoder encodeInteger:_frameRate forKey:kFrameRateKey];
  [aCoder encodeInteger:_animationFrameCount forKey:kAnimationFrameCountKey];
  [aCoder encodeObject:_features forKey:kFeaturesKey];
  [aCoder encodeObject:_animationGroups forKey:kAnimationGroupsKey];
}

- (NSUInteger)hash
{
  NSUInteger subhashes[] = {HashCGFloat(_canvasSize.width), HashCGFloat(_canvasSize.height), [_name hash], [_formatVersion hash], ABS(_key), _frameRate, _animationFrameCount, [_features hash], [_animationGroups hash]};
  NSUInteger result = subhashes[0];
  for (int ii = 1; ii < 9; ++ii) {
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

- (BOOL)isEqual:(KFVector *)object
{
  if (self == object) {
    return YES;
  } else if (self == nil || object == nil || ![object isKindOfClass:[self class]]) {
    return NO;
  }
  return
    _key == object->_key &&
    _frameRate == object->_frameRate &&
    _animationFrameCount == object->_animationFrameCount &&
    CGSizeEqualToSize(_canvasSize, object->_canvasSize) &&
    (_name == object->_name ? YES : [_name isEqual:object->_name]) &&
    (_formatVersion == object->_formatVersion ? YES : [_formatVersion isEqual:object->_formatVersion]) &&
    (_features == object->_features ? YES : [_features isEqual:object->_features]) &&
    (_animationGroups == object->_animationGroups ? YES : [_animationGroups isEqual:object->_animationGroups]);
}

@end

