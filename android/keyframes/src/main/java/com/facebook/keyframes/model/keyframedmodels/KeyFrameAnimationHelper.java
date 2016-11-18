/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.model.keyframedmodels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.view.animation.Interpolator;

import com.facebook.keyframes.util.KFPathInterpolator;

/**
 * A helper class to build a list of interpolators corresponding to a list of timing curves, for use
 * with key framed animations to describe interpolation between key frames.
 */
public class KeyFrameAnimationHelper {

  /**
   * Given a list of timing curves consisting of an outTangent and an inTangent with x/y values,
   * returns an ImmutableList with a corresponding interpolator for each timing curve, in the same
   * order as supplied.
   */
  public static List<Interpolator> buildInterpolatorList(float[][][] timingCurves) {
    if (timingCurves == null) {
      return Collections.emptyList();
    }
    List<Interpolator> interpolatorList = new ArrayList<>();
    for (int i = 0, len = timingCurves.length; i < len; i++) {
      float[][] influences = timingCurves[i];
      interpolatorList.add(
          new KFPathInterpolator(
              influences[0][0],
              influences[0][1],
              influences[1][0],
              influences[1][1]));
    }
    return Collections.unmodifiableList(interpolatorList);
  }

}
