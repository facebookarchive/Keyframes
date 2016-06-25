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
        createDummyAnimationOfType(KFAnimation.PropertyType.POSITION),
        createDummyAnimationOfType(KFAnimation.PropertyType.SCALE));
    KFAnimationGroup group = builder.build();

    Assert.assertEquals(
        KFAnimation.PropertyType.SCALE,
        group.getAnimations().get(0).getPropertyType());
    Assert.assertEquals(
        KFAnimation.PropertyType.ROTATION,
        group.getAnimations().get(1).getPropertyType());
    Assert.assertEquals(
        KFAnimation.PropertyType.POSITION,
        group.getAnimations().get(2).getPropertyType());
  }

  @Test
  public void testFeatureAnimationOrderingAndExtraction() {
    // Test transform matrix ordering is correct, as well as filtering out stroke_width from matrix
    // based animations.
    KFShape.Builder builder = new KFShape.Builder();
    KFAnimation dummyStrokeWidth =
        createDummyAnimationOfType(KFAnimation.PropertyType.STROKE_WIDTH);
    builder.featureAnimations = new ArrayList<>(Arrays.asList(
        createDummyAnimationOfType(KFAnimation.PropertyType.ROTATION),
        dummyStrokeWidth,
        createDummyAnimationOfType(KFAnimation.PropertyType.POSITION),
        createDummyAnimationOfType(KFAnimation.PropertyType.SCALE)));
    KFShape shape = builder.build();

    Assert.assertEquals(3, shape.mFeatureMatrixAnimations.size(), 0);
    Assert.assertEquals(
        KFAnimation.PropertyType.SCALE,
        shape.mFeatureMatrixAnimations.get(0).getPropertyType());
    Assert.assertEquals(
        KFAnimation.PropertyType.ROTATION,
        shape.mFeatureMatrixAnimations.get(1).getPropertyType());
    Assert.assertEquals(
        KFAnimation.PropertyType.POSITION,
        shape.mFeatureMatrixAnimations.get(2).getPropertyType());
    Assert.assertEquals(
        dummyStrokeWidth,
        shape.mStrokeWidthAnimation);
  }

  private KFAnimation createDummyAnimationOfType(KFAnimation.PropertyType type) {
    KFAnimation animation = Mockito.mock(KFAnimation.class);
    Mockito.when(animation.getPropertyType()).thenReturn(type);
    return animation;
  }
}
