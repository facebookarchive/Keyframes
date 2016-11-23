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
 * The name of the input file is KFVectorAnimationGroup.value
 */

#if  ! __has_feature(objc_arc)
#error This file must be compiled with ARC. Use -fobjc-arc flag (or convert project to ARC).
#endif

#import "KFVectorAnimationGroup.h"

static __unsafe_unretained NSString * const kGroupNameKey = @"GROUP_NAME";
static __unsafe_unretained NSString * const kGroupIdKey = @"GROUP_ID";
static __unsafe_unretained NSString * const kParentGroupIdKey = @"PARENT_GROUP_ID";
static __unsafe_unretained NSString * const kAnimationsKey = @"ANIMATIONS";

@implementation KFVectorAnimationGroup

- (instancetype)initWithCoder:(NSCoder *)aDecoder
{
  if ((self = [super init])) {
    _groupName = [aDecoder decodeObjectForKey:kGroupNameKey];
    _groupId = [aDecoder decodeIntegerForKey:kGroupIdKey];
    _parentGroupId = [aDecoder decodeIntegerForKey:kParentGroupIdKey];
    _animations = [aDecoder decodeObjectForKey:kAnimationsKey];
  }
  return self;
}

- (instancetype)initWithGroupName:(NSString *)groupName groupId:(NSInteger)groupId parentGroupId:(NSUInteger)parentGroupId animations:(NSArray *)animations
{
  if ((self = [super init])) {
    _groupName = [groupName copy];
    _groupId = groupId;
    _parentGroupId = parentGroupId;
    _animations = [animations copy];
  }

  return self;
}

- (id)copyWithZone:(NSZone *)zone
{
  return self;
}

- (NSString *)description
{
  return [NSString stringWithFormat:@"%@ - \n\t groupName: %@; \n\t groupId: %zd; \n\t parentGroupId: %tu; \n\t animations: %@; \n", [super description], _groupName, _groupId, _parentGroupId, _animations];
}

- (void)encodeWithCoder:(NSCoder *)aCoder
{
  [aCoder encodeObject:_groupName forKey:kGroupNameKey];
  [aCoder encodeInteger:_groupId forKey:kGroupIdKey];
  [aCoder encodeInteger:_parentGroupId forKey:kParentGroupIdKey];
  [aCoder encodeObject:_animations forKey:kAnimationsKey];
}

- (NSUInteger)hash
{
  NSUInteger subhashes[] = {[_groupName hash], ABS(_groupId), _parentGroupId, [_animations hash]};
  NSUInteger result = subhashes[0];
  for (int ii = 1; ii < 4; ++ii) {
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

- (BOOL)isEqual:(KFVectorAnimationGroup *)object
{
  if (self == object) {
    return YES;
  } else if (self == nil || object == nil || ![object isKindOfClass:[self class]]) {
    return NO;
  }
  return
    _groupId == object->_groupId &&
    _parentGroupId == object->_parentGroupId &&
    (_groupName == object->_groupName ? YES : [_groupName isEqual:object->_groupName]) &&
    (_animations == object->_animations ? YES : [_animations isEqual:object->_animations]);
}

@end

