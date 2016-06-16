// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.data;

/**
 * A simple class which wraps a float[] needed for one key frame.
 */
public class ReactionsAnimationFrame implements HasKeyFrame {

  /**
   * The key frame # in the animation sequence.
   */
  public static final String START_FRAME_JSON_FIELD = "start_frame";
  private final int mStartFrame;

  /**
   * The values for this key frame.
   */
  public static final String DATA_JSON_FIELD = "data";
  private final float[] mData;

  public static class Builder {
    public int startFrame;
    public float[] data;

    public ReactionsAnimationFrame build() {
      return new ReactionsAnimationFrame(startFrame, data);
    }
  }

  private ReactionsAnimationFrame(int startFrame, float[] data) {
    mStartFrame = startFrame;
    mData = data;
  }

  @Override
  public int getKeyFrame() {
    return mStartFrame;
  }

  public float[] getData() {
    return mData;
  }
}
