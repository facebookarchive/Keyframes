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
 * The name of the input file is KFVectorPathTrim.value
 */

#if  ! __has_feature(objc_arc)
#error This file must be compiled with ARC. Use -fobjc-arc flag (or convert project to ARC).
#endif

#import "KFVectorPathTrim.h"

static __unsafe_unretained NSString * const kPathTrimStartKey = @"PATH_TRIM_START";
static __unsafe_unretained NSString * const kPathTrimEndKey = @"PATH_TRIM_END";
static __unsafe_unretained NSString * const kPathTrimOffsetKey = @"PATH_TRIM_OFFSET";

@implementation KFVectorPathTrim

- (instancetype)initWithCoder:(NSCoder *)aDecoder
{
  if ((self = [super init])) {
    _pathTrimStart = [aDecoder decodeObjectForKey:kPathTrimStartKey];
    _pathTrimEnd = [aDecoder decodeObjectForKey:kPathTrimEndKey];
    _pathTrimOffset = [aDecoder decodeObjectForKey:kPathTrimOffsetKey];
  }
  return self;
}

- (instancetype)initWithPathTrimStart:(KFVectorAnimation *)pathTrimStart pathTrimEnd:(KFVectorAnimation *)pathTrimEnd pathTrimOffset:(KFVectorAnimation *)pathTrimOffset
{
  if ((self = [super init])) {
    _pathTrimStart = [pathTrimStart copy];
    _pathTrimEnd = [pathTrimEnd copy];
    _pathTrimOffset = [pathTrimOffset copy];
  }

  return self;
}

- (id)copyWithZone:(NSZone *)zone
{
  return self;
}

- (NSString *)description
{
  return [NSString stringWithFormat:@"%@ - \n\t pathTrimStart: %@; \n\t pathTrimEnd: %@; \n\t pathTrimOffset: %@; \n", [super description], _pathTrimStart, _pathTrimEnd, _pathTrimOffset];
}

- (void)encodeWithCoder:(NSCoder *)aCoder
{
  [aCoder encodeObject:_pathTrimStart forKey:kPathTrimStartKey];
  [aCoder encodeObject:_pathTrimEnd forKey:kPathTrimEndKey];
  [aCoder encodeObject:_pathTrimOffset forKey:kPathTrimOffsetKey];
}

- (NSUInteger)hash
{
  NSUInteger subhashes[] = {[_pathTrimStart hash], [_pathTrimEnd hash], [_pathTrimOffset hash]};
  NSUInteger result = subhashes[0];
  for (int ii = 1; ii < 3; ++ii) {
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

- (BOOL)isEqual:(KFVectorPathTrim *)object
{
  if (self == object) {
    return YES;
  } else if (self == nil || object == nil || ![object isKindOfClass:[self class]]) {
    return NO;
  }
  return
    (_pathTrimStart == object->_pathTrimStart ? YES : [_pathTrimStart isEqual:object->_pathTrimStart]) &&
    (_pathTrimEnd == object->_pathTrimEnd ? YES : [_pathTrimEnd isEqual:object->_pathTrimEnd]) &&
    (_pathTrimOffset == object->_pathTrimOffset ? YES : [_pathTrimOffset isEqual:object->_pathTrimOffset]);
}

@end

