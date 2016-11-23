/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.keyframes.model;

import com.facebook.keyframes.util.ArgCheckUtil;

/**
 * A simple class which wraps a float[] needed for one key frame.
 */
public class KFAnimationFrame implements HasKeyFrame {

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

    public KFAnimationFrame build() {
      return new KFAnimationFrame(startFrame, data);
    }
  }

  private KFAnimationFrame(int startFrame, float[] data) {
    mStartFrame = startFrame;
    mData = ArgCheckUtil.checkArg(
        data,
        data.length > 0,
        DATA_JSON_FIELD);
  }

  @Override
  public int getKeyFrame() {
    return mStartFrame;
  }

  public float[] getData() {
    return mData;
  }
}
