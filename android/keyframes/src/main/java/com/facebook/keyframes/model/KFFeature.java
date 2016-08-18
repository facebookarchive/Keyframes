/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.model;

import android.graphics.Matrix;

import java.util.List;

import com.facebook.keyframes.model.keyframedmodels.KeyFramedPath;
import com.facebook.keyframes.model.keyframedmodels.KeyFramedStrokeWidth;
import com.facebook.keyframes.util.ArgCheckUtil;
import com.facebook.keyframes.util.ListHelper;

/**
 * An object which describes one feature layer to be drawn.  This includes color, stroke, and
 * animation information for just this feature.  The shape is backed by a series of vector commands
 * that describe how to draw the feature as a path.
 */
public class KFFeature {

  /**
   * The name of this feature, for ease of identification.
   */
  public static final String NAME_JSON_FIELD = "name";
  private final String mName;

  /**
   * The fill color to use for this feature.
   */
  public static final String FILL_COLOR_JSON_FIELD = "fill_color";
  private final int mFillColor;

  /**
   * The stroke color to use for this feature.
   */
  public static final String STROKE_COLOR_JSON_FIELD = "stroke_color";
  private final int mStrokeColor;

  /**
   * The width of the stroke used if tracing the path.
   */
  public static final String STROKE_WIDTH_JSON_FIELD = "stroke_width";
  private final float mStrokeWidth;

  /**
   * A list of {@link KFFeatureFrame}s which holds information about how the path of this
   * feature changes throughout the duration of the animation.
   */
  public static final String KEY_FRAMES_JSON_FIELD = "key_frames";
  private final List<KFFeatureFrame> mKeyFrames;

  /**
   * Timing curves needed if mKeyFrames is present to describe how to animate between each key
   * frame.
   */
  public static final String TIMING_CURVES_JSON_FIELD = "timing_curves";
  private final float[][][] mTimingCurves;

  /**
   * The animation layer that this feature belongs to.  The final animation matrix of the group will
   * be applied to this feature, and any animations belonging to the feature will be nested within
   * this group.
   */
  public static final String ANIMATION_GROUP_JSON_FIELD = "animation_group";
  private final int mAnimationGroup;

  /**
   * A list of animations to apply to just this feature layer.
   */
  public static final String FEATURE_ANIMATIONS_JSON_FIELD = "feature_animations";
  /**
   * A KFAnimation just for the special cased stroke width animation.  Package private for testing.
   */
  final KFAnimation mStrokeWidthAnimation;
  /**
   * The remaining, matrix based animations from the feature_animations set.
   * Package private for testing.
   */
  final List<KFAnimation> mFeatureMatrixAnimations;

  /**
   * An optional effect that this feature layer can have.
   * Currently, only a simple linear gradient is supported.
   */
  public static final String EFFECT_JSON_FIELD = "effects";
  private final KFFeatureEffect mEffect;

  /**
   * EXPERIMENTAL optional "class" name used to reconfigure how this feature will render.
   * WARNING: May not be available on other platforms.
   */
  public static final String CLASS_NAME_JSON_FIELD = "class";
  private final String mClassName;

  /**
   * A post-processed object containing cached information for this path, if keyframed.
   */
  private final KeyFramedPath mKeyFramedPath;

  public static class Builder {
    public String name;
    public int fillColor;
    public int strokeColor;
    public float strokeWidth;
    public List<KFFeatureFrame> keyFrames;
    public float[][][] timingCurves;
    public int animationGroup;
    public List<KFAnimation> featureAnimations;
    public KFFeatureEffect effect;
    public String className;

    public KFFeature build() {
      return new KFFeature(
          name,
          fillColor,
          strokeColor,
          strokeWidth,
          keyFrames,
          timingCurves,
          animationGroup,
          featureAnimations,
          effect,
          className);
    }
  }

  public KFFeature(
      String name,
      int fillColor,
      int strokeColor,
      float strokeWidth,
      List<KFFeatureFrame> keyFrames,
      float[][][] timingCurves,
      int animationGroup,
      List<KFAnimation> featureAnimations,
      KFFeatureEffect effect,
      String className) {
    mName = name;
    mFillColor = fillColor;
    mStrokeColor = strokeColor;
    mStrokeWidth = strokeWidth;
    mKeyFrames = ListHelper.immutableOrEmpty(keyFrames);
    mTimingCurves = ArgCheckUtil.checkArg(
        timingCurves,
        ArgCheckUtil.checkTimingCurveObjectValidity(timingCurves, mKeyFrames.size()),
        TIMING_CURVES_JSON_FIELD);
    mAnimationGroup = animationGroup;

    mStrokeWidthAnimation = extractStrokeWidthFromAnimationSet(featureAnimations);
    ListHelper.sort(featureAnimations, KFAnimation.ANIMATION_PROPERTY_COMPARATOR);
    mFeatureMatrixAnimations = ListHelper.immutableOrEmpty(featureAnimations);
    mEffect = effect;
    mClassName = className;

    mKeyFramedPath = mKeyFrames.isEmpty() ? null : KeyFramedPath.fromFeature(this);
  }

  /**
   * Returns a KeyFramedStrokeWidth, if available.  This method modifies the list passed in by
   * removing the stroke width entry from the list.
   * @param animations The complete list of feature animations to extract stroke width from
   * @return a valid KeyFramedStrokeWidth, if found, or a no animation sentinel otherwise
   */
  private KFAnimation extractStrokeWidthFromAnimationSet(
      List<KFAnimation> animations) {
    if (animations == null) {
      return null;
    }
    int strokeWidthAnimationIndex = -1;
    for (int i = 0, len = animations.size(); i < len; i++) {
      if (animations.get(i).getPropertyType() == KFAnimation.PropertyType.STROKE_WIDTH) {
        // Only case is a stroke width animation, special to feature animation set.  Remove from the
        // set of matrix based animations and remember the index.
        strokeWidthAnimationIndex = i;
        break;
      }
    }
    if (strokeWidthAnimationIndex == -1) {
      return null;
    }
    return animations.remove(strokeWidthAnimationIndex);
  }

  public String getName() {
    return mName;
  }

  public int getFillColor() {
    return mFillColor;
  }

  public int getStrokeColor() {
    return mStrokeColor;
  }

  public List<KFFeatureFrame> getKeyFrames() {
    return mKeyFrames;
  }

  public float[][][] getTimingCurves() {
    return mTimingCurves;
  }

  public KeyFramedPath getPath() {
    return mKeyFramedPath;
  }

  public int getAnimationGroup() {
    return mAnimationGroup;
  }

  public void setStrokeWidth(
      KeyFramedStrokeWidth.StrokeWidth strokeWidth,
      float frameProgress) {
    if (strokeWidth == null) {
      return;
    }
    strokeWidth.setStrokeWidth(mStrokeWidth);
    if (mStrokeWidthAnimation == null) {
      return;
    }
    mStrokeWidthAnimation.getAnimation().apply(frameProgress, strokeWidth);
  }

  public void setAnimationMatrix(Matrix featureMatrix, float frameProgress) {
    if (featureMatrix == null) {
      return;
    }
    featureMatrix.reset();
    if (mFeatureMatrixAnimations == null) {
      return;
    }
    for (int i = 0, len = mFeatureMatrixAnimations.size(); i < len; i++) {
      mFeatureMatrixAnimations.get(i).getAnimation().apply(frameProgress, featureMatrix);
    }
  }

  public KFFeatureEffect getEffect() {
    return mEffect;
  }

  public String getConfigClassName() {
    return mClassName;
  }
}