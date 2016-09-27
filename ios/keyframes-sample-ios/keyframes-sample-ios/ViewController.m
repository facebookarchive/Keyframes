/* This file provided by Facebook is for non-commercial testing and evaluation
 * purposes only.  Facebook reserves all rights not expressly granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * FACEBOOK BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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


