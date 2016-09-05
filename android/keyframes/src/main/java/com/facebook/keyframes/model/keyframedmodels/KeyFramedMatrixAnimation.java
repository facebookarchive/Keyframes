/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.model.keyframedmodels;

import java.util.List;

import android.graphics.Matrix;

import com.facebook.keyframes.model.KFAnimation;
import com.facebook.keyframes.model.KFAnimationFrame;

/**
 * A {@link KeyFramedObject} which houses information for a matrix based animation.  This includes
 * rotation, scale, and translation (position) information which can be applied to other animation
 * layers or feature layers.  This is a post-process object used for {@link KFAnimation}.
 */
public class KeyFramedMatrixAnimation extends KeyFramedObject<KFAnimationFrame, Matrix> {

  /**
   * Constructs a KeyFramedMatrixAnimation from a {@link KFAnimation}.
   */
  public static KeyFramedMatrixAnimation fromAnimation(KFAnimation animation) {
    if (!animation.getPropertyType().isMatrixBased()) {
      throw new IllegalArgumentException(
          "Cannot create a KeyFramedMatrixAnimation from a non matrix based KFAnimation.");
    }
    return new KeyFramedMatrixAnimation(
        animation.getAnimationFrames(),
        animation.getTimingCurves(),
        animation.getPropertyType());
  }

  /**
   * The property type that is animated by this animation.
   */
  private final KFAnimation.PropertyType mPropertyType;

  private KeyFramedMatrixAnimation(
      List<KFAnimationFrame> objects,
      float[][][] timingCurves,
      KFAnimation.PropertyType propertyType) {
    super(objects, timingCurves);
    mPropertyType = propertyType;
  }

  /**
   * Applies the current state, given by interpolationValue, to the Matrix object.  Implementation
   * of the application method depends on the {@link KFAnimation.PropertyType} for this
   * animation.
   * @param stateA Initial state
   * @param stateB End state
   * @param interpolationValue Progress [0..1] between stateA and stateB
   * @param modifiable The matrix to apply the values to
   */
  @Override
  protected void applyImpl(
      KFAnimationFrame stateA,
      KFAnimationFrame stateB,
      float interpolationValue,
      Matrix modifiable) {
    switch (mPropertyType) {
      case ROTATION:
        applyRotation(stateA, stateB, interpolationValue, modifiable);
        break;
      case SCALE:
        applyScale(stateA, stateB, interpolationValue, modifiable);
        break;
      case X_POSITION:
        applyXPosition(stateA, stateB, interpolationValue, modifiable);
        break;
      case Y_POSITION:
        applyYPosition(stateA, stateB, interpolationValue, modifiable);
        break;
      default:
        throw new UnsupportedOperationException(
            "Cannot apply matrix transformation to " + mPropertyType);
    }
  }

  /**
   * This method applies a rotational transform to the matrix, interpolated between two states.
   */
  private void applyRotation(
      KFAnimationFrame stateA,
      KFAnimationFrame stateB,
      float interpolationValue,
      Matrix modifiable) {
    if (stateB == null) {
      modifiable.postRotate(stateA.getData()[0]);
      return;
    }
    float rotationStart = stateA.getData()[0];
    float rotationEnd = stateB.getData()[0];
    modifiable.postRotate(
        interpolateValue(rotationStart, rotationEnd, interpolationValue));
  }

  /**
   * This method applies a scale transformation to the matrix, interpolated between two states.
   */
  private void applyScale(
      KFAnimationFrame stateA,
      KFAnimationFrame stateB,
      float interpolationValue,
      Matrix modifiable) {
    if (stateB == null) {
      modifiable.postScale(
          stateA.getData()[0] / 100f,
          stateA.getData()[1] / 100f);
      return;
    }
    float scaleStartX = stateA.getData()[0];
    float scaleEndX = stateB.getData()[0];
    float scaleStartY = stateA.getData()[1];
    float scaleEndY = stateB.getData()[1];
    modifiable.postScale(
        interpolateValue(scaleStartX, scaleEndX, interpolationValue) / 100f,
        interpolateValue(scaleStartY, scaleEndY, interpolationValue) / 100f);
  }

  /**
   * This method applies an X translation transformation to the matrix, interpolated between two
   * states.
   */
  private void applyXPosition(
      KFAnimationFrame stateA,
      KFAnimationFrame stateB,
      float interpolationValue,
      Matrix modifiable) {
    if (stateB == null) {
      modifiable.postTranslate(stateA.getData()[0], 0);
      return;
    }
    float translationStartX = stateA.getData()[0];
    float translationEndX = stateB.getData()[0];
    modifiable.setTranslate(
        interpolateValue(translationStartX, translationEndX, interpolationValue),
        0);
  }

  /**
   * This method applies a Y translation transformation to the matrix, interpolated between two
   * states.
   */
  private void applyYPosition(
      KFAnimationFrame stateA,
      KFAnimationFrame stateB,
      float interpolationValue,
      Matrix modifiable) {
    if (stateB == null) {
      modifiable.postTranslate(0, stateA.getData()[0]);
      return;
    }
    float translationStartY = stateA.getData()[0];
    float translationEndY = stateB.getData()[0];
    modifiable.postTranslate(
        0,
        interpolateValue(translationStartY, translationEndY, interpolationValue));
  }
}
