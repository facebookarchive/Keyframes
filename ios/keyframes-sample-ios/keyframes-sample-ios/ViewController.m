//
//  ViewController.m
//  keyframes-sample-ios
//
//  Created by Sean Lee on 7/5/16.
//  Copyright © 2016 facebook. All rights reserved.
//

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


