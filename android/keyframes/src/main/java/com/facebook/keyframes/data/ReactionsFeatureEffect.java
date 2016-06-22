// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.data;

import com.facebook.keyframes.util.ArgCheckUtil;

/**
 * Describes an effect that can be applied to a shape layer.  Currently, only a simple linear
 * gradient effect is supported.
 */
public class ReactionsFeatureEffect {

  /**
   * The object containing gradient and gradient animation information.
   */
  public static final String GRADIENT_JSON_FIELD = "gradient";
  private final ReactionsGradient mGradient;

  public static class Builder {
    public ReactionsGradient gradient;

    public ReactionsFeatureEffect build() {
      return new ReactionsFeatureEffect(gradient);
    }
  }

  private ReactionsFeatureEffect(ReactionsGradient gradient) {
    mGradient = ArgCheckUtil.checkArg(
        gradient,
        gradient != null,
        GRADIENT_JSON_FIELD);
  }

  public ReactionsGradient getGradient() {
    return mGradient;
  }
}
