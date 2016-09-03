/**
 * Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 *
 */
/**
 * This file is generated using the remodel generation script.
 * The name of the input file is KFVectorAnimationKeyValue.value
 */

#import <Foundation/Foundation.h>

/**
 * Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 * 
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 * 
 */
@interface KFVectorAnimationKeyValue : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly, copy) id keyValue;
@property (nonatomic, readonly) NSInteger startFrame;

- (instancetype)initWithKeyValue:(id)keyValue startFrame:(NSInteger)startFrame;

@end

