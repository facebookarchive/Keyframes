/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes;

import android.graphics.Matrix;
import android.graphics.Path;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class KFPathTest {

  private class KFPathTestObject {
    final Path mockPath;
    final KFPath kfPath;

    public KFPathTestObject() {
      mockPath = Mockito.mock(Path.class);
      kfPath = new KFPath(mockPath, new float[]{0, 0});
    }
  }

  @Test
  public void testSimpleCommands() {
    KFPathTestObject kfPathWrapper = new KFPathTestObject();
    InOrder orderedMock = Mockito.inOrder(kfPathWrapper.mockPath);

    kfPathWrapper.kfPath.isEmpty();
    orderedMock.verify(kfPathWrapper.mockPath).isEmpty();

    Matrix matrix = new Matrix();
    kfPathWrapper.kfPath.transform(matrix);
    orderedMock.verify(kfPathWrapper.mockPath).transform(matrix);
  }

  @Test
  public void testDrawingCommandsAndLastPoint() {
    KFPathTestObject kfPathWrapper = new KFPathTestObject();
    InOrder orderedMock = Mockito.inOrder(kfPathWrapper.mockPath);

    kfPathWrapper.kfPath.moveTo(1.1f, 1.2f);
    orderedMock.verify(kfPathWrapper.mockPath).moveTo(1.1f, 1.2f);
    Assert.assertEquals(kfPathWrapper.kfPath.getLastPoint()[0], 1.1f);
    Assert.assertEquals(kfPathWrapper.kfPath.getLastPoint()[1], 1.2f);

    kfPathWrapper.kfPath.rMoveTo(2.1f, 2.2f);
    orderedMock.verify(kfPathWrapper.mockPath).rMoveTo(2.1f, 2.2f);
    Assert.assertEquals(kfPathWrapper.kfPath.getLastPoint()[0], 1.1f + 2.1f);
    Assert.assertEquals(kfPathWrapper.kfPath.getLastPoint()[1], 1.2f + 2.2f);

    kfPathWrapper.kfPath.reset();
    orderedMock.verify(kfPathWrapper.mockPath).reset();

    kfPathWrapper.kfPath.lineTo(1.1f, 1.2f);
    orderedMock.verify(kfPathWrapper.mockPath).lineTo(1.1f, 1.2f);
    Assert.assertEquals(kfPathWrapper.kfPath.getLastPoint()[0], 1.1f);
    Assert.assertEquals(kfPathWrapper.kfPath.getLastPoint()[1], 1.2f);

    kfPathWrapper.kfPath.rLineTo(2.1f, 2.2f);
    orderedMock.verify(kfPathWrapper.mockPath).rLineTo(2.1f, 2.2f);
    Assert.assertEquals(kfPathWrapper.kfPath.getLastPoint()[0], 1.1f + 2.1f);
    Assert.assertEquals(kfPathWrapper.kfPath.getLastPoint()[1], 1.2f + 2.2f);

    kfPathWrapper.kfPath.reset();
    orderedMock.verify(kfPathWrapper.mockPath).reset();

    kfPathWrapper.kfPath.quadTo(1.1f, 1.2f, 1.3f, 1.4f);
    orderedMock.verify(kfPathWrapper.mockPath).quadTo(1.1f, 1.2f, 1.3f, 1.4f);
    Assert.assertEquals(kfPathWrapper.kfPath.getLastPoint()[0], 1.3f);
    Assert.assertEquals(kfPathWrapper.kfPath.getLastPoint()[1], 1.4f);

    kfPathWrapper.kfPath.rQuadTo(2.1f, 2.2f, 2.3f, 2.4f);
    orderedMock.verify(kfPathWrapper.mockPath).rQuadTo(2.1f, 2.2f, 2.3f, 2.4f);
    Assert.assertEquals(kfPathWrapper.kfPath.getLastPoint()[0], 1.3f + 2.3f);
    Assert.assertEquals(kfPathWrapper.kfPath.getLastPoint()[1], 1.4f + 2.4f);

    kfPathWrapper.kfPath.reset();
    orderedMock.verify(kfPathWrapper.mockPath).reset();

    kfPathWrapper.kfPath.cubicTo(1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 1.6f);
    orderedMock.verify(kfPathWrapper.mockPath).cubicTo(1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 1.6f);
    Assert.assertEquals(kfPathWrapper.kfPath.getLastPoint()[0], 1.5f);
    Assert.assertEquals(kfPathWrapper.kfPath.getLastPoint()[1], 1.6f);

    kfPathWrapper.kfPath.rCubicTo(2.1f, 2.2f, 2.3f, 2.4f, 2.5f, 2.6f);
    orderedMock.verify(kfPathWrapper.mockPath).rCubicTo(2.1f, 2.2f, 2.3f, 2.4f, 2.5f, 2.6f);
    Assert.assertEquals(kfPathWrapper.kfPath.getLastPoint()[0], 1.5f + 2.5f);
    Assert.assertEquals(kfPathWrapper.kfPath.getLastPoint()[1], 1.6f + 2.6f);
  }
}
