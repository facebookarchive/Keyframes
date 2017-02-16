/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#ifndef KFBASE_DEFINES_H
#define KFBASE_DEFINES_H

#ifdef __cplusplus
# define KF_EXTERN_C_BEGIN extern "C" {
# define KF_EXTERN_C_END   }
#else
# define KF_EXTERN_C_BEGIN
# define KF_EXTERN_C_END
#endif

#endif

#import <Foundation/Foundation.h>

#import <TargetConditionals.h>

#if TARGET_OS_OSX
    @class NSColor;
#else
    @class UIColor;
#endif

typedef id (^KFMapArrayHandler)(id object);
typedef id (^KFMapArrayWithIndexHandler)(id object, NSUInteger index);

NSArray *KFMapArray(NSArray *arrayToMap, KFMapArrayHandler mapBlock);
NSArray *KFMapArrayWithIndex(NSArray *arrayToMap, KFMapArrayWithIndexHandler mapBlock);

#if TARGET_OS_OSX
    NSColor *KFColorWithHexString(NSString *hexString);
#else
    UIColor *KFColorWithHexString(NSString *hexString);
#endif

BOOL KFVersionLessThan(NSString *versionA, NSString *versionB);

@interface NSMutableArray<ObjectType> (KFFoundation)

- (void)removeFirstObject;

@end
