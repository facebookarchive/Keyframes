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
public class KeyFramedStrokeColor
    extends KeyFramedObject<KFAnimationFrame, KeyFramedStrokeColor.StrokeColor> {

  /**
   * A container object so that this class can set values on an object which on a common reference.
   */
  public static class StrokeColor {
    private float mStrokeColor;
    private boolean mHasStrokeColor;

    public float getStrokeColor() {
      return mStrokeColor;
    }

    public void setStrokeColor(float strokeColor) {
      mStrokeColor = strokeColor;
      mHasStrokeColor = true;
    }

    public boolean hasStrokeColor() {
      return mHasStrokeColor;
    }
  }

  /**
   * Constructs a KeyFramedStrokeColor from a {@link KFAnimation}.
   */
  public static KeyFramedStrokeColor fromAnimation(KFAnimation animation) {
    if (animation.getPropertyType() != KFAnimation.PropertyType.STROKE_COLOR) {
      throw new IllegalArgumentException(
          "Cannot create a KeyFramedStrokeColor object from a non STROKE_COLOR animation.");
    }
    return new KeyFramedStrokeColor(animation.getAnimationFrames(), animation.getTimingCurves());
  }

  public KeyFramedStrokeColor(
      List<KFAnimationFrame> objects,
      float[][][] timingCurves) {
    super(objects, timingCurves);
  }

  private KeyFramedStrokeColor() {
    super();
  }

  /**
   * Applies the current state, given by interpolationValue, to the StrokeColor object.
   * @param stateA Initial state
   * @param stateB End state
   * @param interpolationValue Progress [0..1] between stateA and stateB
   * @param modifiable The StrokeColor to apply the values to
   */
  @Override
  protected void applyImpl(
      KFAnimationFrame stateA,
      KFAnimationFrame stateB,
      float interpolationValue,
      StrokeColor modifiable) {
    if (stateB == null) {
      modifiable.setStrokeColor(stateA.getData()[0]);
      return;
    }

    // Interpolate A, R, G, B values separately
    int aColor = (int)stateA.getData()[0];
    int bColor = (int)stateB.getData()[0];
    float alpha = interpolateValue(Color.alpha(aColor), Color.alpha(bColor), interpolationValue);
    float red = interpolateValue(Color.red(aColor), Color.red(bColor), interpolationValue);
    float green = interpolateValue(Color.green(aColor), Color.green(bColor), interpolationValue);
    float blue = interpolateValue(Color.blue(aColor), Color.blue(bColor), interpolationValue);

    modifiable.setStrokeColor(Color.argb((int)alpha, (int)red, (int)green, (int)blue));
  }
}
