/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.keyframes.model.keyframedmodels;

import android.graphics.Color;

import com.facebook.keyframes.model.KFAnimation;
import com.facebook.keyframes.model.KFAnimationFrame;

import java.util.List;


/**
 * This is a special cased KFAnimation, since it is an animation which is not applied
 * via a matrix.  The information for this Modifiable is packed into a single length array.
 * A {@link KeyFramedObject} which houses information about a stroke color animation.  This includes
 * float values for stroke color at any given key frame.  This is a post-process object used for
 * KFAnimation.
 */
public class KeyFramedFillColor
    extends KeyFramedObject<KFAnimationFrame, KeyFramedFillColor.FillColor> {

  /**
   * A container object so that this class can set values on an object which on a common reference.
   */
  public static class FillColor {
    private float mFillColor;
    private boolean mHasFillColor;

    public float getFillColor() {
      return mFillColor;
    }

    public void setFillColor(float strokeColor) {
      mFillColor = strokeColor;
      mHasFillColor = true;
    }

    public boolean hasFillColor() {
      return mHasFillColor;
    }
  }

  /**
   * Constructs a KeyFramedFillColor from a {@link KFAnimation}.
   */
  public static KeyFramedFillColor fromAnimation(KFAnimation animation) {
    if (animation.getPropertyType() != KFAnimation.PropertyType.FILL_COLOR) {
      throw new IllegalArgumentException(
          "Cannot create a KeyFramedFillColor object from a non FILL_COLOR animation.");
    }
    return new KeyFramedFillColor(animation.getAnimationFrames(), animation.getTimingCurves());
  }

  public KeyFramedFillColor(
      List<KFAnimationFrame> objects,
      float[][][] timingCurves) {
    super(objects, timingCurves);
  }

  private KeyFramedFillColor() {
    super();
  }

  /**
   * Applies the current state, given by interpolationValue, to the FillColor object.
   * @param stateA Initial state
   * @param stateB End state
   * @param interpolationValue Progress [0..1] between stateA and stateB
   * @param modifiable The FillColor to apply the values to
   */
  @Override
  protected void applyImpl(
      KFAnimationFrame stateA,
      KFAnimationFrame stateB,
      float interpolationValue,
      FillColor modifiable) {
    if (stateB == null) {
      modifiable.setFillColor(stateA.getData()[0]);
      return;
    }

    // Interpolate A, R, G, B values separately
    int aColor = (int)stateA.getData()[0];
    int bColor = (int)stateB.getData()[0];
    float alpha = interpolateValue(Color.alpha(aColor), Color.alpha(bColor), interpolationValue);
    float red = interpolateValue(Color.red(aColor), Color.red(bColor), interpolationValue);
    float green = interpolateValue(Color.green(aColor), Color.green(bColor), interpolationValue);
    float blue = interpolateValue(Color.blue(aColor), Color.blue(bColor), interpolationValue);

    modifiable.setFillColor(Color.argb((int)alpha, (int)red, (int)green, (int)blue));
  }
}
