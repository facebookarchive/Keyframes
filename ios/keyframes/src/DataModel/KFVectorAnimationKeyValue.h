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

#import <Foundation/Foundation.h>

/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 * 
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
@interface KFVectorAnimationKeyValue : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly, copy) id keyValue;
@property (nonatomic, readonly) NSInteger startFrame;

- (instancetype)initWithKeyValue:(id)keyValue startFrame:(NSInteger)startFrame;

@end

