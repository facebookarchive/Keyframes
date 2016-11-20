/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes;

import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import com.facebook.keyframes.model.KFAnimationGroup;
import com.facebook.keyframes.model.KFFeature;
import com.facebook.keyframes.model.KFGradient;
import com.facebook.keyframes.model.KFImage;
import com.facebook.keyframes.model.keyframedmodels.KeyFramedGradient;
import com.facebook.keyframes.model.keyframedmodels.KeyFramedOpacity;
import com.facebook.keyframes.model.keyframedmodels.KeyFramedPath;
import com.facebook.keyframes.model.keyframedmodels.KeyFramedStrokeWidth;
import com.facebook.keyframes.util.MatrixUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This object contains methods to calculate and cache the state of an image at a certain frame.
 */
public class KFImageStateProcessor {

  private static final float GRADIENT_PRECISION_PER_SECOND = 30;

  /**
   * The KFImage object this processor supports.
   */
  private final KFImage mKFImage;
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
   * A recyclable matrix that can be reused.
   */
  private final Matrix mRecyclableTransformMatrix = new Matrix();

  /**
   * Experimental for particle effects.
   */
  private final Map<String, FeatureConfig> mFeatureConfigs;

  KFImageStateProcessor(KFImage image, Map<String, FeatureConfig> featureconfigMap) {
    mKFImage = image;
    mFeatureConfigs = featureconfigMap;

    // Setup feature state list
    List<FeatureState> featureStateList = new ArrayList<>();
    for (int i = 0, len = mKFImage.getFeatures().size(); i < len; i++) {
      featureStateList.add(new FeatureState(mKFImage.getFeatures().get(i)));
    }
    mFeatureStateList = Collections.unmodifiableList(featureStateList);

    // Setup animation layers
    mAnimationGroupMatrices = new SparseArray<>();
    List<KFAnimationGroup> animationGroups = mKFImage.getAnimationGroups();
    for (int i = 0, len = animationGroups.size(); i < len; i++) {
      mAnimationGroupMatrices.put(animationGroups.get(i).getGroupId(), new Matrix());
    }
  }

  public void calculateAndSetFrameProgress(float frameProgress) {
    mKFImage.setAnimationMatrices(mAnimationGroupMatrices, frameProgress);
    for (int i = 0, len = mFeatureStateList.size(); i < len; i++) {
      mFeatureStateList.get(i).setupFeatureStateForProgress(frameProgress);
    }
  }

  public List<FeatureState> getFeatureStateList() {
    return mFeatureStateList;
  }

  class FeatureState {
    private final KFFeature mFeature;

    // Reuseable modifiable objects for drawing
    private final KFPath mPath;
    private final KFPath mFeatureMaskPath;
    private final KeyFramedStrokeWidth.StrokeWidth mStrokeWidth;
    private final KeyFramedOpacity.Opacity mOpacity;
    private final Matrix mFeatureMatrix;
    private final Matrix mFeatureMaskMatrix;

    private boolean mIsVisible;

    public Matrix getUniqueFeatureMatrix() {
      if (mFeatureMatrix == mRecyclableTransformMatrix) {
        // Don't return a matrix unless it's known to be unique for this feature
        return null;
      }
      return mFeatureMatrix;
    }

    // Cached shader vars
    private Shader[] mCachedShaders;
    private Shader mCurrentShader;

    public FeatureState(KFFeature feature) {
      mFeature = feature;
      if (hasCustomDrawable()) {
        mPath = null;
        mStrokeWidth = null;
        // Bitmap features use the matrix later in draw()
        // so there's no way to reuse a globally cached matrix
        mFeatureMatrix = new Matrix();
      } else {
        mPath = new KFPath();
        mStrokeWidth = new KeyFramedStrokeWidth.StrokeWidth();
        // Path features use the matrix immediately
        // so there's no need to waste memory with a unique copy
        mFeatureMatrix = mRecyclableTransformMatrix;
      }
      mOpacity = new KeyFramedOpacity.Opacity();
      if (mFeature.getFeatureMask() != null) {
        mFeatureMaskPath = new KFPath();
        mFeatureMaskMatrix = new Matrix();
      } else {
        mFeatureMaskPath = null;
        mFeatureMaskMatrix = null;
      }
      assert mFeatureMatrix != null;
    }

