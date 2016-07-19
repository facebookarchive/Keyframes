/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

#import <UIKit/UIKit.h>

@class KFVector;

@interface KFVectorView : UIView

- (instancetype)initWithFrame:(CGRect)frame faceVector:(KFVector *)faceVector NS_DESIGNATED_INITIALIZER;

- (instancetype)init __attribute__((unavailable("Must use designated initializer")));

@end
