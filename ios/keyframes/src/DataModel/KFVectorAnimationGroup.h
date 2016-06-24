/**
 * This file is generated using the remodel generation script.
 * The name of the input file is KFVectorAnimationGroup.value
 */

#import <Foundation/Foundation.h>

@interface KFVectorAnimationGroup : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly, copy) NSString *groupName;
@property (nonatomic, readonly) NSInteger groupId;
@property (nonatomic, readonly) NSUInteger parentGroupId;
@property (nonatomic, readonly, copy) NSArray *animations;

- (instancetype)initWithGroupName:(NSString *)groupName groupId:(NSInteger)groupId parentGroupId:(NSUInteger)parentGroupId animations:(NSArray *)animations;

@end

