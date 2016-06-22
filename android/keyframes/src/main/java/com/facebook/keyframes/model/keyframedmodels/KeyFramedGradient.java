/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.model.keyframedmodels;

import java.util.List;

import android.graphics.Color;

import com.facebook.keyframes.model.KFColorFrame;
import com.facebook.keyframes.model.KFGradientColor;

/**
 * A {link KeyFramedObject} which houses information for a gradient animation.  This includes just
 * start and end colors.  The modifiable class is a simple container object that wraps a start and
 * end color that can be modified and passed around.  This is a post-process object used for
 * KFGradientColor.
 */
public class KeyFramedGradient
    extends KeyFramedObject<KFColorFrame, KeyFramedGradient.GradientColorPair> {

  /**
   * An enum for whether this gradient color is for the start (top) or end (bottom) color.
   */
  public enum Position {
    START,
    END
  }

  /**
   * A simple container that includes a start and end color which can be passed around and modified.
   * The setter methods can only be called by this class, since all other classes should only need
   * to grab values from the container.
   */
  public static class GradientColorPair {
    private int mStartColor;
    private int mEndColor;

    public int getStartColor() {
      return mStartColor;
    }

    private void setStartColor(int startColor) {
      mStartColor = startColor;
    }

    public int getEndColor() {
      return mEndColor;
    }

    private void setEndColor(int endColor) {
      mEndColor = endColor;
    }
  }

  /**
   * Constructs a KeyFramedGradient from a {@link KFGradientColor}.
   * @param gradientColor The corresponding {@link KFGradientColor}
   * @param position The position of this color, either START or END
   */
  public static KeyFramedGradient fromGradient(
      KFGradientColor gradientColor,
      Position position) {
    return new KeyFramedGradient(
        gradientColor.getKeyValues(),
        gradientColor.getTimingCurves(),
        position);
  }

  private final Position mPosition;

  private KeyFramedGradient(
      List<KFColorFrame> objects,
      float[][][] timingCurves,
      Position position) {
    super(objects, timingCurves);
    mPosition = position;
  }

  /**
   * Applies the current state, given by the interpolationValue, to the {@link GradientColorPair}.
   * @param stateA Initial state
   * @param stateB End state
   * @param interpolationValue Progress [0..1] between stateA and stateB
   * @param modifiable The {@link GradientColorPair} to apply the values to
   */
  @Override
  protected void applyImpl(
      KFColorFrame stateA,
      KFColorFrame stateB,
      float interpolationValue,
      GradientColorPair modifiable) {
    if (stateB == null) {
      if (mPosition == Position.START) {
        modifiable.setStartColor(stateA.getColor());
      } else {
        modifiable.setEndColor(stateA.getColor());
      }
      return;
    }
    if (mPosition == Position.START) {
      modifiable.setStartColor(
          getTransitionColor(interpolationValue, stateA.getColor(), stateB.getColor()));
    } else {
      modifiable.setEndColor(
          getTransitionColor(interpolationValue, stateA.getColor(), stateB.getColor()));
    }
  }

  /**
   * Given a start and end color, as well as a transition progress between the two, return a color
   * which is in between colorA and colorB, where 0 progress is colorA, 1 progress is colorB, and
   * all other progress values in between describe a linear transition.
   * @param progress Progress [0..1] between colorA and colorB.
   * @param colorA The color associated with progress 0
   * @param colorB The color associated with progress 1
   * @return The intermediate color that is {progress} between {colorA} and {colorB}.
   */
  public static int getTransitionColor(float progress, int colorA, int colorB) {
    int startA = Color.alpha(colorA);
    int startR = Color.red(colorA);
    int startG = Color.green(colorA);
    int startB = Color.blue(colorA);

    int endA = Color.alpha(colorB);
    int endR = Color.red(colorB);
    int endG = Color.green(colorB);
    int endB = Color.blue(colorB);
    return ((startA + (int) (progress * (endA - startA))) << 24) |
        ((startR + (int) (progress * (endR - startR))) << 16) |
        ((startG + (int) (progress * (endG - startG))) << 8) |
        ((startB + (int) (progress * (endB - startB))));
  }
}
