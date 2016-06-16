// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.data.keyframedmodels;

import java.util.List;

import android.graphics.Path;

import com.facebook.keyframes.data.ReactionsFeature;
import com.facebook.keyframes.data.ReactionsFeatureFrame;

/**
 * A {@link KeyFramedObject} which houses information for a key framed shape object.  This includes
 * the commands to draw a shape at each given key frame.  This is a post-process object used for
 * ReactionsFeature.
 */
public class KeyFramedPath extends KeyFramedObject<ReactionsFeatureFrame, Path> {

  /**
   * Constructs a KeyFramedPath from a {@link ReactionsFeature}.
   */
  public static KeyFramedPath fromFeature(ReactionsFeature feature) {
    return new KeyFramedPath(feature.getKeyFrames(), feature.getTimingCurves());
  }

  private KeyFramedPath(List<ReactionsFeatureFrame> featureFrames, float[][][] timingCurves) {
    super(featureFrames, timingCurves);
  }

  /**
   * Applies the current state, given by interpolationValue, to the supplied Path object.
   * @param stateA Initial state
   * @param stateB End state
   * @param interpolationValue Progress [0..1] between stateA and stateB
   * @param modifiable The modifiable object to apply the values to
   */
  @Override
  protected void applyImpl(
      ReactionsFeatureFrame stateA,
      ReactionsFeatureFrame stateB,
      float interpolationValue,
      Path modifiable) {
    if (stateB == null || interpolationValue == 0) {
      stateA.getShapeData().applyFeature(modifiable);
      return;
    }
    ReactionsFeatureFrame.ShapeMoveListData thisMoveList = stateA.getShapeData();
    ReactionsFeatureFrame.ShapeMoveListData nextMoveList = stateB.getShapeData();
    for (int i = 0, len = thisMoveList.getVectorCommands().size(); i < len; i++) {
      thisMoveList.getVectorCommands().get(i).interpolate(
          nextMoveList.getVectorCommands().get(i),
          interpolationValue,
          modifiable);
    }
  }
}
