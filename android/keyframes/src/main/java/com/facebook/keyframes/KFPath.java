/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.keyframes;

import android.graphics.Matrix;
import android.graphics.Path;

/**
 * A wrapper around the Android {@link android.graphics.Path} object, only revealing methods which
 * we will use in Keyframes, as well as some basic ability to keep track of the last point set.
 */
public class KFPath {

  private final Path mPath;
  private final float[] mLastPoint;

  public KFPath() {
    this(new Path(), new float[]{0, 0});
  }

  /**
   * Constructor for tests to pass in objects
   */
  KFPath(Path path, float[] lastPoint) {
    mPath = path;
    mLastPoint = lastPoint;
  }

  /**
   * Returns the last point, which should *never* be modified outside of this class.
   */
  public float[] getLastPoint() {
    return mLastPoint;
  }

  /**
   * See {@link Path#reset()}.
   */
  public void reset() {
    mPath.reset();
    adjustLastPoint(0, 0);
  }

  /**
   * See {@link Path#moveTo(float, float)}.
   */
  public void moveTo(float x, float y) {
    mPath.moveTo(x, y);
    adjustLastPoint(x, y);
  }

  /**
   * See {@link Path#rMoveTo(float, float)}.
   */
  public void rMoveTo(float dx, float dy) {
    mPath.rMoveTo(dx, dy);
    rAdjustLastPoint(dx, dy);
  }

  /**
   * See {@link Path#lineTo(float, float)}.
   */
  public void lineTo(float x, float y) {
    mPath.lineTo(x, y);
    adjustLastPoint(x, y);
  }

  /**
   * See {@link Path#rLineTo(float, float)}.
   */
  public void rLineTo(float dx, float dy) {
    mPath.rLineTo(dx, dy);
    rAdjustLastPoint(dx, dy);
  }

  /**
   * See {@link Path#quadTo(float, float, float, float)}.
   */
  public void quadTo(float x1, float y1, float x2, float y2) {
    mPath.quadTo(x1, y1, x2, y2);
    adjustLastPoint(x2, y2);
  }

  /**
   * See {@link Path#rQuadTo(float, float, float, float)}.
   */
  public void rQuadTo(float dx1, float dy1, float dx2, float dy2) {
    mPath.rQuadTo(dx1, dy1, dx2, dy2);
    rAdjustLastPoint(dx2, dy2);
  }

  /**
   * See {@link Path#cubicTo(float, float, float, float, float, float)}.
   */
  public void cubicTo(
      float x1,
      float y1,
      float x2,
      float y2,
      float x3,
      float y3) {
    mPath.cubicTo(x1, y1, x2, y2, x3, y3);
    adjustLastPoint(x3, y3);
  }

  /**
   * See {@link Path#rCubicTo(float, float, float, float, float, float)}.
   */
  public void rCubicTo(
      float dx1,
      float dy1,
      float dx2,
      float dy2,
      float dx3,
      float dy3) {
    mPath.rCubicTo(dx1, dy1, dx2, dy2, dx3, dy3);
    rAdjustLastPoint(dx3, dy3);
  }

  /**
   * See {@link Path#transform(Matrix)}.
   */
  public void transform(Matrix matrix) {
    mPath.transform(matrix);
    matrix.mapPoints(mLastPoint);
  }

  /**
   * See {@link Path#isEmpty()}.
   */
  public boolean isEmpty() {
    return mPath.isEmpty();
  }

  /**
   * Returns this path, which should *never* be modified outside of this class.
   */
  protected Path getPath() {
    return mPath;
  }

  /**
   * Moves mLastPoint to (x, y).
   */
  private void adjustLastPoint(float x, float y) {
    mLastPoint[0] = x;
    mLastPoint[1] = y;
  }

  /**
   * Moves mLastPoint by (+dx, +dy).
   */
  private void rAdjustLastPoint(float dx, float dy) {
    mLastPoint[0] += dx;
    mLastPoint[1] += dy;
  }
}
