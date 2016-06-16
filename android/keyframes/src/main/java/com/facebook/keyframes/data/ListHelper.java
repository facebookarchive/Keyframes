// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.data;

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
