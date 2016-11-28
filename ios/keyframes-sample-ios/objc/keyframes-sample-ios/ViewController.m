/* Copyright 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-sample file in the root directory of this source tree.
 */

#import "ViewController.h"

#import <keyframes/KFVector.h>
#import <keyframes/KFVectorLayer.h>
#import <keyframes/KFVectorParsingHelper.h>

@interface ViewController ()

@end

@implementation ViewController

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
  // Do any additional setup after loading the view, typically from a nib.
  
  KFVector *sampleVector = [self loadSampleVectorFromDisk];
  
  KFVectorLayer *sampleVectorLayer = [KFVectorLayer new];
  const CGFloat shortSide = MIN(CGRectGetWidth(self.view.bounds), CGRectGetHeight(self.view.bounds));
  const CGFloat longSide = MAX(CGRectGetWidth(self.view.bounds), CGRectGetHeight(self.view.bounds));
  sampleVectorLayer.frame = CGRectMake(shortSide / 4, longSide / 2 - shortSide / 4, shortSide / 2, shortSide / 2);
  sampleVectorLayer.faceModel = sampleVector;
  [self.view.layer addSublayer:sampleVectorLayer];
  [sampleVectorLayer startAnimation];
}

- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
  // Dispose of any resources that can be recreated.
}

@end


