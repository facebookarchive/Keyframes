---
docid: ios-rendering
title: iOS Rendering
layout: docs
permalink: /docs/ios/rendering
---

#### Setup

Use the provided deserializers on the generated JSON blob from the **Image Data Extraction** step to create a `KFVector` model object.  If your JSON blob lives in the assets directory, this might look like:

```objc
NSString *filePath = [[NSBundle bundleForClass:[self class]] pathForResource:@"asset_name" ofType:@"json" inDirectory:nil];
NSData *data = [NSData dataWithContentsOfFile:filePath];
NSDictionary *vectorDictionary = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:nil];
KFVector *vector = KFVectorFromDictionary(vectorDictionary);
```

Then a `KFVectorLayer` can be created using this `KFVector`, `KFVectorLayer` can be used as normal `CALayer`.

```objc
KFVectorLayer *layer = [KFVectorLayer layer];
// Set a non-zero layer frame before setting the KFVector is required.
layer.frame = $AnyNonZeroCGRect$;
layer.faceModel = vector;
```

If you don't want the control of animation and prefer a `UIView`, `KFVectorView` can also be created using `KFVector`.

```objc
KFVectorView *view = [[KFVectorView alloc] initWithFrame:$AnyNonZeroCGRect$ faceVector:vector];
```

#### Play!
Use `startAnimation`, `pauseAnimation`, `resumeAnimation` and `seekToProgress` on `KFVectorLayer` to control the animation.

```objc
// Animation will start from beginning.
[layer startAnimation];

// Pause the animation at current progress.
[layer pauseAnimation];

// Resume the animation from where we paused last time.
[layer resumeAnimation];

// Seek to a given progress in range [0, 1]
[layer seekToProgress:0.5]; // seek to the mid point of the animation
```
