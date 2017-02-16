/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "Compatibility.h"

@interface UIBezierPath (KFVectorLayer)

/// Returns the trimmed path, start should be from 0..1 and end should be from 0..1
- (UIBezierPath *)pathTrimFrom:(CGFloat)start to:(CGFloat)end;

@end
