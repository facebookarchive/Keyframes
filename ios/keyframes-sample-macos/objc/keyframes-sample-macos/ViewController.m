/* Copyright 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-sample file in the root directory of this source tree.
 */

#import "ViewController.h"

@import Keyframes;

@implementation ViewController
{
    KFVectorLayer *_sampleVectorLayer;
}

- (KFVector *)loadSampleVectorFromDisk
{
    static KFVector *sampleVector;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        NSString *filePath = [[NSBundle bundleForClass:[self class]] pathForResource:@"sample_logo" ofType:@"json" inDirectory:nil];
        NSData *data = [NSData dataWithContentsOfFile:filePath];
        NSDictionary *sampleVectorDictionary = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:nil];
        sampleVector = KFVectorFromDictionary(sampleVectorDictionary);
    });
    return sampleVector;
}


- (void)viewDidLoad {
    [super viewDidLoad];
    
    KFVector *sampleVector = [self loadSampleVectorFromDisk];
    
    self.view.wantsLayer = YES;
    self.view.layer.backgroundColor = [[NSColor whiteColor] CGColor];
    
    _sampleVectorLayer = [KFVectorLayer new];
    _sampleVectorLayer.frame = CGRectMake(self.view.bounds.size.width / 2 - 200, self.view.bounds.size.height / 2 - 200, 400, 400);
    _sampleVectorLayer.faceModel = sampleVector;
    
    [self.view.layer addSublayer:_sampleVectorLayer];
    
    [_sampleVectorLayer startAnimation];
}

- (void)viewDidLayout
{
    [super viewDidLayout];
    
    [CATransaction begin];
    [CATransaction setDisableActions:YES];
    [CATransaction setAnimationDuration:0];
    {
        _sampleVectorLayer.frame = CGRectMake(self.view.bounds.size.width / 2 - 200, self.view.bounds.size.height / 2 - 200, 400, 400);
    }
    [CATransaction commit];
}

@end
