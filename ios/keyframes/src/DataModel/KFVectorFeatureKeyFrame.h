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
 * The name of the input file is KFVectorFeatureKeyFrame.value
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
@interface KFVectorFeatureKeyFrame : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly, copy) NSString *type;
@property (nonatomic, readonly, copy) NSArray *paths;
@property (nonatomic, readonly) NSInteger startFrame;

- (instancetype)initWithType:(NSString *)type paths:(NSArray *)paths startFrame:(NSInteger)startFrame;

@end

