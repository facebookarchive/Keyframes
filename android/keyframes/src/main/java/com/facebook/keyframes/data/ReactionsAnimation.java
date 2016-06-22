// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.data;

import java.util.Comparator;
import java.util.List;

import com.facebook.keyframes.data.keyframedmodels.KeyFramedAnimation;
import com.facebook.keyframes.util.ArgCheckUtil;
import com.facebook.keyframes.util.ListHelper;

/**
 * The base class for describing an animated property.  This can range from matrix based properties
 * such as scale, rotation, position, to others like stroke width.
 */
public class ReactionsAnimation {

  public static final Comparator<ReactionsAnimation> ANIMATION_PROPERTY_COMPARATOR =
      new Comparator<ReactionsAnimation>() {
        @Override
        public int compare(
            ReactionsAnimation lhs, ReactionsAnimation rhs) {
          return lhs.getPropertyType().compareTo(rhs.getPropertyType());
        }
      };

  /**
   * Enum for the different supported animation types, listed in order of transform application.
   * Standard transform ordering is SCALE * ROTATION * TRANSLATION.
   */
  public enum PropertyType {
    SCALE (true),
    ROTATION (true),
    POSITION (true),
    STROKE_WIDTH (false);

    /**
     * Whether this animation is matrix based or not.  Currently, the only non-matrix based
     * animation is stroke width, which is specific to {@link ReactionsFeature}.
     */
    final boolean mIsMatrixBased;

    PropertyType(boolean isMatrixBased) {
      mIsMatrixBased = isMatrixBased;
    }

    /**
     * Returns whether this animation is matrix based or not.  Currently, the only non-matrix based
     * animation is stroke width, which is specific to {@link ReactionsFeature}.
     */
    public boolean isMatrixBased() {
      return mIsMatrixBased;
    }
  }

  /**
   * The property animated by this ReactionsAnimation instance.
   */
  public static final String PROPERTY_TYPE_JSON_FIELD = "property";
  private final PropertyType mPropertyType;

  /**
   * A list of {@link ReactionsAnimationFrame}s, each describing timing and value data for a
   * specific key frame.
   */
  public static final String ANIMATION_FRAMES_JSON_FIELD = "key_values";
  private final List<ReactionsAnimationFrame> mAnimationFrames;

  /**
   * An array of timing curve data.  This array describes transition values between each key frame
   * in mAnimationFrames.  For each timing curve, there are two sets of points which are the control
   * points describing how to draw the cubic curve from (0,0) to (1,1).
   */
  public static final String TIMING_CURVES_JSON_FIELD = "timing_curves";
  private final float[][][] mTimingCurves;

  /**
   * An anchor point, which changes the origin of a matrix based property.
   */
  public static final String ANCHOR_JSON_FIELD = "anchor";
  private final float[] mAnchor;

  /**
   * A post-processed data structure containing cached information for this key frame animation.
   */
  private final KeyFramedAnimation mKeyFramedAnimation;

  public static class Builder {
    public PropertyType propertyType;
    public List<ReactionsAnimationFrame> animationFrames;
    public float[][][] timingCurves;
    public float[] anchor;

    public ReactionsAnimation build() {
      return new ReactionsAnimation(propertyType, animationFrames, timingCurves, anchor);
    }
  }

  public ReactionsAnimation(
      PropertyType propertyType,
      List<ReactionsAnimationFrame> animationFrames,
      float[][][] timingCurves,
      float[] anchor) {
    mPropertyType = ArgCheckUtil.checkArg(
        propertyType,
        propertyType != null,
        PROPERTY_TYPE_JSON_FIELD);
    mAnimationFrames = ArgCheckUtil.checkArg(
        ListHelper.immutableOrEmpty(animationFrames),
        animationFrames != null && animationFrames.size() > 1,
        ANIMATION_FRAMES_JSON_FIELD);
    mTimingCurves = ArgCheckUtil.checkArg(
        timingCurves,
        ArgCheckUtil.checkTimingCurveObjectValidity(timingCurves, mAnimationFrames.size()),
        TIMING_CURVES_JSON_FIELD);
    mAnchor = ArgCheckUtil.checkArg(
        anchor,
        anchor == null || anchor.length == 2,
        ANCHOR_JSON_FIELD);
    mKeyFramedAnimation = KeyFramedAnimation.fromAnimation(this);
  }

  public PropertyType getPropertyType() {
    return mPropertyType;
  }

  public List<ReactionsAnimationFrame> getAnimationFrames() {
    return mAnimationFrames;
  }

  public float[][][] getTimingCurves() {
    return mTimingCurves;
  }

  public float[] getAnchor() {
    return mAnchor;
  }

  public KeyFramedAnimation getAnimation() {
    return mKeyFramedAnimation;
  }
}
