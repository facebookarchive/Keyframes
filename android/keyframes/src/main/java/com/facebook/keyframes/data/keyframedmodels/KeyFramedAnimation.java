// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.data.keyframedmodels;

import java.util.List;

import android.graphics.Matrix;

import com.facebook.keyframes.data.ReactionsAnimation;
import com.facebook.keyframes.data.ReactionsAnimationFrame;

/**
 * A {@link KeyFramedObject} which houses information for a matrix based animation.  This includes
 * rotation, scale, and translation (position) information which can be applied to other animation
 * layers or shape layers.  This is a post-process object used for {@link ReactionsAnimation}.
 */
public class KeyFramedAnimation extends KeyFramedObject<ReactionsAnimationFrame, Matrix> {

  /**
   * Constructs a KeyFramedAnimation from a {@link ReactionsAnimation}.
   */
  public static KeyFramedAnimation fromAnimation(ReactionsAnimation animation) {
    return new KeyFramedAnimation(
        animation.getAnimationFrames(),
        animation.getTimingCurves(),
        animation.getPropertyType(),
        animation.getAnchor());
  }

  /**
   * The property type that is animated by this animation.
   */
  private final ReactionsAnimation.PropertyType mPropertyType;

  /**
   * The origin for this matrix animation.
   */
  private final float[] mAnchor;

  private KeyFramedAnimation(
      List<ReactionsAnimationFrame> objects,
      float[][][] timingCurves,
      ReactionsAnimation.PropertyType propertyType,
      float[] anchor) {
    super(objects, timingCurves);
    mPropertyType = propertyType;
    mAnchor = anchor != null ? anchor : new float[2];

    if (propertyType == ReactionsAnimation.PropertyType.POSITION) {
      // Translations are special cased relative transforms.
      mAnchor[0] = objects.get(0).getData()[0];
      mAnchor[1] = objects.get(0).getData()[1];
    }
  }

  /**
   * Applies the current state, given by interpolationValue, to the Matrix object.  Implementation
   * of the application method depends on the {@link ReactionsAnimation.PropertyType} for this
   * animation.
   * @param stateA Initial state
   * @param stateB End state
   * @param interpolationValue Progress [0..1] between stateA and stateB
   * @param modifiable The matrix to apply the values to
   */
  @Override
  protected void applyImpl(
      ReactionsAnimationFrame stateA,
      ReactionsAnimationFrame stateB,
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
      default:
        throw new UnsupportedOperationException(
            "Cannot apply matrix transformation to " + mPropertyType);
    }
  }

  /**
   * This method applies a rotational transform to the matrix.  The anchor points determine where
   * the rotation is centered around.
   */
  private void applyRotation(
      ReactionsAnimationFrame stateA,
      ReactionsAnimationFrame stateB,
      float interpolationValue,
      Matrix modifiable) {
    if (stateB == null) {
      modifiable.postRotate(stateA.getData()[0], mAnchor[0], mAnchor[1]);
      return;
    }
    float rotationStart = stateA.getData()[0];
    float rotationEnd = stateB.getData()[0];
    modifiable.postRotate(
        interpolateValue(rotationStart, rotationEnd, interpolationValue),
        mAnchor[0],
        mAnchor[1]);
  }

  /**
   * This method applies a scale transformation to the matrix.  The anchor points determine where
   * the original of scaling is.
   */
  private void applyScale(
      ReactionsAnimationFrame stateA,
      ReactionsAnimationFrame stateB,
      float interpolationValue,
      Matrix modifiable) {
    if (stateB == null) {
      modifiable.postScale(
          stateA.getData()[0] / 100f,
          stateA.getData()[1] / 100f,
          mAnchor[0],
          mAnchor[1]);
      return;
    }
    float scaleStartX = stateA.getData()[0];
    float scaleEndX = stateB.getData()[0];
    float scaleStartY = stateA.getData()[1];
    float scaleEndY = stateB.getData()[1];
    modifiable.postScale(
        interpolateValue(scaleStartX, scaleEndX, interpolationValue) / 100f,
        interpolateValue(scaleStartY, scaleEndY, interpolationValue) / 100f,
        mAnchor[0],
        mAnchor[1]);
  }

  /**
   * This method applies a translation transformation to the matrix.  Anchor points for a
   * translation animation are special cased to be relative to the position of the first frame
   * of this animation.  This means that if the translation for the first frame is 80x, 80y, and the
   * translation for the second key frame is 90x, 70y, the resulting translation is 10x, -10y.
   */
  private void applyPosition(
      ReactionsAnimationFrame stateA,
      ReactionsAnimationFrame stateB,
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
