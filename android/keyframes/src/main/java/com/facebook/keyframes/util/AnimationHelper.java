/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.util;

import com.facebook.keyframes.model.KFAnimation;

import java.util.List;

/**
 * A helper class for parsing and extracting information from various KFAnimations.
 */
public class AnimationHelper {

  /**
   * Returns a special PropertyType KeyframeAnimation, if available.  This method modifies the list
   * passed in by found entry from the list.
   * @param animations The complete list of feature animations to extract the animation from
   * @return a valid animation of the passed in type, if found, or null otherwise
   */
  public static KFAnimation extractSpecialAnimationAnimationSet(
      List<KFAnimation> animations,
      KFAnimation.PropertyType specialAnimationType) {
    if (animations == null) {
      return null;
    }
    int specialAnimationIndex = -1;
    for (int i = 0, len = animations.size(); i < len; i++) {
      if (animations.get(i).getPropertyType() == specialAnimationType) {
        // Only case is a stroke width animation, special to feature animation set.  Remove from the
        // set of matrix based animations and remember the index.
        specialAnimationIndex = i;
        break;
      }
    }
    if (specialAnimationIndex == -1) {
      return null;
    }
    return animations.remove(specialAnimationIndex);
  }
}
