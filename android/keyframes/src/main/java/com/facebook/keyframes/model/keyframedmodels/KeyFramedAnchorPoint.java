/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.model.keyframedmodels;

import android.graphics.Matrix;

import com.facebook.keyframes.model.HasKeyFrame;
import com.facebook.keyframes.model.KFAnimation;

/**
 * A special object that defines the anchor point for the other animations in a group or feature.
 * Currently, it does not support keyframing.
 */
public class KeyFramedAnchorPoint extends KeyFramedObject<HasKeyFrame, Matrix> {

  public final float anchorX;
  public final float anchorY;

  public static KeyFramedAnchorPoint fromAnchorPoint(KFAnimation animation) {
    float[] anchor = animation.getAnimationFrames().get(0).getData();
    return new KeyFramedAnchorPoint(anchor[0], anchor[1]);
  }

  private KeyFramedAnchorPoint(float anchorX, float anchorY) {
    this.anchorX = anchorX;
    this.anchorY = anchorY;
  }

  public void apply(Matrix matrix) {
    matrix.postTranslate(-anchorX, -anchorY);
  }

  @Override
  public void apply(float frameProgress, Matrix modifiable) {
    throw new NoSuchMethodError("Anchor point currently has no keyframing ability");
  }

  @Override
  protected void applyImpl(
      HasKeyFrame stateA,
      HasKeyFrame stateB,
      float interpolationValue,
      Matrix modifiable) {
    throw new NoSuchMethodError("Anchor point currently has no keyframing ability");
  }
}
