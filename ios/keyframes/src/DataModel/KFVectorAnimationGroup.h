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

#import <Foundation/Foundation.h>

/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 * 
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
@interface KFVectorAnimationGroup : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly, copy) NSString *groupName;
@property (nonatomic, readonly) NSInteger groupId;
@property (nonatomic, readonly) NSUInteger parentGroupId;
@property (nonatomic, readonly, copy) NSArray *animations;

- (instancetype)initWithGroupName:(NSString *)groupName groupId:(NSInteger)groupId parentGroupId:(NSUInteger)parentGroupId animations:(NSArray *)animations;

@end

