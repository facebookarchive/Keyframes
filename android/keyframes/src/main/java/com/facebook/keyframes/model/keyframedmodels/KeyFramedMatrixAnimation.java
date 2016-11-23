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
import com.facebook.keyframes.model.KFAnimation;
import com.facebook.keyframes.model.KFAnimationFrame;

import java.util.List;

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
        animation.getPropertyType(),
        animation.getAnchor());
  }

  /**
   * The property type that is animated by this animation.
   */
  private final KFAnimation.PropertyType mPropertyType;

  /**
   * The origin for this matrix animation.
   * Deprecated in favor of the ANCHOR_POINT animation.
   */
  @Deprecated
  private final float[] mAnchor;

  private KeyFramedMatrixAnimation(
      List<KFAnimationFrame> objects,
      float[][][] timingCurves,
      KFAnimation.PropertyType propertyType,
      float[] anchor) {
    super(objects, timingCurves);
    mPropertyType = propertyType;

    // Deprecated anchor behavior below
    mAnchor = anchor != null ? anchor : new float[2];
    if (propertyType == KFAnimation.PropertyType.POSITION) {
      // Translations are special cased relative transforms.
      mAnchor[0] = objects.get(0).getData()[0];
      mAnchor[1] = objects.get(0).getData()[1];
    }
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
      case POSITION:
        applyPosition(stateA, stateB, interpolationValue, modifiable);
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
      modifiable.postRotate(stateA.getData()[0],
          mAnchor != null ? mAnchor[0] : 0,
          mAnchor != null ? mAnchor[1] : 0);
      return;
    }
    float rotationStart = stateA.getData()[0];
    float rotationEnd = stateB.getData()[0];
    modifiable.postRotate(
        interpolateValue(rotationStart, rotationEnd, interpolationValue),
        mAnchor != null ? mAnchor[0] : 0,
        mAnchor != null ? mAnchor[1] : 0);
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
          stateA.getData()[1] / 100f,
          mAnchor != null ? mAnchor[0] : 0,
          mAnchor != null ? mAnchor[1] : 0);
      return;
    }
    float scaleStartX = stateA.getData()[0];
    float scaleEndX = stateB.getData()[0];
    float scaleStartY = stateA.getData()[1];
    float scaleEndY = stateB.getData()[1];
    modifiable.postScale(
        interpolateValue(scaleStartX, scaleEndX, interpolationValue) / 100f,
        interpolateValue(scaleStartY, scaleEndY, interpolationValue) / 100f,
        mAnchor != null ? mAnchor[0] : 0,
        mAnchor != null ? mAnchor[1] : 0);
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
    modifiable.postTranslate(
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

  /**
   * This method applies a translation transformation to the matrix.  Anchor points for a
   * translation animation are special cased to be relative to the position of the first frame
   * of this animation.  This means that if the translation for the first frame is 80x, 80y, and the
   * translation for the second key frame is 90x, 70y, the resulting translation is 10x, -10y.
   *
   * Deprecated in favor of X_POSITION and Y_POSITION transforms.
   */
  @Deprecated
  private void applyPosition(
      KFAnimationFrame stateA,
      KFAnimationFrame stateB,
      float interpolationValue,
      Matrix modifiable) {
    if (stateB == null) {
      return;
    }
    float translationStartX = stateA.getData()[0];
    float translationEndX = stateB.getData()[0];
    float translationStartY = stateA.getData()[1];
    float translationEndY = stateB.getData()[1];
    modifiable.postTranslate(
        interpolateValue(translationStartX, translationEndX, interpolationValue) - mAnchor[0],
        interpolateValue(translationStartY, translationEndY, interpolationValue) - mAnchor[1]);
  }
}
