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
 * The name of the input file is KFVectorAnimation.value
 */

#if  ! __has_feature(objc_arc)
#error This file must be compiled with ARC. Use -fobjc-arc flag (or convert project to ARC).
#endif

#import <UIKit/UIGeometry.h>
#import "KFVectorAnimation.h"

static __unsafe_unretained NSString * const kPropertyKey = @"PROPERTY";
static __unsafe_unretained NSString * const kAnchorKey = @"ANCHOR";
static __unsafe_unretained NSString * const kKeyValuesKey = @"KEY_VALUES";
static __unsafe_unretained NSString * const kTimingCurvesKey = @"TIMING_CURVES";

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

@implementation KFVectorAnimation

- (instancetype)initWithCoder:(NSCoder *)aDecoder
{
  if ((self = [super init])) {
    _property = [aDecoder decodeObjectForKey:kPropertyKey];
    _anchor = CGPointFromString([aDecoder decodeObjectForKey:kAnchorKey]);
    _keyValues = [aDecoder decodeObjectForKey:kKeyValuesKey];
    _timingCurves = [aDecoder decodeObjectForKey:kTimingCurvesKey];
  }
  return self;
}

- (instancetype)initWithProperty:(NSString *)property anchor:(CGPoint)anchor keyValues:(NSArray *)keyValues timingCurves:(NSArray *)timingCurves
{
  if ((self = [super init])) {
    _property = [property copy];
    _anchor = anchor;
    _keyValues = [keyValues copy];
    _timingCurves = [timingCurves copy];
  }

  return self;
}

- (id)copyWithZone:(NSZone *)zone
{
  return self;
}

- (NSString *)description
{
  return [NSString stringWithFormat:@"%@ - \n\t property: %@; \n\t anchor: %@; \n\t keyValues: %@; \n\t timingCurves: %@; \n", [super description], _property, NSStringFromCGPoint(_anchor), _keyValues, _timingCurves];
}

- (void)encodeWithCoder:(NSCoder *)aCoder
{
  [aCoder encodeObject:_property forKey:kPropertyKey];
  [aCoder encodeObject:NSStringFromCGPoint(_anchor) forKey:kAnchorKey];
  [aCoder encodeObject:_keyValues forKey:kKeyValuesKey];
  [aCoder encodeObject:_timingCurves forKey:kTimingCurvesKey];
}

- (NSUInteger)hash
{
  NSUInteger subhashes[] = {[_property hash], HashCGFloat(_anchor.x), HashCGFloat(_anchor.y), [_keyValues hash], [_timingCurves hash]};
  NSUInteger result = subhashes[0];
  for (int ii = 1; ii < 5; ++ii) {
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

- (BOOL)isEqual:(KFVectorAnimation *)object
{
  if (self == object) {
    return YES;
  } else if (self == nil || object == nil || ![object isKindOfClass:[self class]]) {
    return NO;
  }
  return
    CGPointEqualToPoint(_anchor, object->_anchor) &&
    (_property == object->_property ? YES : [_property isEqual:object->_property]) &&
    (_keyValues == object->_keyValues ? YES : [_keyValues isEqual:object->_keyValues]) &&
    (_timingCurves == object->_timingCurves ? YES : [_timingCurves isEqual:object->_timingCurves]);
}

@end

