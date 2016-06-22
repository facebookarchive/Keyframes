// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.data;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Path;

import com.facebook.keyframes.util.VectorCommand;
import com.facebook.keyframes.util.ArgCheckUtil;
import com.facebook.keyframes.util.ListHelper;

/**
 * A simple class which wraps path command information needed for one key frame.
 */
public class ReactionsFeatureFrame implements HasKeyFrame {

  /**
   * The key frame # in the animation sequence.
   */
  public static final String START_FRAME_JSON_FIELD = "start_frame";
  private final int mStartFrame;

  /**
   * The raw string commands for this shape in one key frame.
   */
  public static final String DATA_JSON_FIELD = "data";
  private final ShapeMoveListData mShapeData;

  public static class Builder {
    public int startFrame;
    public List<String> data;

    public ReactionsFeatureFrame build() {
      return new ReactionsFeatureFrame(startFrame, data);
    }
  }

  public ReactionsFeatureFrame(int startFrame, List<String> data) {
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

    public void applyFeature(Path path) {
      for (int i = 0, len = mVectorCommands.size(); i < len; i++) {
        mVectorCommands.get(i).apply(path);
      }
    }

    public List<VectorCommand> getVectorCommands() {
      return mVectorCommands;
    }
  }
}
