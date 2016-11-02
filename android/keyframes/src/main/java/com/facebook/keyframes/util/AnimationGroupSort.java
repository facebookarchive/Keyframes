/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.util;

import com.facebook.keyframes.model.KFAnimationGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class AnimationGroupSort {

  /**
   * Sorts a list of {@link KFAnimationGroup} in topological order.  The list passed in should be
   * mutable, and will be destroyed in the process.
   */
  public static List<KFAnimationGroup> sort(List<KFAnimationGroup> animationGroupList) {
    if (animationGroupList == null || animationGroupList.isEmpty()) {
      return Collections.EMPTY_LIST;
    }
    List<KFAnimationGroup> result = new ArrayList<>();
    Stack<KFAnimationGroup> rootNodes = new Stack<>();

    KFAnimationGroup currGroup;
    for (Iterator<KFAnimationGroup> iterator = animationGroupList.iterator();
         iterator.hasNext();) {
      currGroup = iterator.next();
      if (currGroup.getParentGroup() == 0) {
        rootNodes.push(currGroup);
        iterator.remove();
      }
    }

    while (!rootNodes.isEmpty()) {
      currGroup = rootNodes.pop();
      result.add(currGroup);
      for (Iterator<KFAnimationGroup> iterator = animationGroupList.iterator();
           iterator.hasNext();) {
        KFAnimationGroup childGroup = iterator.next();
        if (childGroup.getParentGroup() == currGroup.getGroupId()) {
          rootNodes.push(childGroup);
        }
      }
    }
    return result;
  }
}
