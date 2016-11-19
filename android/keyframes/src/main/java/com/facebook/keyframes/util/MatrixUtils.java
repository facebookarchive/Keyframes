/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.util;

import android.graphics.Matrix;

/**
 * Utility methods for working with matrices.  For the sake of not allocating many redundant arrays,
 * the current implementation doesn't have great thread safety.
 */
public class MatrixUtils {

  private final static float[] MATRIX_VALUE_RECYCLABLE_ARRAY = new float[9];

  public static synchronized float extractScaleFromMatrix(Matrix matrix) {
    matrix.getValues(MATRIX_VALUE_RECYCLABLE_ARRAY);
    return (Math.abs(MATRIX_VALUE_RECYCLABLE_ARRAY[0]) +
        Math.abs(MATRIX_VALUE_RECYCLABLE_ARRAY[4])) / 2f;
  }
}
