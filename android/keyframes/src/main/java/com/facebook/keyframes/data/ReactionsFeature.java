// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.data;

import java.util.List;

import android.graphics.Color;
import android.graphics.Matrix;

import com.facebook.keyframes.data.keyframedmodels.KeyFramedPath;
import com.facebook.keyframes.data.keyframedmodels.KeyFramedStrokeWidth;

import static com.facebook.keyframes.data.keyframedmodels.KeyFramedStrokeWidth.NO_STROKE_WIDTH_ANIMATION_SENTINEL;

/**
 * An object which describes one shape layer to be drawn.  This includes color, stroke, and
 * animation information for just this shape.  The shape is backed by a series of vector commands
 * that describe how to draw the shape as a path.
 */
public class ReactionsFeature {

  /**
   * The name of this shape, for ease of identification.
   */
  public static final String NAME_JSON_FIELD = "name";
  private final String mName;

  /**
   * The fill color to use for this shape.
   */
  public static final String FILL_COLOR_JSON_FIELD = "fill_color";
  private final int mFillColor;

  /**
   * The stroke color to use for this shape.
   */
  public static final String STROKE_COLOR_JSON_FIELD = "stroke_color";
  private final int mStrokeColor;

  /**
   * The width of the stroke used if tracing the path.
   */
  public static final String STROKE_WIDTH_JSON_FIELD = "stroke_width";
  private final float mStrokeWidth;

  /**
   * A list of {@link ReactionsFeatureFrame}s which holds information about how the path of this
   * shape changes throughout the duration of the animation.
   */
  public static final String KEY_FRAMES_JSON_FIELD = "key_frames";
  private final List<ReactionsFeatureFrame> mKeyFrames;

  /**
   * Timing curves needed if mKeyFrames is present to describe how to animate between each key
   * frame.
   */
  public static final String TIMING_CURVES_JSON_FIELD = "timing_curves";
  private final float[][][] mTimingCurves;

  /**
   * The animation layer that this shape belongs to.  The final animation matrix of the group will
   * be applied to this feature, and any animations belonging to the feature will be nested within
   * this group.
   */
  public static final String ANIMATION_GROUP_JSON_FIELD = "animation_group";
  private final int mAnimationGroup;

  /**
   * A list of animations to apply to just this shape layer.
   */
  public static final String FEATURE_ANIMATIONS_JSON_FIELD = "feature_animations";
  /**
   * A post-processed object containing cached information for this path stroke width, if keyframed.
   */
  private final KeyFramedStrokeWidth mKeyFramedStrokeWidth;
  /**
   * The remaining, matrix based animations from the feature animations set.
   */
  private final List<ReactionsAnimation> mFeatureMatrixAnimations;

  /**
   * An optional effect that this shape layer can have.  Currently, only a simple linear gradient
   * is supported.
   */
  public static final String EFFECT_JSON_FIELD = "effects";
  private final ReactionsFeatureEffect mEffect;

  /**
   * A post-processed object containing cached information for this path, if keyframed.
   */
  private final KeyFramedPath mKeyFramedPath;


  public static class Builder {
    public String name;
    public int fillColor;
    public int strokeColor;
    public float strokeWidth;
    public List<ReactionsFeatureFrame> keyFrames;
    public float[][][] timingCurves;
    public int animationGroup;
    public List<ReactionsAnimation> featureAnimations;
    public ReactionsFeatureEffect effect;

    public ReactionsFeature build() {
      return new ReactionsFeature(
          name,
          fillColor,
          strokeColor,
          strokeWidth,
          keyFrames,
          timingCurves,
          animationGroup,
          featureAnimations,
          effect);
    }
  }

  public ReactionsFeature(
      String name,
      int fillColor,
      int strokeColor,
      float strokeWidth,
      List<ReactionsFeatureFrame> keyFrames,
      float[][][] timingCurves,
      int animationGroup,
      List<ReactionsAnimation> featureAnimations,
      ReactionsFeatureEffect effect) {
    mName = name;
    mFillColor = fillColor;
    mStrokeColor = strokeColor;
    mStrokeWidth = strokeWidth;
    mKeyFrames = ListHelper.immutableOrEmpty(keyFrames);
    mTimingCurves = timingCurves;
    mAnimationGroup = animationGroup;

    mKeyFramedStrokeWidth = extractKeyFramedStrokeWidthFromAnimationSet(featureAnimations);
    ListHelper.sort(featureAnimations, ReactionsAnimation.ANIMATION_PROPERTY_COMPARATOR);
    mFeatureMatrixAnimations = ListHelper.immutableOrEmpty(featureAnimations);
    mEffect = effect;

    mKeyFramedPath = KeyFramedPath.fromFeature(this);
  }

  /**
   * Returns a KeyFramedStrokeWidth, if available.  This method modifies the list passed in by
   * removing the stroke width entry from the list.
   * @param animations The complete list of feature animations to extract stroke width from
   * @return a valid KeyFramedStrokeWidth, if found, or a no animation sentinel otherwise
   */
  private KeyFramedStrokeWidth extractKeyFramedStrokeWidthFromAnimationSet(
      List<ReactionsAnimation> animations) {
    if (animations == null) {
      return NO_STROKE_WIDTH_ANIMATION_SENTINEL;
    }
    int strokeWidthAnimationIndex = -1;
    for (int i = 0, len = animations.size(); i < len; i++) {
      if (!animations.get(i).getPropertyType().isMatrixBased()) {
        // Only case is a stroke width animation, special to feature animation set.  Remove from the
        // set of matrix based animations and remember the index.
        strokeWidthAnimationIndex = i;
        break;
      }
    }
    if (strokeWidthAnimationIndex == -1) {
      return NO_STROKE_WIDTH_ANIMATION_SENTINEL;
    }
    return KeyFramedStrokeWidth.fromAnimation(animations.remove(strokeWidthAnimationIndex));
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

  private static int parseColor(String colorString) {
    if (colorString == null) {
      return Color.TRANSPARENT;
    }
    return Color.parseColor(colorString);
  }

  public void setStrokeWidth(
      KeyFramedStrokeWidth.StrokeWidth strokeWidth,
      float frameProgress) {
    strokeWidth.setStrokeWidth(mStrokeWidth);
    if (mKeyFramedStrokeWidth == NO_STROKE_WIDTH_ANIMATION_SENTINEL) {
      return;
    }
    mKeyFramedStrokeWidth.apply(frameProgress, strokeWidth);
  }

  public List<ReactionsFeatureFrame> getKeyFrames() {
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

  public void setAnimationMatrix(Matrix featureMatrix, float frameProgress) {
    featureMatrix.reset();
    if (mFeatureMatrixAnimations == null) {
      return;
    }
    for (ReactionsAnimation animation : mFeatureMatrixAnimations) {
      animation.getAnimation().apply(frameProgress, featureMatrix);
    }
  }

  public ReactionsFeatureEffect getEffect() {
    return mEffect;
  }
}