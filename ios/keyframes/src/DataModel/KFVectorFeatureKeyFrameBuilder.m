#if  ! __has_feature(objc_arc)
#error This file must be compiled with ARC. Use -fobjc-arc flag (or convert project to ARC).
#endif

#import "KFVectorFeatureKeyFrame.h"
#import "KFVectorFeatureKeyFrameBuilder.h"

@implementation KFVectorFeatureKeyFrameBuilder
{
  NSString *_type;
  NSArray *_paths;
  NSInteger _startFrame;
}

+ (instancetype)vectorFeatureKeyFrame
{
  return [[KFVectorFeatureKeyFrameBuilder alloc] init];
}

+ (instancetype)vectorFeatureKeyFrameFromExistingVectorFeatureKeyFrame:(KFVectorFeatureKeyFrame *)existingVectorFeatureKeyFrame
{
  return [[[[KFVectorFeatureKeyFrameBuilder vectorFeatureKeyFrame]
            withType:existingVectorFeatureKeyFrame.type]
           withPaths:existingVectorFeatureKeyFrame.paths]
          withStartFrame:existingVectorFeatureKeyFrame.startFrame];
}

- (KFVectorFeatureKeyFrame *)build
{
  return [[KFVectorFeatureKeyFrame alloc] initWithType:_type paths:_paths startFrame:_startFrame];
}

- (instancetype)withType:(NSString *)type
{
  _type = [type copy];
  return self;
}

- (instancetype)withPaths:(NSArray *)paths
{
  _paths = [paths copy];
  return self;
}

- (instancetype)withStartFrame:(NSInteger)startFrame
{
  _startFrame = startFrame;
  return self;
}

@end

