/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.keyframes.model;

import com.facebook.keyframes.util.ArgCheckUtil;

/**
 * Describes an effect that can be applied to a feature layer.  Currently, only a simple linear
 * gradient effect is supported.
 */
public class KFFeatureEffect {

  /**
   * The object containing gradient and gradient animation information.
   */
  public static final String GRADIENT_JSON_FIELD = "gradient";
  private final KFGradient mGradient;

  public static class Builder {
    public KFGradient gradient;

    public KFFeatureEffect build() {
      return new KFFeatureEffect(gradient);
    }
  }

  private KFFeatureEffect(KFGradient gradient) {
    mGradient = ArgCheckUtil.checkArg(
        gradient,
        gradient != null,
        GRADIENT_JSON_FIELD);
  }

  public KFGradient getGradient() {
    return mGradient;
  }
}
