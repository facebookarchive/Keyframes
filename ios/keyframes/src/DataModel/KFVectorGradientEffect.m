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
 * The name of the input file is KFVectorGradientEffect.value
 */

#if  ! __has_feature(objc_arc)
#error This file must be compiled with ARC. Use -fobjc-arc flag (or convert project to ARC).
#endif

#import "KFVectorGradientEffect.h"

static __unsafe_unretained NSString * const kGradientTypeStringKey = @"GRADIENT_TYPE_STRING";
static __unsafe_unretained NSString * const kColorStartKey = @"COLOR_START";
static __unsafe_unretained NSString * const kColorEndKey = @"COLOR_END";
static __unsafe_unretained NSString * const kRampStartKey = @"RAMP_START";
static __unsafe_unretained NSString * const kRampEndKey = @"RAMP_END";

@implementation KFVectorGradientEffect

- (instancetype)initWithCoder:(NSCoder *)aDecoder
{
  if ((self = [super init])) {
    _gradientTypeString = [aDecoder decodeObjectForKey:kGradientTypeStringKey];
    _colorStart = [aDecoder decodeObjectForKey:kColorStartKey];
    _colorEnd = [aDecoder decodeObjectForKey:kColorEndKey];
    _rampStart = [aDecoder decodeObjectForKey:kRampStartKey];
    _rampEnd = [aDecoder decodeObjectForKey:kRampEndKey];
  }
  return self;
}

- (instancetype)initWithGradientTypeString:(NSString *)gradientTypeString colorStart:(KFVectorAnimation *)colorStart colorEnd:(KFVectorAnimation *)colorEnd rampStart:(KFVectorAnimation *)rampStart rampEnd:(KFVectorAnimation *)rampEnd
{
  if ((self = [super init])) {
    _gradientTypeString = [gradientTypeString copy];
    _colorStart = [colorStart copy];
    _colorEnd = [colorEnd copy];
    _rampStart = [rampStart copy];
    _rampEnd = [rampEnd copy];
  }

  return self;
}

- (id)copyWithZone:(NSZone *)zone
{
  return self;
}

- (NSString *)description
{
  return [NSString stringWithFormat:@"%@ - \n\t gradientTypeString: %@; \n\t colorStart: %@; \n\t colorEnd: %@; \n\t rampStart: %@; \n\t rampEnd: %@; \n", [super description], _gradientTypeString, _colorStart, _colorEnd, _rampStart, _rampEnd];
}

- (void)encodeWithCoder:(NSCoder *)aCoder
{
  [aCoder encodeObject:_gradientTypeString forKey:kGradientTypeStringKey];
  [aCoder encodeObject:_colorStart forKey:kColorStartKey];
  [aCoder encodeObject:_colorEnd forKey:kColorEndKey];
  [aCoder encodeObject:_rampStart forKey:kRampStartKey];
  [aCoder encodeObject:_rampEnd forKey:kRampEndKey];
}

- (NSUInteger)hash
{
  NSUInteger subhashes[] = {[_gradientTypeString hash], [_colorStart hash], [_colorEnd hash], [_rampStart hash], [_rampEnd hash]};
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

- (BOOL)isEqual:(KFVectorGradientEffect *)object
{
  if (self == object) {
    return YES;
  } else if (self == nil || object == nil || ![object isKindOfClass:[self class]]) {
    return NO;
  }
  return
    (_gradientTypeString == object->_gradientTypeString ? YES : [_gradientTypeString isEqual:object->_gradientTypeString]) &&
    (_colorStart == object->_colorStart ? YES : [_colorStart isEqual:object->_colorStart]) &&
    (_colorEnd == object->_colorEnd ? YES : [_colorEnd isEqual:object->_colorEnd]) &&
    (_rampStart == object->_rampStart ? YES : [_rampStart isEqual:object->_rampStart]) &&
    (_rampEnd == object->_rampEnd ? YES : [_rampEnd isEqual:object->_rampEnd]);
}

@end

