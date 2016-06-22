/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListHelper {

  public static void sort(List list, Comparator comparator) {
    if (list == null || list.isEmpty()) {
      return;
    }
    Collections.sort(list, comparator);
  }

  public static List immutableOrEmpty(List list) {
    return list == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(list);
  }
}
