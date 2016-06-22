// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.data;

import android.graphics.Color;

import com.facebook.keyframes.util.ArgCheckUtil;

/**
 * A simple class which wraps a color value needed for one key frame.
 */
public class ReactionsColorFrame implements HasKeyFrame {

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

    public ReactionsColorFrame build() {
      return new ReactionsColorFrame(startFrame, color);
    }
  }

  private ReactionsColorFrame(int startFrame, int color) {
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
