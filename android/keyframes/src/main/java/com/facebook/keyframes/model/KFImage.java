/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.facebook.keyframes.model;

import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.SparseArray;

import com.facebook.keyframes.util.AnimationGroupSort;
import com.facebook.keyframes.util.ArgCheckUtil;
import com.facebook.keyframes.util.ListHelper;

/**
 * The top level model object for one entire animated image.  Global information such as frame rate
 * it was exported as, frame count, and canvas size are included here for renderers.
 */
public class KFImage {

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
   * A list of all the feature layers for this image.
   */
  public static final String FEATURES_JSON_FIELD = "features";
  private final List<KFFeature> mFeatures;

  /**
   * A list of all the animation layers for this image.
   */
  public static final String ANIMATION_GROUPS_JSON_FIELD = "animation_groups";
  private final List<KFAnimationGroup> mAnimationGroups;

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

  /**
   * An optional map of bitmaps for this image.
   */
  public static final String BITMAPS_JSON_FIELD = "bitmaps";
  private final Map<String, Bitmap> mBitmaps;

  public static class Builder {
    public int frameRate;
    public int frameCount;
    public List<KFFeature> features;
    public List<KFAnimationGroup> animationGroups;
    public float[] canvasSize;
    public int key;
    public Map<String, Bitmap> bitmaps;

    public KFImage build() {
      return new KFImage(frameRate, frameCount, features, animationGroups, canvasSize, key, bitmaps);
    }
  }

  private KFImage(
      int frameRate,
      int frameCount,
      List<KFFeature> features,
      List<KFAnimationGroup> animationGroups,
      float[] canvasSize,
      int key,
      Map<String, Bitmap> bitmaps) {
    mFrameRate = ArgCheckUtil.checkArg(
        frameRate,
        frameRate > 0,
        FRAME_RATE_JSON_FIELD);
    mFrameCount = ArgCheckUtil.checkArg(
        frameCount,
        frameCount > 0,
        FRAME_COUNT_JSON_FIELD);
    mFeatures = ArgCheckUtil.checkArg(
        ListHelper.immutableOrEmpty(features),
        features.size() > 0,
        FEATURES_JSON_FIELD);
    animationGroups = AnimationGroupSort.sort(animationGroups);
    mAnimationGroups =
        ArgCheckUtil.checkArg(
            ListHelper.immutableOrEmpty(animationGroups),
            ArgCheckUtil.checkAnimationGroupIdUniqueness(animationGroups),
            ANIMATION_GROUPS_JSON_FIELD);
    mCanvasSize = ArgCheckUtil.checkArg(
        canvasSize,
        canvasSize.length == 2 && canvasSize[0] > 0 && canvasSize[1] > 0,
        CANVAS_SIZE_JSON_FIELD);
    mKey = key;
    mBitmaps = bitmaps;
  }

  public int getFrameRate() {
    return mFrameRate;
  }

  public int getFrameCount() {
    return mFrameCount;
  }

  public List<KFFeature> getFeatures() {
    return mFeatures;
  }

  public List<KFAnimationGroup> getAnimationGroups() {
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
    for (int groupIndex = 0, groupsLen = mAnimationGroups.size();
         groupIndex < groupsLen;
         groupIndex++) {
      KFAnimationGroup group = mAnimationGroups.get(groupIndex);
      matrix = matrixMap.get(group.getGroupId());
      matrix.reset();
      if (group.getAnchorPoint() != null) {
        group.getAnchorPoint().apply(frameProgress, matrix);
      }
      for (int animationIndex = 0, animationsLen = group.getAnimations().size();
           animationIndex < animationsLen;
           animationIndex++) {
        group.getAnimations().get(animationIndex).getAnimation().apply(frameProgress, matrix);
      }
      if (group.getParentGroup() > 0) {
        matrix.postConcat(matrixMap.get(group.getParentGroup()));
      }
    }
  }

  public float[] getCanvasSize() {
    return mCanvasSize;
  }

  public int getKey() {
    return mKey;
  }

  public Map<String, Bitmap> getBitmaps() {
    return mBitmaps;
  }
}
