// Copyright 2004-present Facebook. All Rights Reserved.

#import <UIKit/UIKit.h>

@class KFVector;

@interface KFVectorView : UIView

- (instancetype)initWithFrame:(CGRect)frame faceVector:(KFVector *)faceVector NS_DESIGNATED_INITIALIZER;

- (instancetype)init __attribute__((unavailable("Must use designated initializer")));

@end
