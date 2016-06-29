# [Logo] Keyframes

Keyframes is a combination of (1) an ExtendScript script that extracts image animation data from an After Effects file and (2) a corresponding rendering library for Android and iOS.  Keyframes can be used to export and render high quality, vector based animations with complex shape and path curves, all with minimal file footprint.

## Usage

### Image Data Extraction

Use of the extraction script requires an installation of **Adobe After Effects** as well as **Adobe ExtendScript Toolkit**.  If Keyframes JSON files are already available, only the corresponding iOS and Android libraries are needed.

Simply have the project open in After Effects, open the extraction script provided in `~/scripts` in ExtendScript, and run the script.  The script will iterate through the compositions in the project and output a JSON blob for each composition with the necessary metadata to reconstruct the images.

### iOS Rendering

*Halp, Sean!*

### Android Rendering

#### Download

Download [the latest JAR](link to github releases) or grab via Gradle:
```
compile 'com.facebook.keyframes:keyframes:1.0'
```
or Maven:
```
<dependency>
  <groupId>com.facebook.keyframes</groupId>
  <artifactId>keyframes</artifactId>
  <version>1.0</version>
</dependency>
```

#### Rendering setup

Use the provided deserializers on the generated JSON blob from the **Image Data Extraction** step to create a `KFImage` model object.  If your JSON blob lives in the assets directory, this might look like:
```
InputStream stream = getResources().getAssets().open("asset_name");
KFImage kfImage = KFImageDeserializer.deserialize(stream);
```
A KeyframesDrawable object can be created now using this `KFImage`, and this drawable can be used as a normal drawable.
```
Drawable kfDrawable = new KeyframesDrawable(kfImage);

ImageView imageView = (ImageView) findViewById(R.id.some_image_view);
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

## Understanding Keyframes
Some explanation with pretty images of shape (vector commands), animation (SRT), Keyframes and influence (keyframe, inTangent, outTangent), and how they all tie together.

## Troubleshooting

Various issues people might run into?  I remember some issue with setting up extendscript settings to allow output, but can't find it right now.

## Contributing

See `~/CONTRIBUTING` for how to help us by improving this library!

## License

Keyframes is BSD-licensed.  Additional information can be found at `~/LICENSE`