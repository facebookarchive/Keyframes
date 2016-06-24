#if  ! __has_feature(objc_arc)
#error This file must be compiled with ARC. Use -fobjc-arc flag (or convert project to ARC).
#endif

#import "KFVectorAnimationKeyValue.h"
#import "KFVectorAnimationKeyValueBuilder.h"

@implementation KFVectorAnimationKeyValueBuilder
{
  id _keyValue;
  NSInteger _startFrame;
}

+ (instancetype)vectorAnimationKeyValue
{
  return [[KFVectorAnimationKeyValueBuilder alloc] init];
}

+ (instancetype)vectorAnimationKeyValueFromExistingVectorAnimationKeyValue:(KFVectorAnimationKeyValue *)existingVectorAnimationKeyValue
{
  return [[[KFVectorAnimationKeyValueBuilder vectorAnimationKeyValue]
           withKeyValue:existingVectorAnimationKeyValue.keyValue]
          withStartFrame:existingVectorAnimationKeyValue.startFrame];
}

- (KFVectorAnimationKeyValue *)build
{
  return [[KFVectorAnimationKeyValue alloc] initWithKeyValue:_keyValue startFrame:_startFrame];
}

- (instancetype)withKeyValue:(id)keyValue
{
  _keyValue = [keyValue copy];
  return self;
}

- (instancetype)withStartFrame:(NSInteger)startFrame
{
  _startFrame = startFrame;
  return self;
}

@end

