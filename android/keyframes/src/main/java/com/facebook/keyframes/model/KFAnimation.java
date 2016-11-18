/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.model;

import java.util.Comparator;
import java.util.List;

import com.facebook.keyframes.model.keyframedmodels.KeyFramedAnchorPoint;
import com.facebook.keyframes.model.keyframedmodels.KeyFramedMatrixAnimation;
import com.facebook.keyframes.model.keyframedmodels.KeyFramedObject;
import com.facebook.keyframes.model.keyframedmodels.KeyFramedStrokeWidth;
import com.facebook.keyframes.util.ArgCheckUtil;
import com.facebook.keyframes.util.ListHelper;

/**
 * The base class for describing an animated property.  This can range from matrix based properties
 * such as scale, rotation, position, to others like stroke width.
 */
public class KFAnimation {

  public static final Comparator<KFAnimation> ANIMATION_PROPERTY_COMPARATOR =
      new Comparator<KFAnimation>() {
        @Override
        public int compare(
            KFAnimation lhs, KFAnimation rhs) {
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
    X_POSITION (true),
    Y_POSITION (true),
    ANCHOR_POINT (false),
    STROKE_WIDTH (false);

    /**
     * Whether this animation is matrix based or not.  Currently, the only non-matrix based
     * animation is stroke width, which is specific to {@link KFFeature}.
     */
    final boolean mIsMatrixBased;

    PropertyType(boolean isMatrixBased) {
      mIsMatrixBased = isMatrixBased;
    }

    /**
     * Returns whether this animation is matrix based or not.  Currently, the only non-matrix based
     * animation is stroke width, which is specific to {@link KFFeature}.
     */
    public boolean isMatrixBased() {
      return mIsMatrixBased;
    }
  }

  /**
   * The property animated by this KFAnimation instance.
   */
  public static final String PROPERTY_TYPE_JSON_FIELD = "property";
  private final PropertyType mPropertyType;

  /**
   * A list of {@link KFAnimationFrame}s, each describing timing and value data for a
   * specific key frame.
   */
  public static final String ANIMATION_FRAMES_JSON_FIELD = "key_values";
  private final List<KFAnimationFrame> mAnimationFrames;

  /**
   * An array of timing curve data.  This array describes transition values between each key frame
   * in mAnimationFrames.  For each timing curve, there are two sets of points which are the control
   * points describing how to draw the cubic curve from (0,0) to (1,1).
   */
  public static final String TIMING_CURVES_JSON_FIELD = "timing_curves";
  private final float[][][] mTimingCurves;

  /**
   * An anchor point, which changes the origin of a matrix based property.
   * Deprecated in favor of the ANCHOR_POINT animation.
   */
  @Deprecated
  public static final String ANCHOR_JSON_FIELD = "anchor";
  @Deprecated
  private final float[] mAnchor;

  /**
   * A post-processed data structure containing cached information for this key frame animation.
   */
  private final KeyFramedObject mKeyFramedAnimation;

  public static class Builder {
    public PropertyType propertyType;
    public List<KFAnimationFrame> animationFrames;
    public float[][][] timingCurves;
    public float[] anchor;

    public KFAnimation build() {
      return new KFAnimation(propertyType, animationFrames, timingCurves, anchor);
    }
  }

  public KFAnimation(
      PropertyType propertyType,
      List<KFAnimationFrame> animationFrames,
      float[][][] timingCurves,
      float[] anchor) {
    mPropertyType = ArgCheckUtil.checkArg(
        propertyType,
        propertyType != null,
        PROPERTY_TYPE_JSON_FIELD);
    mAnimationFrames = ArgCheckUtil.checkArg(
        ListHelper.immutableOrEmpty(animationFrames),
        animationFrames != null && animationFrames.size() > 0,
        ANIMATION_FRAMES_JSON_FIELD);
    mTimingCurves = ArgCheckUtil.checkArg(
        timingCurves,
        ArgCheckUtil.checkTimingCurveObjectValidity(timingCurves, mAnimationFrames.size()),
        TIMING_CURVES_JSON_FIELD);
    mAnchor = ArgCheckUtil.checkArg(
        anchor,
        anchor == null || anchor.length == 2,
        ANCHOR_JSON_FIELD);
    if (mPropertyType.isMatrixBased()) {
      mKeyFramedAnimation = KeyFramedMatrixAnimation.fromAnimation(this);
    } else if (mPropertyType == PropertyType.STROKE_WIDTH){
      mKeyFramedAnimation = KeyFramedStrokeWidth.fromAnimation(this);
    } else if (mPropertyType == PropertyType.ANCHOR_POINT) {
      mKeyFramedAnimation = KeyFramedAnchorPoint.fromAnchorPoint(this);
    } else {
      throw new IllegalArgumentException(
          "Unknown property type for animation post processing: " + mPropertyType);
    }
  }

  public PropertyType getPropertyType() {
    return mPropertyType;
  }

  public List<KFAnimationFrame> getAnimationFrames() {
    return mAnimationFrames;
  }

  public float[][][] getTimingCurves() {
    return mTimingCurves;
  }

  /**
   * Deprecated in favor of the ANCHOR_POINT animation.
   */
  @Deprecated
  public float[] getAnchor() {
    return mAnchor;
  }

  public KeyFramedObject getAnimation() {
    return mKeyFramedAnimation;
  }
}
