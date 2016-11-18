![Keyframes](/docs/images/doc-logo.png)
# Keyframes

Keyframes is a combination of (1) an ExtendScript script that extracts image animation data from an After Effects file and (2) a corresponding rendering library for Android and iOS.  Keyframes can be used to export and render high quality, vector based animations with complex shape and path curves, all with minimal file footprint.

## Usage

### Developing Animations in After Effects

For a detailed list of constraints for developing animations to use with the Keyframes library, please refer to the [Keyframes After Effects Guidelines](/docs/AfterEffectsGuideline.md).

### Image Data Extraction

Use of the extraction script requires an installation of **Adobe After Effects** as well as **Adobe ExtendScript Toolkit**.  If Keyframes JSON files are already available, only the corresponding iOS and Android libraries are needed.

For detailed steps on running the ExtendScript script on your AE comp, please refer to the instructions detailed [here](/Keyframes After Effects Scripts).

### iOS Rendering

#### Rendering Setup

Use the provided deserializers on the generated JSON blob from the **Image Data Extraction** step to create a `KFVector` model object.  If your JSON blob lives in the assets directory, this might look like:

```
NSString *filePath = [[NSBundle bundleForClass:[self class]] pathForResource:@"asset_name" ofType:@"json" inDirectory:nil];
NSData *data = [NSData dataWithContentsOfFile:filePath];
NSDictionary *vectorDictionary = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:nil];
KFVector *vector = KFVectorFromDictionary(vectorDictionary);
```

Then a `KFVectorLayer` can be created using this `KFVector`, `KFVectorLayer` can be used as normal `CALayer`.

```
KFVectorLayer *layer = [KFVectorLayer layer];
// Set a non-zero layer frame before setting the KFVector is required.
layer.frame = $AnyNonZeroCGRect$;
layer.faceModel = vector;
```

If you don't want the control of animation and prefer a `UIView`, `KFVectorView` can also be created using `KFVector`.
```
KFVectorView *view = [[KFVectorView alloc] initWithFrame:$AnyNonZeroCGRect$ faceVector:vector];
```

#### Play!
Use `startAnimation`, `pauseAnimation`, `resumeAnimation` and `seekToProgress` on `KFVectorLayer` to control the animation.

```
// Animation will start from beginning.
[layer startAnimation];

// Pause the animation at current progress.
[layer pauseAnimation];

// Resume the animation from where we paused last time.
[layer resumeAnimation];

// Seek to a given progress in range [0, 1]
[layer seekToProgress:0.5]; // seek to the mid point of the animation
```

### Android Rendering

#### Rendering setup

Use the provided deserializers on the generated JSON blob from the **Image Data Extraction** step to create a `KFImage` model object.  If your JSON blob lives in the assets directory, this might look like:
```
InputStream stream = getResources().getAssets().open("asset_name");
KFImage kfImage = KFImageDeserializer.deserialize(stream);
```
A KeyframesDrawable object can be created now using this `KFImage`, and this drawable can be used as a normal drawable.  It is highly recommended to use the software layer on any views displaying Keyframes animations.
```
Drawable kfDrawable = new KeyframesDrawableBuilder().withImage(kfImage).build();

ImageView imageView = (ImageView) findViewById(R.id.some_image_view);
imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
imageView.setImageDrawable(kfDrawable);
```

#### Play!
Use the start and stop animations on the drawable when appropriate to begin playback of the animation or end it after the end of the current loop.
```
// Starts a loop that progresses animation and invalidates the drawable.
kfDrawable.startAnimation();

// Stops the animation when the current animation ends.
kfDrawable.stopAnimationAtLoopEnd();
```

## Understanding Keyframes Model Objects

### **Image**

An `Image` in Keyframes consists of a number of important fields, which together, describe an animated and scalable image.  At the top level, an image contains information about how to scale (`canvas_size`) an image, as well as how to play back an animation at the correct speed (`frame_rate`, `frame_count`).  The animation itself is not bound to the discrete `frame_rate` that the image was extracted at, as the Keyframes rendering library supports fractional frames.  In addition to these global parameters of an `Image`, an `Image` also contains a number of `Feature`s, which describe different shapes to be drawn, as well as `Animation Group`s, which describe transforms that can be applied to multiple `Feature`s or even other `Animation Group`s at once.

Let's break down this simple image of a star against a circle scaling up and down.  The animation was exported at 24FPS, and the frame number is shown in the top left corner as well as the scale of the star on the bottom.
![Star, Real Time](/docs/images/doc-star-realtime.gif)

Let's slow that down a bit, frame by frame.

![Star, Slowwwww](/docs/images/doc-star-slow.gif)

### **Features**

A `Feature` is any independent visual object of the image.  Most important, it includes shape drawing commands, presented very similary to SVG type commands, which describe the `Feature`'s shape at any given time.  A `Feature` may belong to a larger `Animation Group`, as well as contain features specific animations of its own, including a specialized `STROKE_WIDTH` animation.  The shape of a `Feature` can also change over time, using the same `Keyframe` and `Interpolator` pattern described below.

##### Shape

A shape is any list of line drawing commands, which strung together, describe a continuous line or closed shape that can be filled or stroked.  The commands are given as a series of `Move`, `Line`, `Quadratic`, and `Cubic` commands, one after another.

Here are the important shapes for the above image, along with vertices (squares) and control points (circles), if relevant.

![Circle Shape](/docs/images/doc-circle-shape.png)
![Star Shape](/docs/images/doc-star-shape.png)

### **Animations**

##### Transforms

The Keyframes rendering library includes support for the common matrix based transform operations, `SCALE`, `ROTATE`, and `TRANSLATE`.  For `Feature`s specifically, an additional non-matrix `STROKE_WIDTH` is available.  `Animation`s may belong to specific `Feature`s, or as part of a larger `Animation Group`.

##### Animation Progression

The values of a transform of an animation and how they change during the playback of an animation are determined by two key fields, `Keyframe`s and `Timing Curves`.  Using the combination of the two fields, we can calculate back a value for a transform at any specified time in the animation.

**Keyframes** are specific frames an the animation that have specific target vlaues.  For example, in scaling a shape up and back down over 10 frames, we will want to start and end at 100% scale at the 0th and 10th frame, and hit our max scale of 150% on the 7th frame.  In this example, our key frames for the `SCALE` transform for this shape would be `[0, 7, 10]` with the values `[100%, 150%, 100%` respectively.

**Timing Curves** describe the pace with which a transform changes between each keyframe.  Each timing curve is modeled as a cubic bezier curve from point `[0, 0]` to `[1, 1]`, where the X value is the progression in time from the origin keyframe to the destination keyframe, and the Y value describes the amount of change in the value at a given time: `origValue + (destValue - origValue) * Y`.

For our scaling star image, the graph of scale change over time looks like this, with vertices and timing curve in/out values shown.

![Scale Animation Curve](/docs/images/doc-scale-curve.png)

### **Tying it all together**

With these fields from an `Image` object, as well as a progress value, we can build back all of the shapes of the `Image` at a given time, as well as any transformations to apply to the shapes, and draw back the `Image` at that frame.  Because the animations are driven by flexible `timing_curves`, a Keyframes `Image` is not limited to discrete integer frames for playback, but it is important to note that all progress values are given in relation to the frame count.  This means a 10 frame animation accepts all values in the range `[0..10]`.

## Contributing

See `~/CONTRIBUTING` for how to help us by improving this library!
