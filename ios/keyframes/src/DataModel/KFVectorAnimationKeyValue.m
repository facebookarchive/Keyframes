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
 * The name of the input file is KFVectorAnimationKeyValue.value
 */

#if  ! __has_feature(objc_arc)
#error This file must be compiled with ARC. Use -fobjc-arc flag (or convert project to ARC).
#endif

#import "KFVectorAnimationKeyValue.h"

static __unsafe_unretained NSString * const kKeyValueKey = @"KEY_VALUE";
static __unsafe_unretained NSString * const kStartFrameKey = @"START_FRAME";

@implementation KFVectorAnimationKeyValue

- (instancetype)initWithCoder:(NSCoder *)aDecoder
{
  if ((self = [super init])) {
    _keyValue = [aDecoder decodeObjectForKey:kKeyValueKey];
    _startFrame = [aDecoder decodeIntegerForKey:kStartFrameKey];
  }
  return self;
}

- (instancetype)initWithKeyValue:(id)keyValue startFrame:(NSInteger)startFrame
{
  if ((self = [super init])) {
    _keyValue = [keyValue copy];
    _startFrame = startFrame;
  }

  return self;
}

- (id)copyWithZone:(NSZone *)zone
{
  return self;
}

- (NSString *)description
{
  return [NSString stringWithFormat:@"%@ - \n\t keyValue: %@; \n\t startFrame: %zd; \n", [super description], _keyValue, _startFrame];
}

- (void)encodeWithCoder:(NSCoder *)aCoder
{
  [aCoder encodeObject:_keyValue forKey:kKeyValueKey];
  [aCoder encodeInteger:_startFrame forKey:kStartFrameKey];
}

- (NSUInteger)hash
{
  NSUInteger subhashes[] = {[_keyValue hash], ABS(_startFrame)};
  NSUInteger result = subhashes[0];
  for (int ii = 1; ii < 2; ++ii) {
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

- (BOOL)isEqual:(KFVectorAnimationKeyValue *)object
{
  if (self == object) {
    return YES;
  } else if (self == nil || object == nil || ![object isKindOfClass:[self class]]) {
    return NO;
  }
  return
    _startFrame == object->_startFrame &&
    (_keyValue == object->_keyValue ? YES : [_keyValue isEqual:object->_keyValue]);
}

@end

