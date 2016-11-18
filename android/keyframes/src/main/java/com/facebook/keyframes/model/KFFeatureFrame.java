/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.model;

import java.util.ArrayList;
import java.util.List;

import com.facebook.keyframes.KFPath;
import com.facebook.keyframes.util.VectorCommand;
import com.facebook.keyframes.util.ListHelper;

/**
 * A simple class which wraps path command information needed for one key frame.
 */
public class KFFeatureFrame implements HasKeyFrame {

  /**
   * The key frame # in the animation sequence.
   */
  public static final String START_FRAME_JSON_FIELD = "start_frame";
  private final int mStartFrame;

  /**
   * The raw string commands for this feature in one key frame.
   */
  public static final String DATA_JSON_FIELD = "data";
  private final ShapeMoveListData mShapeData;

  public static class Builder {
    public int startFrame;
    public List<String> data;

    public KFFeatureFrame build() {
      return new KFFeatureFrame(startFrame, data);
    }
  }

  public KFFeatureFrame(int startFrame, List<String> data) {
    mStartFrame = startFrame;
    mShapeData = new ShapeMoveListData(data);
  }

  @Override
  public int getKeyFrame() {
    return mStartFrame;
  }

  public ShapeMoveListData getShapeData() {
    return mShapeData;
  }

  public static class ShapeMoveListData {

    private final List<VectorCommand> mVectorCommands;

    public ShapeMoveListData(List<String> data) {
      List<VectorCommand> vectorCommandList = new ArrayList<>();
      for (int i = 0, len = data.size(); i < len; i ++) {
        vectorCommandList.add(VectorCommand.createVectorCommand(data.get(i)));
      }
      mVectorCommands = ListHelper.immutableOrEmpty(vectorCommandList);
    }

    public void applyFeature(KFPath path) {
      for (int i = 0, len = mVectorCommands.size(); i < len; i++) {
        mVectorCommands.get(i).apply(path);
      }
    }

    public List<VectorCommand> getVectorCommands() {
      return mVectorCommands;
    }
  }
}
