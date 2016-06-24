// Copyright 2004-present Facebook. All Rights Reserved.

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

@interface NSMutableArray<ObjectType> (KFFoundation)

- (void)removeFirstObject;

@end
