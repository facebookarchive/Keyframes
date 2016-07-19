/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

#import "ViewController.h"

#import <keyframes/KFVector.h>
#import <keyframes/KFVectorLayer.h>
#import <keyframes/KFVectorParsingHelper.h>

@interface ViewController ()

@end

@implementation ViewController

- (KFVector *)loadLikeVectorFromDisk
{
  static KFVector *likeVector;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    NSString *filePath = [[NSBundle bundleForClass:[self class]] pathForResource:@"sample_like" ofType:@"json" inDirectory:nil];
    NSData *data = [NSData dataWithContentsOfFile:filePath];
    NSDictionary *likeVectorDictionary = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:nil];
    likeVector = KFVectorFromDictionary(likeVectorDictionary);
  });
  return likeVector;
}


- (void)viewDidLoad {
  [super viewDidLoad];
  // Do any additional setup after loading the view, typically from a nib.
  
  KFVector *likeVector = [self loadLikeVectorFromDisk];
  
  KFVectorLayer *likeVectorLayer = [KFVectorLayer new];
  const CGFloat shortSide = MIN(CGRectGetWidth(self.view.bounds), CGRectGetHeight(self.view.bounds));
  const CGFloat longSide = MAX(CGRectGetWidth(self.view.bounds), CGRectGetHeight(self.view.bounds));
  likeVectorLayer.frame = CGRectMake(shortSide / 4, longSide / 2 - shortSide / 4, shortSide / 2, shortSide / 2);
  likeVectorLayer.faceModel = likeVector;
  [self.view.layer addSublayer:likeVectorLayer];
  [likeVectorLayer startAnimation];
}

- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
  // Dispose of any resources that can be recreated.
}

@end


