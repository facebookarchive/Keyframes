// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.data;

import java.util.List;

import android.graphics.Matrix;
import android.util.SparseArray;

/**
 * The top level model object for one entire animated image.  Global information such as frame rate
 * it was exported as, frame count, and canvas size are included here for renderers.
 */
public class ReactionsFace {

  /**
   * The frame rate that this animation was exported at.  This is needed to play back the animation
   * at the correct speed.  It does not limit the playback to discrete frames per second.
   */
  public static final String FRAME_RATE_JSON_FIELD = "frame_rate";
  private final int mFrameRate;

  /**
   * The total number of frames for this animation.
   */
  public static final String FRAME_COUNT_JSON_FIELD = "animation_frame_count";
  private final int mFrameCount;

  /**
   * A list of all the shape layers for this image.
   */
  public static final String FEATURES_JSON_FIELD = "features";
  private final List<ReactionsFeature> mFeatures;

  /**
   * A list of all the animation layers for this image.
   */
  public static final String ANIMATION_GROUPS_JSON_FIELD = "animation_groups";
  private final List<ReactionsAnimationGroup> mAnimationGroups;

  /**
   * The canvas size that this image was initially exported as.
   */
  public static final String CANVAS_SIZE_JSON_FIELD = "canvas_size";
  private final float[] mCanvasSize;

  /**
   * An optional identification key for this image.
   */
  public static final String KEY_JSON_FIELD = "key";
  private final int mKey;

  public static class Builder {
    public int frameRate;
    public int frameCount;
    public List<ReactionsFeature> features;
    public List<ReactionsAnimationGroup> animationGroups;
    public float[] canvasSize;
    public int key;

    public ReactionsFace build() {
      return new ReactionsFace(frameRate, frameCount, features, animationGroups, canvasSize, key);
    }
  }

  private ReactionsFace(
      int frameRate,
      int frameCount,
      List<ReactionsFeature> features,
      List<ReactionsAnimationGroup> animationGroups,
      float[] canvasSize,
      int key) {
    mFrameRate = frameRate;
    mFrameCount = frameCount;
    mFeatures = ListHelper.immutableOrEmpty(features);
    mAnimationGroups = ListHelper.immutableOrEmpty(animationGroups);
    mCanvasSize = canvasSize;
    mKey = key;
  }

  public int getFrameRate() {
    return mFrameRate;
  }

  public int getFrameCount() {
    return mFrameCount;
  }

  public List<ReactionsFeature> getFeatures() {
    return mFeatures;
  }

  public List<ReactionsAnimationGroup> getAnimationGroups() {
    return mAnimationGroups;
  }

  /**
   * Given a map of group id and corresponding matrices, apply the current matrix state calculated
   * from progress in the animation to the matrix in the map.
   * @param matrixMap A prefilled map of animation group id -> matrix
   * @param frameProgress The progress in animation, given as a frame value
   */
  public void setAnimationMatrices(SparseArray<Matrix> matrixMap, float frameProgress) {
    Matrix matrix;
    for (ReactionsAnimationGroup group : mAnimationGroups) {
      matrix = matrixMap.get(group.getGroupId());
      matrix.reset();
      for (ReactionsAnimation animation : group.getAnimations()) {
        if (!animation.getPropertyType().isMatrixBased()) {
          continue;
        }
        animation.getAnimation().apply(frameProgress, matrix);
      }
      if (group.getParentGroup() > 0) {
        matrix.preConcat(matrixMap.get(group.getParentGroup()));
      }
    }
  }

  public float[] getCanvasSize() {
    return mCanvasSize;
  }

  public int getKey() {
    return mKey;
  }
}
