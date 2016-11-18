/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.model;

import android.graphics.Matrix;
import android.graphics.Paint;

import java.util.List;

import com.facebook.keyframes.model.keyframedmodels.KeyFramedAnchorPoint;
import com.facebook.keyframes.model.keyframedmodels.KeyFramedPath;
import com.facebook.keyframes.model.keyframedmodels.KeyFramedStrokeWidth;
import com.facebook.keyframes.util.AnimationHelper;
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
   * The frame that this feature stops showing up.
   */
  public static final String FROM_FRAME_JSON_FIELD = "from_frame";
  private final float mFromFrame;

  /**
   * The frame that this feature starts showing up.
   */
  public static final String TO_FRAME_JSON_FIELD = "to_frame";
  private final float mToFrame;

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
   * The cap the use at the ends of stroke lines.
   */
  public static final String STROKE_LINE_CAP_JSON_FIELD = "stroke_line_cap";
  private final Paint.Cap mStrokeLineCap;

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
   * The anchor point for all animations in this feature.
   */
  final KFAnimation mAnchorPoint;

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
    public float fromFrame = 0;
    public float toFrame = Float.MAX_VALUE;
    public List<KFFeatureFrame> keyFrames;
    public float[][][] timingCurves;
    public int animationGroup;
    public Paint.Cap strokeLineCap = Paint.Cap.ROUND;
    public List<KFAnimation> featureAnimations;
    public float[] anchorPoint;
    public KFFeatureEffect effect;
    public String className;

    public KFFeature build() {
      return new KFFeature(
          name,
          fillColor,
          strokeColor,
          strokeWidth,
          fromFrame,
          toFrame,
          keyFrames,
          timingCurves,
          animationGroup,
          strokeLineCap,
          featureAnimations,
          anchorPoint,
          effect,
          className);
    }
  }

  public KFFeature(
      String name,
      int fillColor,
      int strokeColor,
      float strokeWidth,
      float fromFrame,
      float toFrame,
      List<KFFeatureFrame> keyFrames,
      float[][][] timingCurves,
      int animationGroup,
      Paint.Cap strokeLineCap,
      List<KFAnimation> featureAnimations,
      float[] anchorPoint,
      KFFeatureEffect effect,
      String className) {
    mName = name;
    mFillColor = fillColor;
    mStrokeColor = strokeColor;
    mStrokeWidth = strokeWidth;
    mFromFrame = fromFrame;
    mToFrame = toFrame;
    mKeyFrames = ListHelper.immutableOrEmpty(keyFrames);
    mTimingCurves = ArgCheckUtil.checkArg(
        timingCurves,
        ArgCheckUtil.checkTimingCurveObjectValidity(timingCurves, mKeyFrames.size()),
        TIMING_CURVES_JSON_FIELD);
    mAnimationGroup = animationGroup;
    mStrokeLineCap = strokeLineCap;

    mStrokeWidthAnimation = AnimationHelper.extractSpecialAnimationAnimationSet(
        featureAnimations,
        KFAnimation.PropertyType.STROKE_WIDTH);
    mAnchorPoint = AnimationHelper.extractSpecialAnimationAnimationSet(
        featureAnimations,
        KFAnimation.PropertyType.ANCHOR_POINT);
    ListHelper.sort(featureAnimations, KFAnimation.ANIMATION_PROPERTY_COMPARATOR);
    mFeatureMatrixAnimations = ListHelper.immutableOrEmpty(featureAnimations);
    mEffect = effect;
    mClassName = className;

    mKeyFramedPath = mKeyFrames.isEmpty() ? null : KeyFramedPath.fromFeature(this);
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

  public float getFromFrame() {
    return mFromFrame;
  }

  public float getToFrame() {
    return mToFrame;
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

  public Paint.Cap getStrokeLineCap() {
    return mStrokeLineCap;
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
    if (mAnchorPoint != null) {
      ((KeyFramedAnchorPoint) mAnchorPoint.getAnimation()).apply(featureMatrix);
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