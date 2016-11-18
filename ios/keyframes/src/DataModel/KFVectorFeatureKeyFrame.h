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
 * The name of the input file is KFVectorFeatureKeyFrame.value
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
@interface KFVectorFeatureKeyFrame : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly, copy) NSString *type;
@property (nonatomic, readonly, copy) NSArray *paths;
@property (nonatomic, readonly) NSInteger startFrame;

- (instancetype)initWithType:(NSString *)type paths:(NSArray *)paths startFrame:(NSInteger)startFrame;

@end

