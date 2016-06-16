// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import com.facebook.keyframes.data.ReactionsFace;
import com.facebook.keyframes.data.ReactionsFeature;
import com.facebook.keyframes.data.keyframedmodels.KeyFramedPath;

/**
 * This drawable will render a ReactionsFace model by painting paths to the supplied canvas in
 * {@link #draw(Canvas)}.  There are methods to begin and end animation playback here, which need to
 * be managed carefully so as not to leave animation callbacks running indefinitely.  At each
 * animation callback, the next frame's matrices and paths are calculated and the drawable is then
 * invalidated.
 */
public class ReactionsFaceDrawable extends Drawable
    implements ReactionsFaceAnimationCallback.FrameListener, ReactionsScalingDrawable {

  /**
   * The ReactionsFace object to render.
   */
  private final ReactionsFace mReactionsFace;
  /**
   * A recyclable {@link Paint} object used to draw all of the shapes.
   */
  private final Paint mDrawingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  /**
   * The base feature paths for this {@link ReactionsFace}.
   */
  private final List<KeyFramedPath> mBaseFeaturePaths;
  /**
   * The actual paths to be used for drawing each corresponding BaseFeaturePath.  The paths in this
   * array are calculated for the current animation progress and have their animation layer matrices
   * applied.
   */
  private final List<Path> mPathsForDrawing;
  /**
   * The animation callback object used to start and stop the animation.
   */
  private final ReactionsFaceAnimationCallback mReactionsFaceAnimationCallback;
  /**
   * A recyclable matrix that can be used to transform paths.
   */
  private final Matrix mRecyclableTransformMatrix;
  /**
   * The scale matrix to be applied for the final size of this drawable.
   * TODO: markpeng this probably needs to be converted to a new helper "setBoundsF" method, since
   * currently it draws outside of set bounds which isn't really conforming to Android API.  This
   * should get rid of mInverseScaleMatrix and setScaleForDrawing.
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
  /**
   * TODO markpeng need to move this somewhere where it's mapped directly to the feature.
   */
  private Shader mAngerFaceShader;

  public ReactionsFaceDrawable(ReactionsFace reactionsFace) {
    mReactionsFace = reactionsFace;
    mDrawingPaint.setStrokeCap(Paint.Cap.ROUND);
    List<KeyFramedPath> baseFeaturePaths = new ArrayList<>();
    List<Path> pathsForDrawing = new ArrayList<>();
    for (int i = 0, len = mReactionsFace.getFeatures().size(); i < len; i++) {
      baseFeaturePaths.add(mReactionsFace.getFeatures().get(i).getPath());
      pathsForDrawing.add(new Path());
    }
    mBaseFeaturePaths = Collections.unmodifiableList(baseFeaturePaths);
    mPathsForDrawing = Collections.unmodifiableList(pathsForDrawing);
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

  /**
   * TODO: markpeng probably remove this and replace with a setBoundsF
   */
  @Override
  public void setScaleForDrawing(
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
    ReactionsFeature feature;
    Path pathToDraw;
    for (int i = 0; i < mReactionsFace.getFeatures().size(); i++) {
      feature = mReactionsFace.getFeatures().get(i);
      pathToDraw = mPathsForDrawing.get(i);
      mDrawingPaint.setShader(null);
      if (feature.getFillColor() != Color.TRANSPARENT) {
        mDrawingPaint.setStyle(Paint.Style.FILL);
        if (feature.getEffect() == null) {
          mDrawingPaint.setColor(feature.getFillColor());
          pathToDraw.transform(mScaleMatrix);
          canvas.drawPath(pathToDraw, mDrawingPaint);
          pathToDraw.transform(mInverseScaleMatrix);
        } else {
          mDrawingPaint.setShader(mAngerFaceShader);
          canvas.concat(mScaleMatrix);
          canvas.drawPath(pathToDraw, mDrawingPaint);
          canvas.concat(mInverseScaleMatrix);
        }
      }
      if (feature.getStrokeColor() != Color.TRANSPARENT) {
        mDrawingPaint.setColor(feature.getStrokeColor());
        mDrawingPaint.setStyle(Paint.Style.STROKE);
        mDrawingPaint.setStrokeWidth(
                feature.getStrokeWidth() * mXScale * mScaleFromCenter * mScaleFromEnd);
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
    mReactionsFace.buildAnimationMatrices(frameProgress);
    Path pathForDrawing;
    ReactionsFeature feature;
    Matrix layerTransformMatrix;
    for (int i = 0; i < mReactionsFace.getFeatures().size(); i++) {
      feature = mReactionsFace.getFeatures().get(i);
      layerTransformMatrix = mReactionsFace.getAnimationMatrix(feature.getAnimationGroup());

      if (layerTransformMatrix != null) {
        mRecyclableTransformMatrix.set(layerTransformMatrix);
      } else {
        mRecyclableTransformMatrix.reset();
      }
      mRecyclableTransformMatrix.preConcat(feature.getAnimationMatrix(frameProgress));

      pathForDrawing = mPathsForDrawing.get(i);
      pathForDrawing.reset();
      mBaseFeaturePaths.get(i).apply(frameProgress, pathForDrawing);
      pathForDrawing.transform(mRecyclableTransformMatrix);
      feature.calculateStrokeWidth(frameProgress);
      if (feature.getEffect() != null) {
        feature.getEffect().getGradient().prepareShaders(
            mReactionsFace.getCanvasSize()[1],
            mReactionsFace.getFrameRate(),
            mReactionsFace.getFrameCount());
        mAngerFaceShader =
            feature.getEffect().getGradient().getNearestShaderForFrame(frameProgress);
      }
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

  /**
   * TODO markpeng maybe remove this and replace with setBoundsF
   */
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
}
