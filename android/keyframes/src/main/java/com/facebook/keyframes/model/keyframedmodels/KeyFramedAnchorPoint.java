/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.keyframes.model.keyframedmodels;

import android.graphics.Matrix;

import com.facebook.keyframes.model.HasKeyFrame;
import com.facebook.keyframes.model.KFAnimation;
import com.facebook.keyframes.model.KFAnimationFrame;

import java.util.List;

/**
 * A special object that defines the anchor point for the other animations in a group or feature.
 */
public class KeyFramedAnchorPoint extends KeyFramedObject<KFAnimationFrame, Matrix> {

  public static KeyFramedAnchorPoint fromAnchorPoint(KFAnimation animation) {
    return new KeyFramedAnchorPoint(animation.getAnimationFrames(), animation.getTimingCurves());
  }

  private KeyFramedAnchorPoint(
      List<KFAnimationFrame> objects,
      float[][][] timingCurves) {
    super(objects, timingCurves);
  }

  @Override
  protected void applyImpl(
      KFAnimationFrame stateA,
      KFAnimationFrame stateB,
      float interpolationValue,
      Matrix matrix) {
    if (stateB == null) {
      matrix.postTranslate(-stateA.getData()[0], -stateA.getData()[1]);
      return;
    }
    matrix.postTranslate(
        -interpolateValue(stateA.getData()[0], stateB.getData()[0], interpolationValue),
        -interpolateValue(stateA.getData()[1], stateB.getData()[1], interpolationValue));
  }
}
