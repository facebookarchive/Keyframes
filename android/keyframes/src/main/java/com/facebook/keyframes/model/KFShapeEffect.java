/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.model;

import com.facebook.keyframes.util.ArgCheckUtil;

/**
 * Describes an effect that can be applied to a shape layer.  Currently, only a simple linear
 * gradient effect is supported.
 */
public class KFShapeEffect {

  /**
   * The object containing gradient and gradient animation information.
   */
  public static final String GRADIENT_JSON_FIELD = "gradient";
  private final KFGradient mGradient;

  public static class Builder {
    public KFGradient gradient;

    public KFShapeEffect build() {
      return new KFShapeEffect(gradient);
    }
  }

  private KFShapeEffect(KFGradient gradient) {
    mGradient = ArgCheckUtil.checkArg(
        gradient,
        gradient != null,
        GRADIENT_JSON_FIELD);
  }

  public KFGradient getGradient() {
    return mGradient;
  }
}
