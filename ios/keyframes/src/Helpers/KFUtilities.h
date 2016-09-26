/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
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

#import <UIKit/UIKit.h>

typedef id (^KFMapArrayHandler)(id object);
typedef id (^KFMapArrayWithIndexHandler)(id object, NSUInteger index);

NSArray *KFMapArray(NSArray *arrayToMap, KFMapArrayHandler mapBlock);
NSArray *KFMapArrayWithIndex(NSArray *arrayToMap, KFMapArrayWithIndexHandler mapBlock);

UIColor *KFColorWithHexString(NSString *hexString);

BOOL KFVersionLessThan(NSString *versionA, NSString *versionB);

@interface NSMutableArray<ObjectType> (KFFoundation)

- (void)removeFirstObject;

@end
