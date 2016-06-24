#import <Foundation/Foundation.h>

@class KFVectorAnimationKeyValue;

@interface KFVectorAnimationKeyValueBuilder : NSObject

+ (instancetype)vectorAnimationKeyValue;

+ (instancetype)vectorAnimationKeyValueFromExistingVectorAnimationKeyValue:(KFVectorAnimationKeyValue *)existingVectorAnimationKeyValue;

- (KFVectorAnimationKeyValue *)build;

- (instancetype)withKeyValue:(id)keyValue;

- (instancetype)withStartFrame:(NSInteger)startFrame;

@end

