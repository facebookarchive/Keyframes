// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.data.keyframedmodels;

import java.util.List;

import com.facebook.keyframes.data.ReactionsAnimation;
import com.facebook.keyframes.data.ReactionsAnimationFrame;


/**
 * This is a special cased KeyFramedAnimation, since it is the only animation which is not applied
 * via a matrix.  The information for this Modifiable is packed into a single length array.
 * A {@link KeyFramedObject} which houses information about a stroke width animation.  This includes
 * float values for stroke width at any given key frame.  This is a post-process object used for
 * ReactionsAnimation.
 */
public class KeyFramedStrokeWidth
    extends KeyFramedObject<ReactionsAnimationFrame, KeyFramedStrokeWidth.StrokeWidth> {

  public static final KeyFramedStrokeWidth NO_STROKE_WIDTH_ANIMATION_SENTINEL =
      new KeyFramedStrokeWidth();

  /**
   * A container object so that this class can set values on an object which on a common reference.
   */
  public static class StrokeWidth {
    private float mStrokeWidth;

    public float getStrokeWidth() {
      return mStrokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
      mStrokeWidth = strokeWidth;
    }
  }

  /**
   * Constructs a KeyFramedStrokeWidth from a {@link ReactionsAnimation}.
   */
  public static KeyFramedStrokeWidth fromAnimation(ReactionsAnimation animation) {
    if (animation.getPropertyType() != ReactionsAnimation.PropertyType.STROKE_WIDTH) {
      throw new IllegalArgumentException(
          "Attempted to create a KeyFramedStrokeWidth object from a non STROKE_WIDTH animation.");
    }
    return new KeyFramedStrokeWidth(animation.getAnimationFrames(), animation.getTimingCurves());
  }

  public KeyFramedStrokeWidth(
      List<ReactionsAnimationFrame> objects,
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
      ReactionsAnimationFrame stateA,
      ReactionsAnimationFrame stateB,
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
