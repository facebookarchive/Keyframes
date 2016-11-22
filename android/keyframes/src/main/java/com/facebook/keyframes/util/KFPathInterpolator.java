/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.keyframes.util;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.view.animation.Interpolator;

/**
 * This class is directly lifted from the compat library's
 * {@link android.support.v4.view.animation.PathInterpolatorCompat}.  The reason we need this class
 * is to be able to control the precision ourselves for a smaller memory footprint.  The normal
 * PathInterpolator allocates ~1400 floats every time and holds on to this in memory for the entire
 * lifetime of the object, while this one allocates only 10% of that.  We dont need high precision
 * for what we are using this for.
 */
public class KFPathInterpolator implements Interpolator {
  /**
   * Governs the accuracy of the approximation of the {@link Path}.
   */
  private static final float PRECISION = 0.03f;
  private final float[] mX;
  private final float[] mY;

  public KFPathInterpolator(float controlX1, float controlY1, float controlX2, float controlY2) {
    Path path = new Path();
    path.moveTo(0, 0);
    path.cubicTo(controlX1, controlY1, controlX2, controlY2, 1f, 1f);
    final PathMeasure pathMeasure = new PathMeasure(path, false /* forceClosed */);
    final float pathLength = pathMeasure.getLength();
    final int numPoints = (int) (pathLength / PRECISION) + 1;
    mX = new float[numPoints];
    mY = new float[numPoints];
    final float[] position = new float[2];
    for (int i = 0; i < numPoints; ++i) {
      final float distance = (i * pathLength) / (numPoints - 1);
      pathMeasure.getPosTan(distance, position, null /* tangent */);
      mX[i] = position[0];
      mY[i] = position[1];
    }
  }

  @Override
  public float getInterpolation(float t) {
    if (t <= 0.0f) {
      return 0.0f;
    } else if (t >= 1.0f) {
      return 1.0f;
    }
    // Do a binary search for the correct x to interpolate between.
    int startIndex = 0;
    int endIndex = mX.length - 1;
    while (endIndex - startIndex > 1) {
      int midIndex = (startIndex + endIndex) / 2;
      if (t < mX[midIndex]) {
        endIndex = midIndex;
      } else {
        startIndex = midIndex;
      }
    }
    final float xRange = mX[endIndex] - mX[startIndex];
    if (xRange == 0) {
      return mY[startIndex];
    }
    final float tInRange = t - mX[startIndex];
    final float fraction = tInRange / xRange;
    final float startY = mY[startIndex];
    final float endY = mY[endIndex];
    return startY + (fraction * (endY - startY));
  }
}
