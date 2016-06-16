// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.data;

import java.util.List;

/**
 * The class which contains information about an animated gradient, including the key frame
 * information and timing curves.
 */
public class ReactionsGradientColor {

  /**
   * A list of {@link ReactionsColorFrame}s, each describing a color given a key frame.
   */
  public static final String KEY_VALUES_JSON_FIELD = "key_values";
  private final List<ReactionsColorFrame> mKeyValues;

  /**
   * A list of timing curves which describes how to interpolate between two
   * {@link ReactionsColorFrame}s.
   */
  public static final String TIMING_CURVES_JSON_FIELD = "timing_curves";
  private final float[][][] mTimingCurves;

  public static class Builder {
    public List<ReactionsColorFrame> keyValues;
    public float[][][] timingCurves;

    public ReactionsGradientColor build() {
      return new ReactionsGradientColor(keyValues, timingCurves);
    }
  }

  public ReactionsGradientColor(List<ReactionsColorFrame> keyValues, float[][][] timingCurves) {
    mKeyValues = ListHelper.immutableOrEmpty(keyValues);
    mTimingCurves = timingCurves;
  }

  public List<ReactionsColorFrame> getKeyValues() {
    return mKeyValues;
  }

  public float[][][] getTimingCurves() {
    return mTimingCurves;
  }
}
