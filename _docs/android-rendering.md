---
docid: android-rendering
title: Android Rendering
layout: docs
permalink: /docs/android/rendering
---

#### Setup

Use the provided deserializers on the generated JSON blob from the **Image Data Extraction** step to create a `KFImage` model object.  If your JSON blob lives in the assets directory, this might look like:

```java
InputStream stream = getResources().getAssets().open("asset_name");
KFImage kfImage = KFImageDeserializer.deserialize(stream);
```

A KeyframesDrawable object can be created now using this `KFImage`, and this drawable can be used as a normal drawable.  It is highly recommended to use the software layer on any views displaying Keyframes animations.

```java
Drawable kfDrawable = new KeyframesDrawableBuilder().withImage(kfImage).build();

ImageView imageView = (ImageView) findViewById(R.id.some_image_view);
imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
imageView.setImageDrawable(kfDrawable);
imageView.setImageAlpha(0);
```

#### Play!
Use the start and stop animations on the drawable when appropriate to begin playback of the animation or end it after the end of the current loop.

```java
// Starts a loop that progresses animation from the beginning and invalidates the drawable.
kfDrawable.startAnimation();

// Pause the animation at current progress.
kfDrawable.pauseAnimation();

// Resume the animation from where we paused last time.
kfDrawable.resumeAnimation();

// Stops the animation.
kfDrawable.stopAnimation();

// Stops the animation when the current animation ends.
kfDrawable.stopAnimationAtLoopEnd();
```
