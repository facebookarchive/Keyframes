// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.data;

import com.facebook.keyframes.util.ArgCheckUtil;
import com.facebook.keyframes.util.ListHelper;

import java.util.List;

/**
 * A class representing a single animation layer of this animation.
 */
public class ReactionsAnimationGroup {

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
   * animations since there are no shapes in this layer.
   */
  public static final String ANIMATIONS_JSON_FIELD = "animations";
  private final List<ReactionsAnimation> mAnimations;

  public static class Builder {
    public int groupId;
    public int parentGroup;
    public List<ReactionsAnimation> animations;

    public ReactionsAnimationGroup build() {
      return new ReactionsAnimationGroup(groupId, parentGroup, animations);
    }
  }

  public ReactionsAnimationGroup(
      int groupId,
      int parentGroup,
      List<ReactionsAnimation> animations) {
    mGroupId = ArgCheckUtil.checkArg(
        groupId,
        groupId > 0,
        GROUP_ID_JSON_FIELD);
    mParentGroup = parentGroup;
    ListHelper.sort(animations, ReactionsAnimation.ANIMATION_PROPERTY_COMPARATOR);
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

  public List<ReactionsAnimation> getAnimations() {
    return mAnimations;
  }
}
