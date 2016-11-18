/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.model;

import com.facebook.keyframes.model.keyframedmodels.KeyFramedAnchorPoint;
import com.facebook.keyframes.util.AnimationHelper;
import com.facebook.keyframes.util.ArgCheckUtil;
import com.facebook.keyframes.util.ListHelper;

import java.util.List;

/**
 * A class representing a single animation layer of this animation.
 */
public class KFAnimationGroup {

  /**
   * An identifier for this animation layer, in case there are dependencies on it.
   */
  public static final String GROUP_ID_JSON_FIELD = "group_id";
  private final int mGroupId;

  /**
   * An identifier for a parent animation layer which this layer is nested in.
   */
  public static final String PARENT_GROUP_JSON_FIELD = "parent_group";
  private final int mParentGroup;

  /**
   * The different animations which are part of this layer.  This should only include matrix based
   * animations since there are no features in this layer.
   */
  public static final String ANIMATIONS_JSON_FIELD = "animations";
  private final List<KFAnimation> mAnimations;

  private final KFAnimation mAnchorPoint;

  public static class Builder {
    public int groupId;
    public int parentGroup;
    public List<KFAnimation> animations;

    public KFAnimationGroup build() {
      return new KFAnimationGroup(groupId, parentGroup, animations);
    }
  }

  public KFAnimationGroup(
      int groupId,
      int parentGroup,
      List<KFAnimation> animations) {
    mGroupId = ArgCheckUtil.checkArg(
        groupId,
        groupId > 0,
        GROUP_ID_JSON_FIELD);
    mParentGroup = parentGroup;
    ListHelper.sort(animations, KFAnimation.ANIMATION_PROPERTY_COMPARATOR);
    mAnchorPoint = AnimationHelper.extractSpecialAnimationAnimationSet(
        animations,
        KFAnimation.PropertyType.ANCHOR_POINT);
    mAnimations = ArgCheckUtil.checkArg(
        ListHelper.immutableOrEmpty(animations),
        animations.size() > 0,
        ANIMATIONS_JSON_FIELD);
  }

  public int getGroupId() {
    return mGroupId;
  }

  public int getParentGroup() {
    return mParentGroup;
  }

  public List<KFAnimation> getAnimations() {
    return mAnimations;
  }

  public KeyFramedAnchorPoint getAnchorPoint() {
    if (mAnchorPoint == null) {
      return null;
    }
    return (KeyFramedAnchorPoint) mAnchorPoint.getAnimation();
  }
}
