/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.keyframes.model;

/**
 * A simple class which wraps a color value needed for one key frame.
 */
public class KFColorFrame implements HasKeyFrame {

  /**
   * The key frame # in the animation sequence.
   */
  public static final String START_FRAME_JSON_FIELD = "start_frame";
  private final int mStartFrame;

  /**
   * The color value for this key frame.
   */
  public static final String COLOR_JSON_FIELD = "data";
  private final int mColor;

  public static class Builder {
    public int startFrame;
    public int color;

    public KFColorFrame build() {
      return new KFColorFrame(startFrame, color);
    }
  }

  private KFColorFrame(int startFrame, int color) {
    mStartFrame = startFrame;
    mColor = color;
  }

  @Override
  public int getKeyFrame() {
    return mStartFrame;
  }

  public int getColor() {
    return mColor;
  }
}
