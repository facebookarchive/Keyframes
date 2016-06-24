/**
 * This file is generated using the remodel generation script.
 * The name of the input file is KFVectorFeatureKeyFrame.value
 */

#import <Foundation/Foundation.h>

@interface KFVectorFeatureKeyFrame : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly, copy) NSString *type;
@property (nonatomic, readonly, copy) NSArray *paths;
@property (nonatomic, readonly) NSInteger startFrame;

- (instancetype)initWithType:(NSString *)type paths:(NSArray *)paths startFrame:(NSInteger)startFrame;

@end

