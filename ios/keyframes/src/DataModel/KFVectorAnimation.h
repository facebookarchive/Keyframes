/**
 * This file is generated using the remodel generation script.
 * The name of the input file is KFVectorAnimation.value
 */

#import <Foundation/Foundation.h>
#import <CoreGraphics/CGGeometry.h>

@interface KFVectorAnimation : NSObject <NSCopying, NSCoding>

@property (nonatomic, readonly, copy) NSString *property;
@property (nonatomic, readonly) CGPoint anchor;
@property (nonatomic, readonly) NSUInteger frameRate;
@property (nonatomic, readonly) NSUInteger animationFrameCount;
@property (nonatomic, readonly, copy) NSArray *keyValues;
@property (nonatomic, readonly, copy) NSArray *timingCurves;

- (instancetype)initWithProperty:(NSString *)property anchor:(CGPoint)anchor frameRate:(NSUInteger)frameRate animationFrameCount:(NSUInteger)animationFrameCount keyValues:(NSArray *)keyValues timingCurves:(NSArray *)timingCurves;

@end

