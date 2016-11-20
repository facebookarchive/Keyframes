/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import java.lang.ref.WeakReference;
import java.util.Collections;

import com.facebook.keyframes.model.KFImage;

/**
 * This drawable will render a KFImage model by painting paths to the supplied canvas in
 * {@link #draw(Canvas)}.  There are methods to begin and end animation playback here, which need to
 * be managed carefully so as not to leave animation callbacks running indefinitely.  At each
 * animation callback, the next frame's matrices and paths are calculated and the drawable is then
 * invalidated.
 */
public class KeyframesDrawable extends Drawable
        implements KeyframesDrawableAnimationCallback.FrameListener, KeyframesDirectionallyScalingDrawable {

  /**
   * The KFImage object to render.
   */
  private final KFImage mKFImage;
  /**
   * A {@link KFFeatureDrawer}, for drawing all features to the canvas.
   */
  private final KFFeatureDrawer mKFFeatureDrawer = new KFFeatureDrawer();
  /**
   * A {@link KFImageStateProcessor}, for calculating frames of the KFImage.
   */
  private final KFImageStateProcessor mKFImageStateProcessor;
  /**
   * The animation callback object used to start and stop the animation.
   */
  private final KeyframesDrawableAnimationCallback mKeyframesDrawableAnimationCallback;

  /**
   * The scale matrix to be applied for the final size of this drawable.
   */
  private final Matrix mScaleMatrix;

  /**
   * The currently set width and height of this drawable.
   */
  private int mSetWidth;
  private int mSetHeight;
  /**
   * The X and Y scales to be used, calculated from the set dimensions compared with the exported
   * canvas size of the image.
   */
  private float mScale;
  private float mScaleFromCenter;
  private float mScaleFromEnd;


  private boolean mHasInitialized = false;

  /**
   * Create a new KeyframesDrawable with the supplied values from the builder.
   * @param builder
   */
  KeyframesDrawable(KeyframesDrawableBuilder builder) {
    mKFImage = builder.getImage();
    mKFImageStateProcessor = new KFImageStateProcessor(
        mKFImage,
        builder.getExperimentalFeatures().getParticleFeatureConfigs() == null ?
            null :
            Collections.unmodifiableMap(
                builder.getExperimentalFeatures().getParticleFeatureConfigs()));

    mScaleMatrix = new Matrix();
    mKeyframesDrawableAnimationCallback = KeyframesDrawableAnimationCallback.create(this, mKFImage);

    setMaxFrameRate(builder.getMaxFrameRate());
  }

  /**
   * Sets the bounds of this drawable.  Here, we calculate values needed to scale the image from the
   * size it was when exported to a size to be drawn on the Android canvas.
   */
  @Override
  public void setBounds(int left, int top, int right, int bottom) {
    super.setBounds(left, top, right, bottom);
    mSetWidth = right - left;
    mSetHeight = bottom - top;

    float idealXScale = (float) mSetWidth / mKFImage.getCanvasSize()[0];
    float idealYScale = (float) mSetHeight / mKFImage.getCanvasSize()[1];

    mScale = Math.min(idealXScale, idealYScale);
    calculateScaleMatrix(1, 1, ScaleDirection.UP);
    if (!mHasInitialized) {
      // Call this at least once or else nothing will render. But if this is called this every time
      // setBounds is called then the animation will reset when resizing.
      setFrameProgress(0);
    }
  }

  @Override
  public void setDirectionalScale(
          float scaleFromCenter,
          float scaleFromEnd,
          ScaleDirection direction) {
    calculateScaleMatrix(scaleFromCenter, scaleFromEnd, direction);
  }

  /**
   * Iterates over the current state of mPathsForDrawing and draws each path, applying properties
   * of the feature to a recycled Paint object.
   */
  @Override
  public void draw(Canvas canvas) {
    Rect currBounds = getBounds();
    canvas.translate(currBounds.left, currBounds.top);
    mKFFeatureDrawer.drawFeaturesToCanvas(
        canvas,
        mKFImageStateProcessor.getFeatureStateList(),
        mScaleMatrix);
    canvas.translate(-currBounds.left, -currBounds.top);
  }

  /**
   * Unsupported for now
   */
  @Override
  public void setAlpha(int alpha) {
  }

  /**
   * Unsupported for now
   */
  @Override
  public void setColorFilter(ColorFilter cf) {
  }

  /**
   * Unsupported for now
   */
  @Override
  public int getOpacity() {
    return PixelFormat.OPAQUE;
  }

  /**
   * Starts the animation callbacks for this drawable.  A corresponding call to
   * {@link #stopAnimationAtLoopEnd()} or {@link #stopAnimation()} needs to be called eventually,
   * or the callback will continue to post callbacks for this drawable indefinitely.
   */
  public void startAnimation() {
    mKeyframesDrawableAnimationCallback.start();
  }

  /**
   * Stops the animation callbacks for this drawable immediately.
   */
  public void stopAnimation() {
    mKeyframesDrawableAnimationCallback.stop();
  }

  /**
   * Finishes the current playthrough of the animation and stops animating this drawable afterwards.
   */
  public void stopAnimationAtLoopEnd() {
    mKeyframesDrawableAnimationCallback.stopAtLoopEnd();
  }

  /**
   * Given a progress in terms of frames, calculates each of the paths needed to be drawn in
   * {@link #draw(Canvas)}.
   */
  public void setFrameProgress(float frameProgress) {
    mHasInitialized = true;
    mKFImageStateProcessor.calculateAndSetFrameProgress(frameProgress);
  }

  /**
   * The callback used to update the frame progress of this drawable.  This leads to a recalculation
   * of the paths that need to be drawn before the Drawable invalidates itself.
   */
  @Override
  public void onProgressUpdate(float frameProgress) {
    setFrameProgress(frameProgress);
    invalidateSelf();
  }

  @Override
  public void onStop() {
    if (mOnAnimationEnd == null) {
      return;
    }
    final OnAnimationEnd onAnimationEnd = mOnAnimationEnd.get();
    if (onAnimationEnd == null) {
      return;
    }
    onAnimationEnd.onAnimationEnd();
    mOnAnimationEnd.clear();
  }

  private WeakReference<OnAnimationEnd> mOnAnimationEnd;

  public void setAnimationListener(OnAnimationEnd listener) {
    mOnAnimationEnd = new WeakReference<>(listener);
  }

  private void calculateScaleMatrix(
          float scaleFromCenter,
          float scaleFromEnd,
          ScaleDirection scaleDirection) {
    if (mScaleFromCenter == scaleFromCenter &&
            mScaleFromEnd == scaleFromEnd) {
      return;
    }

    mScaleMatrix.setScale(mScale, mScale);
    if (scaleFromCenter == 1 && scaleFromEnd == 1) {
      mScaleFromCenter = 1;
      mScaleFromEnd = 1;
      return;
    }

    float scaleYPoint = scaleDirection == ScaleDirection.UP ? mSetHeight : 0;
    mScaleMatrix.postScale(scaleFromCenter, scaleFromCenter, mSetWidth / 2, mSetHeight / 2);
    mScaleMatrix.postScale(scaleFromEnd, scaleFromEnd, mSetWidth / 2, scaleYPoint);

    mScaleFromCenter = scaleFromCenter;
    mScaleFromEnd = scaleFromEnd;
  }

  /**
   * Cap the frame rate to a specific FPS. Consider using this for low end devices.
   * Calls {@link KeyframesDrawableAnimationCallback#setMaxFrameRate}
   * @param maxFrameRate
   */
  public void setMaxFrameRate(int maxFrameRate) {
    mKeyframesDrawableAnimationCallback.setMaxFrameRate(maxFrameRate);
  }



  public interface OnAnimationEnd {
    void onAnimationEnd();
  }
}
