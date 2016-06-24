#import <Foundation/Foundation.h>

@class KFVectorFeatureKeyFrame;

@interface KFVectorFeatureKeyFrameBuilder : NSObject

+ (instancetype)vectorFeatureKeyFrame;

+ (instancetype)vectorFeatureKeyFrameFromExistingVectorFeatureKeyFrame:(KFVectorFeatureKeyFrame *)existingVectorFeatureKeyFrame;

- (KFVectorFeatureKeyFrame *)build;

- (instancetype)withType:(NSString *)type;

- (instancetype)withPaths:(NSArray *)paths;

- (instancetype)withStartFrame:(NSInteger)startFrame;

@end

