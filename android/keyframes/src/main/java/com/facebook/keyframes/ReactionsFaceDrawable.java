// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import com.facebook.keyframes.data.ReactionsAnimationGroup;
import com.facebook.keyframes.data.ReactionsFace;
import com.facebook.keyframes.data.ReactionsFeature;
import com.facebook.keyframes.data.ReactionsGradient;
import com.facebook.keyframes.data.keyframedmodels.KeyFramedGradient;
import com.facebook.keyframes.data.keyframedmodels.KeyFramedStrokeWidth;

/**
 * This drawable will render a ReactionsFace model by painting paths to the supplied canvas in
 * {@link #draw(Canvas)}.  There are methods to begin and end animation playback here, which need to
 * be managed carefully so as not to leave animation callbacks running indefinitely.  At each
 * animation callback, the next frame's matrices and paths are calculated and the drawable is then
 * invalidated.
 */
public class ReactionsFaceDrawable extends Drawable
        implements ReactionsFaceAnimationCallback.FrameListener, ReactionsScalingDrawable {

  private static final float GRADIENT_PRECISION_PER_SECOND = 30;

  /**
   * The ReactionsFace object to render.
   */
  private final ReactionsFace mReactionsFace;
  /**
   * A recyclable {@link Paint} object used to draw all of the shapes.
   */
  private final Paint mDrawingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  /**
   * The list of all {@link FeatureState}s, containing all information needed to render a feature
   * for the current progress of animation.
   */
  private final List<FeatureState> mFeatureStateList;
  /**
   * The current state of animation layer matrices for this animation, keyed by animation group id.
   */
  private final SparseArray<Matrix> mAnimationGroupMatrices;
  /**
   * The animation callback object used to start and stop the animation.
   */
  private final ReactionsFaceAnimationCallback mReactionsFaceAnimationCallback;
  /**
   * A recyclable matrix that can be reused.
   */
  private final Matrix mRecyclableTransformMatrix;

  /**
   * The scale matrix to be applied for the final size of this drawable.
   */
  private final Matrix mScaleMatrix;
  private final Matrix mInverseScaleMatrix;

  /**
   * The currently set width and height of this drawable.
   */
  private int mSetWidth;
  private int mSetHeight;
  /**
   * The X and Y scales to be used, calculated from the set dimensions compared with the exported
   * canvas size of the image.
   */
  private float mXScale;
  private float mYScale;
  /**
   * See {@link mScaleMatrix} comment about removing these.
   */
  private float mScaleFromCenter;
  private float mScaleFromEnd;

  public ReactionsFaceDrawable(ReactionsFace reactionsFace) {
    mReactionsFace = reactionsFace;
    mDrawingPaint.setStrokeCap(Paint.Cap.ROUND);

    // Setup feature state list
    List<FeatureState> featureStateList = new ArrayList<>();
    for (int i = 0, len = mReactionsFace.getFeatures().size(); i < len; i++) {
      featureStateList.add(new FeatureState(mReactionsFace.getFeatures().get(i)));
    }
    mFeatureStateList = Collections.unmodifiableList(featureStateList);

    // Setup animation layers
    mAnimationGroupMatrices = new SparseArray<>();
    List<ReactionsAnimationGroup> animationGroups = mReactionsFace.getAnimationGroups();
    for (int i = 0, len = animationGroups.size(); i < len; i++) {
      mAnimationGroupMatrices.put(animationGroups.get(i).getGroupId(), new Matrix());
    }

    mReactionsFaceAnimationCallback = ReactionsFaceAnimationCallback.create(this, mReactionsFace);
    mRecyclableTransformMatrix = new Matrix();
    mScaleMatrix = new Matrix();
    mInverseScaleMatrix = new Matrix();
  }

  /**
   * Sets the bounds of this drawable.  Here, we calculate vlaues needed to scale the image from the
   * size it was when exported to a size to be drawn on the Android canvas.
   */
  @Override
  public void setBounds(int left, int top, int right, int bottom) {
    super.setBounds(left, top, right, bottom);
    mSetWidth = right - left;
    mSetHeight = bottom - top;

    mXScale = (float) mSetWidth / mReactionsFace.getCanvasSize()[0];
    mYScale = (float) mSetWidth / mReactionsFace.getCanvasSize()[1];
    calculatePathsForProgress(0);
    calculateScaleMatrix(1, 1, ScaleDirection.UP);
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
   * of the shape to a recycled Paint object.
   */
  @Override
  public void draw(Canvas canvas) {
    Rect currBounds = getBounds();
    canvas.translate(currBounds.left, currBounds.top);
    Path pathToDraw;
    FeatureState featureState;
    for (int i = 0, len = mFeatureStateList.size(); i < len; i++) {
      featureState = mFeatureStateList.get(i);
      pathToDraw = featureState.getCurrentPathForDrawing();
      mDrawingPaint.setShader(null);
      if (featureState.getFillColor() != Color.TRANSPARENT) {
        mDrawingPaint.setStyle(Paint.Style.FILL);
        if (featureState.getCurrentShader() == null) {
          mDrawingPaint.setColor(featureState.getFillColor());
          pathToDraw.transform(mScaleMatrix);
          canvas.drawPath(pathToDraw, mDrawingPaint);
          pathToDraw.transform(mInverseScaleMatrix);
        } else {
          mDrawingPaint.setShader(featureState.getCurrentShader());
          canvas.concat(mScaleMatrix);
          canvas.drawPath(pathToDraw, mDrawingPaint);
          canvas.concat(mInverseScaleMatrix);
        }
      }
      if (featureState.getStrokeColor() != Color.TRANSPARENT) {
        mDrawingPaint.setColor(featureState.getStrokeColor());
        mDrawingPaint.setStyle(Paint.Style.STROKE);
        mDrawingPaint.setStrokeWidth(
                featureState.getStrokeWidth() * mXScale * mScaleFromCenter * mScaleFromEnd);
        pathToDraw.transform(mScaleMatrix);
        canvas.drawPath(pathToDraw, mDrawingPaint);
        pathToDraw.transform(mInverseScaleMatrix);
      }
    }
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
   * {@link #stopAnimationAtLoopEnd()} needs to be called eventually, or the callback will continue
   * to post callbacks for this drawable indefinitely.
   */
  public void startAnimation() {
    mReactionsFaceAnimationCallback.start();
  }

  /**
   * Finishes the current playthrough of the animation and stops animating this drawable afterwards.
   */
  public void stopAnimationAtLoopEnd() {
    mReactionsFaceAnimationCallback.stopAtLoopEnd();
  }

  /**
   * Given a progress in terms of frames, calculates each of the paths needed to be drawn in
   * {@link #draw(Canvas)}.
   */
  private void calculatePathsForProgress(float frameProgress) {
    mReactionsFace.setAnimationMatrices(mAnimationGroupMatrices, frameProgress);
    for (int i = 0, len = mFeatureStateList.size(); i < len; i++) {
      mFeatureStateList.get(i).setupFeatureStateForProgress(frameProgress);
    }
  }

  /**
   * The callback used to update the frame progress of this drawable.  This leads to a recalculation
   * of the paths that need to be drawn before the Drawable invalidates itself.
   */
  @Override
  public void onProgressUpdate(float frameProgress) {
    calculatePathsForProgress(frameProgress);
    invalidateSelf();
  }

  private void calculateScaleMatrix(
          float scaleFromCenter,
          float scaleFromEnd,
          ScaleDirection scaleDirection) {
    if (mScaleFromCenter == scaleFromCenter &&
            mScaleFromEnd == scaleFromEnd) {
      return;
    }

    mScaleMatrix.setScale(mXScale, mYScale);
    if (scaleFromCenter == 1 && scaleFromEnd == 1) {
      mScaleFromCenter = 1;
      mScaleFromEnd = 1;
      mScaleMatrix.invert(mInverseScaleMatrix);
      return;
    }

    float scaleYPoint = scaleDirection == ScaleDirection.UP ? mSetHeight : 0;
    mScaleMatrix.postScale(scaleFromCenter, scaleFromCenter, mSetWidth / 2, mSetHeight / 2);
    mScaleMatrix.postScale(scaleFromEnd, scaleFromEnd, mSetWidth / 2, scaleYPoint);

    mScaleFromCenter = scaleFromCenter;
    mScaleFromEnd = scaleFromEnd;
    mScaleMatrix.invert(mInverseScaleMatrix);
  }

  private class FeatureState {
    private final ReactionsFeature mFeature;

    // Reuseable modifiable objects for drawing
    private final Path mPath = new Path();
    private final KeyFramedStrokeWidth.StrokeWidth mStrokeWidth =
            new KeyFramedStrokeWidth.StrokeWidth();

    // Cached shader vars
    private Shader[] mCachedShaders;
    private Shader mCurrentShader;

    public FeatureState(ReactionsFeature feature) {
      mFeature = feature;
    }

    public void setupFeatureStateForProgress(float frameProgress) {
      mFeature.setAnimationMatrix(mRecyclableTransformMatrix, frameProgress);
      Matrix layerTransformMatrix = mAnimationGroupMatrices.get(mFeature.getAnimationGroup());

      if (layerTransformMatrix != null && !layerTransformMatrix.isIdentity()) {
        mRecyclableTransformMatrix.postConcat(layerTransformMatrix);
      }
      mPath.reset();
      mFeature.getPath().apply(frameProgress, mPath);
      mPath.transform(mRecyclableTransformMatrix);

      mFeature.setStrokeWidth(mStrokeWidth, frameProgress);
      if (mFeature.getEffect() != null) {
        prepareShadersForFeature(mFeature);
      }
      mCurrentShader = getNearestShaderForFeature(frameProgress);
    }

    public Path getCurrentPathForDrawing() {
      return mPath;
    }

    public float getStrokeWidth() {
      return mStrokeWidth.getStrokeWidth();
    }

    public Shader getCurrentShader() {
      return mCurrentShader;
    }

    public int getStrokeColor() {
      return mFeature.getStrokeColor();
    }

    public int getFillColor() {
      return mFeature.getFillColor();
    }

    private void prepareShadersForFeature(ReactionsFeature feature) {
      if (mCachedShaders != null) {
        return;
      }

      int frameRate = mReactionsFace.getFrameRate();
      int numFrames = mReactionsFace.getFrameCount();
      int precision = Math.round(GRADIENT_PRECISION_PER_SECOND * numFrames / frameRate);
      mCachedShaders = new LinearGradient[precision + 1];
      float progress;
      KeyFramedGradient.GradientColorPair colorPair = new KeyFramedGradient.GradientColorPair();
      ReactionsGradient gradient = feature.getEffect().getGradient();
      for (int i = 0; i < precision; i++) {
        progress = i / (float) (precision) * numFrames;
        gradient.getStartGradient().apply(progress, colorPair);
        gradient.getEndGradient().apply(progress, colorPair);
        mCachedShaders[i] = new LinearGradient(
                0,
                0,
                0,
                mReactionsFace.getCanvasSize()[1],
                colorPair.getStartColor(),
                colorPair.getEndColor(),
                Shader.TileMode.CLAMP);
      }
    }

    public Shader getNearestShaderForFeature(float frameProgress) {
      if (mCachedShaders == null) {
        return null;
      }
      int shaderIndex =
              (int) ((frameProgress / mReactionsFace.getFrameCount()) * (mCachedShaders.length - 1));
      return mCachedShaders[shaderIndex];
    }
  }
}