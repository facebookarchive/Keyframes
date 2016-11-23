/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "KFUtilities.h"

NSArray *KFMapArray(NSArray *arrayToMap, KFMapArrayHandler mapBlock) {
  NSMutableArray *mappedArray = [NSMutableArray array];
  for (id object in arrayToMap) {
    id result = mapBlock(object);
    if (result != nil) {
      [mappedArray addObject:result];
    }
  }
  return mappedArray;
}

NSArray *KFMapArrayWithIndex(NSArray *arrayToMap, KFMapArrayWithIndexHandler mapBlock)
{
  NSMutableArray *mappedArray = [NSMutableArray array];
  NSUInteger index = 0;
  for (id object in arrayToMap) {
    id result = mapBlock(object, index);
    if (result != nil) {
      [mappedArray addObject:result];
    }
    index++;
  }
  return mappedArray;
}

UIColor *KFColorWithHexString(NSString *string)
{
  NSString *hexString = [string copy];
  if ([hexString hasPrefix:@"#"]) {
    hexString = [hexString substringFromIndex:1];
  }

  NSScanner *scanner = [NSScanner scannerWithString:hexString];
  unsigned int rgb;

  if ([scanner scanHexInt:&rgb]) {
    // default alpha is ff if not included in input
    unsigned int alpha = (rgb >> 24) & 0xff;
    if (hexString.length <= 6 || (([hexString hasPrefix:@"0x"] || [hexString hasPrefix:@"0X"]) && hexString.length <= 8)) {
      alpha = 0xff;
    }
    return [UIColor colorWithRed:((CGFloat)((rgb >> 16) & 0xff) / 255)
                           green:((CGFloat)((rgb >> 8) & 0xff) / 255)
                            blue:((CGFloat)((rgb >> 0) & 0xff) / 255)
                           alpha:((CGFloat)(alpha) / 255)];
  }

  return [UIColor blackColor];
}

BOOL KFVersionLessThan(NSString *versionA, NSString *versionB)
{
  return [versionA compare:versionB options:NSNumericSearch] == NSOrderedAscending;
}

@implementation NSMutableArray (KFFoundation)

- (void)removeFirstObject
{
  if (self.count > 0) {
    [self removeObjectAtIndex:0];
  }
}

@end
