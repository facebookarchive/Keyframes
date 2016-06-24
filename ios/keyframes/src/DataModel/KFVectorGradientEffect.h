/**
 * This file is generated using the remodel generation script.
 * The name of the input file is KFVectorGradientEffect.value
 */

#import <Foundation/Foundation.h>
#import <KFVectorDrawingKit/KFVectorAnimation.h>
#import "KFVectorAnimation.h"

@interface KFVectorGradientEffect : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly, copy) NSString *gradientTypeString;
@property (nonatomic, readonly, copy) KFVectorAnimation *colorStart;
@property (nonatomic, readonly, copy) KFVectorAnimation *colorEnd;

- (instancetype)initWithGradientTypeString:(NSString *)gradientTypeString colorStart:(KFVectorAnimation *)colorStart colorEnd:(KFVectorAnimation *)colorEnd;

@end

