/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.keyframes.util;

import com.facebook.keyframes.model.KFAnimationGroup;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ArgCheckUtil {

  /**
   * Checks the validity of an argument, given a condition.  If the condition passes, the argument
   * is returned.  If not, an IllegalArgumentException is thrown.
   * @param arg The argument
   * @param condition The evaluated condition
   * @param argName The name of the argument field for use in the exception, if needed.
   * @return The argument, if condition is valid.
   * @throws IllegalArgumentException if the condition is invalid
   */
  public static <T> T checkArg(T arg, boolean condition, String argName) {
    if (condition) {
      return arg;
    }
    throw new IllegalArgumentException(
        String.format(Locale.US,
            "Illegal argument for %s.",
            argName));
  }

  /**
   * Checks that the format of a timing curve 3D float array is valid.  The number of timing curves
   * for an animation should be equal to the number of keyframes - 1.
   * @param timingCurves the 3D float array to check
   * @param keyFrameQuantity the number of key frames this animation has
   * @return true if the format is valid, false otherwise
   */
  public static boolean checkTimingCurveObjectValidity(
      float[][][] timingCurves,
      int keyFrameQuantity) {
    if (keyFrameQuantity <= 1 && (timingCurves == null || timingCurves.length == 0)) {
      return true;
    }
    if (keyFrameQuantity - 1 != timingCurves.length) {
      return false;
    }
    for (int i = 0; i < timingCurves.length; i++) {
      if (timingCurves[i].length != 2) {
        return false;
      }
      for (int j = 0; j < timingCurves[i].length; j++) {
        if (timingCurves[i][j].length != 2) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Checks that the list of {@link KFAnimationGroup} all contain unique IDs.
   * @param groups the list of {@link KFAnimationGroup}
   * @return true if all IDs are unique, false otherwise
   */
  public static boolean checkAnimationGroupIdUniqueness(List<KFAnimationGroup> groups) {
    if (groups == null || groups.size() == 0) {
      return true;
    }
    Set<Integer> keys = new HashSet<>(groups.size());
    for (int i = 0, len = groups.size(); i < len; i++) {
      Integer groupId = groups.get(i).getGroupId();
      if (keys.contains(groupId)) {
        return false;
      }
      keys.add(groupId);
    }
    return true;
  }
}
