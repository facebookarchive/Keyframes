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

#import <Foundation/Foundation.h>
#import "KFVectorAnimation.h"

/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 * 
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
@interface KFVectorPathTrim : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly, copy) KFVectorAnimation *pathTrimStart;
@property (nonatomic, readonly, copy) KFVectorAnimation *pathTrimEnd;
@property (nonatomic, readonly, copy) KFVectorAnimation *pathTrimOffset;

- (instancetype)initWithPathTrimStart:(KFVectorAnimation *)pathTrimStart pathTrimEnd:(KFVectorAnimation *)pathTrimEnd pathTrimOffset:(KFVectorAnimation *)pathTrimOffset;

@end

