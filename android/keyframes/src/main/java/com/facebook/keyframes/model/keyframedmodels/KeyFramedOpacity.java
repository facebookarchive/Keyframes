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
import com.facebook.keyframes.model.KFAnimation.PropertyType;
import com.facebook.keyframes.model.KFAnimationFrame;


/**
 * This is a special cased KFAnimation, since it is an animation which is not applied
 * via a matrix.  The information for this Modifiable is packed into a single length array.
 * A {@link KeyFramedObject} which houses information about a stroke width animation.  This includes
 * float values for stroke width at any given key frame.  This is a post-process object used for
 * KFAnimation.
 */
public class KeyFramedOpacity
    extends KeyFramedObject<KFAnimationFrame, KeyFramedOpacity.Opacity> {

  /**
   * A container object so that this class can set values on an object which on a common reference.
   */
  public static class Opacity {
    private float mOpacity = 100;

    public float getOpacity() {
      return mOpacity;
    }

    public void setOpacity(float opacity) {
      mOpacity = opacity;
    }
  }

  /**
   * Constructs a KeyFramedOpacity from a {@link KFAnimation}.
   */
  public static KeyFramedOpacity fromAnimation(KFAnimation animation) {
    if (animation.getPropertyType() != KFAnimation.PropertyType.OPACITY) {
      throw new IllegalArgumentException(
          "Cannot create a KeyFramedOpacity object from a non OPACITY animation.");
    }
    return new KeyFramedOpacity(animation.getAnimationFrames(), animation.getTimingCurves());
  }

  public KeyFramedOpacity(
      List<KFAnimationFrame> objects,
      float[][][] timingCurves) {
    super(objects, timingCurves);
  }

  private KeyFramedOpacity() {
    super();
  }

  /**
   * Applies the current state, given by interpolationValue, to the Opacity object.
   * @param stateA Initial state
   * @param stateB End state
   * @param interpolationValue Progress [0..1] between stateA and stateB
   * @param modifiable The Opacity to apply the values to
   */
  @Override
  protected void applyImpl(
      KFAnimationFrame stateA,
      KFAnimationFrame stateB,
      float interpolationValue,
      Opacity modifiable) {
    if (stateB == null) {
      modifiable.setOpacity(stateA.getData()[0]);
      return;
    }
    modifiable.setOpacity(
        interpolateValue(stateA.getData()[0], stateB.getData()[0], interpolationValue));
  }
}
