/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.keyframes.model.keyframedmodels;

import java.util.List;

import com.facebook.keyframes.model.KFAnimation;
import com.facebook.keyframes.model.KFAnimationFrame;


/**
 * This is a special cased KFAnimation, since it is an animation which is not applied
 * via a matrix.  The information for this Modifiable is packed into a single length array.
 * A {@link KeyFramedObject} which houses information about a stroke width animation.  This includes
 * float values for stroke width at any given key frame.  This is a post-process object used for
 * KFAnimation.
 */
public class KeyFramedStrokeWidth
    extends KeyFramedObject<KFAnimationFrame, KeyFramedStrokeWidth.StrokeWidth> {

  /**
   * A container object so that this class can set values on an object which on a common reference.
   */
  public static class StrokeWidth {
    private float mStrokeWidth;

    public float getStrokeWidth() {
      return Math.abs(mStrokeWidth);
    }

    public void setStrokeWidth(float strokeWidth) {
      mStrokeWidth = strokeWidth;
    }

    public void adjustScale(float scale) {
      mStrokeWidth *= scale;
    }
  }

  /**
   * Constructs a KeyFramedStrokeWidth from a {@link KFAnimation}.
   */
  public static KeyFramedStrokeWidth fromAnimation(KFAnimation animation) {
    if (animation.getPropertyType() != KFAnimation.PropertyType.STROKE_WIDTH) {
      throw new IllegalArgumentException(
          "Cannot create a KeyFramedStrokeWidth object from a non STROKE_WIDTH animation.");
    }
    return new KeyFramedStrokeWidth(animation.getAnimationFrames(), animation.getTimingCurves());
  }

  public KeyFramedStrokeWidth(
      List<KFAnimationFrame> objects,
      float[][][] timingCurves) {
    super(objects, timingCurves);
  }

  private KeyFramedStrokeWidth() {
    super();
  }

  /**
   * Applies the current state, given by interpolationValue, to the StrokeWidth object.
   * @param stateA Initial state
   * @param stateB End state
   * @param interpolationValue Progress [0..1] between stateA and stateB
   * @param modifiable The StrokeWidth to apply the values to
   */
  @Override
  protected void applyImpl(
      KFAnimationFrame stateA,
      KFAnimationFrame stateB,
      float interpolationValue,
      StrokeWidth modifiable) {
    if (stateB == null) {
      modifiable.setStrokeWidth(stateA.getData()[0]);
      return;
    }
    modifiable.setStrokeWidth(
        interpolateValue(stateA.getData()[0], stateB.getData()[0], interpolationValue));
  }
}
