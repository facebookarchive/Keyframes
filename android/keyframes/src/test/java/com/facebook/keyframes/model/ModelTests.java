/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.keyframes.model;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;

public class ModelTests {

  @Test
  public void testMatrixOrderingAnimationGroup() {
    KFAnimationGroup.Builder builder = new KFAnimationGroup.Builder();
    builder.groupId = 1;
    builder.animations = Arrays.asList(
        createDummyAnimationOfType(KFAnimation.PropertyType.ROTATION),
        createDummyAnimationOfType(KFAnimation.PropertyType.X_POSITION),
        createDummyAnimationOfType(KFAnimation.PropertyType.SCALE));
    KFAnimationGroup group = builder.build();

    Assert.assertEquals(
        KFAnimation.PropertyType.SCALE,
        group.getAnimations().get(0).getPropertyType());
    Assert.assertEquals(
        KFAnimation.PropertyType.ROTATION,
        group.getAnimations().get(1).getPropertyType());
    Assert.assertEquals(
        KFAnimation.PropertyType.X_POSITION,
        group.getAnimations().get(2).getPropertyType());
  }

  @Test
  public void testFeatureAnimationOrderingAndExtraction() {
    // Test transform matrix ordering is correct, as well as filtering out stroke_width from matrix
    // based animations.
    KFFeature.Builder builder = new KFFeature.Builder();
    KFAnimation dummyStrokeWidth =
        createDummyAnimationOfType(KFAnimation.PropertyType.STROKE_WIDTH);
    KFAnimation dummyStrokeColor =
            createDummyAnimationOfType(KFAnimation.PropertyType.STROKE_COLOR);
    KFAnimation dummyFillColor =
            createDummyAnimationOfType(KFAnimation.PropertyType.FILL_COLOR);
    builder.featureAnimations = new ArrayList<>(Arrays.asList(
        createDummyAnimationOfType(KFAnimation.PropertyType.ROTATION),
        dummyStrokeWidth,
        dummyStrokeColor,
        dummyFillColor,
        createDummyAnimationOfType(KFAnimation.PropertyType.X_POSITION),
        createDummyAnimationOfType(KFAnimation.PropertyType.SCALE)));
    KFFeature feature = builder.build();

    Assert.assertEquals(3, feature.mFeatureMatrixAnimations.size(), 0);
    Assert.assertEquals(
        KFAnimation.PropertyType.SCALE,
        feature.mFeatureMatrixAnimations.get(0).getPropertyType());
    Assert.assertEquals(
        KFAnimation.PropertyType.ROTATION,
        feature.mFeatureMatrixAnimations.get(1).getPropertyType());
    Assert.assertEquals(
        KFAnimation.PropertyType.X_POSITION,
        feature.mFeatureMatrixAnimations.get(2).getPropertyType());
    Assert.assertEquals(
        dummyStrokeWidth,
        feature.mStrokeWidthAnimation);
    Assert.assertEquals(
        dummyStrokeColor,
        feature.mStrokeColorAnimation);
    Assert.assertEquals(
        dummyFillColor,
        feature.mFillColorAnimation);
  }

  private KFAnimation createDummyAnimationOfType(KFAnimation.PropertyType type) {
    KFAnimation animation = Mockito.mock(KFAnimation.class);
    Mockito.when(animation.getPropertyType()).thenReturn(type);
    return animation;
  }
}