    public void setupFeatureStateForProgress(float frameProgress) {
      if (frameProgress < mFeature.getFromFrame() || frameProgress > mFeature.getToFrame()) {
        mIsVisible = false;
        return;
      }
      mIsVisible = true;
      mFeature.setAnimationMatrix(mFeatureMatrix, frameProgress);
      Matrix layerTransformMatrix = mAnimationGroupMatrices.get(mFeature.getAnimationGroup());

      if (layerTransformMatrix != null && !layerTransformMatrix.isIdentity()) {
        mFeatureMatrix.postConcat(layerTransformMatrix);
      }
      KeyFramedPath path = mFeature.getPath();
      if (hasCustomDrawable() || path == null) {
        return; // skip all the path stuff
      }
      mPath.reset();
      path.apply(frameProgress, mPath);
      mPath.transform(mFeatureMatrix);

      mFeature.setStrokeWidth(mStrokeWidth, frameProgress);
      mStrokeWidth.adjustScale(MatrixUtils.extractScaleFromMatrix(mFeatureMatrix));
      mFeature.setOpacity(mOpacity, frameProgress);
      if (mFeature.getEffect() != null) {
        prepareShadersForFeature(mFeature);
      }
      mCurrentShader = getNearestShaderForFeature(frameProgress);

      if (mFeature.getFeatureMask() != null) {
        mFeature.getFeatureMask().setAnimationMatrix(mFeatureMaskMatrix, frameProgress);
        mFeatureMaskPath.reset();
        mFeature.getFeatureMask().getPath().apply(frameProgress, mFeatureMaskPath);
        mFeatureMaskPath.transform(mFeatureMaskMatrix);
      }
    }

    public KFPath getCurrentPathForDrawing() {
      return mPath;
    }

    public KFPath getCurrentMaskPath() {
      return mFeatureMaskPath;
    }

    public float getStrokeWidth() {
      return mStrokeWidth != null ? mStrokeWidth.getStrokeWidth() : 0;
    }

    public float getOpacity() {
      return mOpacity.getOpacity() / 100;
    }

    public int getAlpha() {
      return Math.round(0xFF * getOpacity());
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

    public Paint.Cap getStrokeLineCap() {
      return mFeature.getStrokeLineCap();
    }

    public boolean isVisible() {
      return mIsVisible;
    }

    private void prepareShadersForFeature(KFFeature feature) {
      if (mCachedShaders != null) {
        return;
      }

      int frameRate = mKFImage.getFrameRate();
      int numFrames = mKFImage.getFrameCount();
      int precision = Math.round(GRADIENT_PRECISION_PER_SECOND * numFrames / frameRate);
      mCachedShaders = new LinearGradient[precision + 1];
      float progress;
      KeyFramedGradient.GradientColorPair colorPair = new KeyFramedGradient.GradientColorPair();
      KFGradient gradient = feature.getEffect().getGradient();
      for (int i = 0; i < precision; i++) {
        progress = i / (float) (precision) * numFrames;
        gradient.getStartGradient().apply(progress, colorPair);
        gradient.getEndGradient().apply(progress, colorPair);
        mCachedShaders[i] = new LinearGradient(
            0,
            0,
            0,
            mKFImage.getCanvasSize()[1],
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
          (int) ((frameProgress / mKFImage.getFrameCount()) * (mCachedShaders.length - 1));
      return mCachedShaders[shaderIndex];
    }

    public final FeatureConfig getConfig() {
      if (mFeatureConfigs == null) return null;
      return mFeatureConfigs.get(mFeature.getConfigClassName());
    }

    private boolean hasCustomDrawable() {
      final FeatureConfig config = getConfig();
      return config != null && config.drawable != null;
    }
  }

  /**
   * Config options define runtime overrides for specific KFFeature behaviors
   */
  public static class FeatureConfig {
    final Drawable drawable;
    final Matrix matrix;

    public FeatureConfig(Drawable drawable, Matrix matrix) {
      this.matrix = matrix;
      this.drawable = drawable;

      if (BuildConfig.DEBUG && drawable != null) {
        final Rect bounds = drawable.getBounds();
        if (bounds.width() <= 0 || bounds.height() <= 0) {
          throw new IllegalStateException("KeyframesDrawable FeatureConfig Drawable must have bounds set");
        }
      }
    }
  }
}
