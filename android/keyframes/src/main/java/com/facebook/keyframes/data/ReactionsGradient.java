// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.data;

import android.graphics.LinearGradient;
import android.graphics.Shader;

import com.facebook.keyframes.data.keyframedmodels.KeyFramedGradient;
import com.facebook.keyframes.util.ArgCheckUtil;

import static com.facebook.keyframes.data.keyframedmodels.KeyFramedGradient.Position.*;

/**
 * An object which wraps information for a gradient effect.  Currently only supports simple linear
 * gradients.
 */
public class ReactionsGradient {

  /**
   * To prevent allocating a lot of LinearGradient shaders during animation, and because it looks
   * looks like LinearGradient shader params can't be modified after instantiation, we cache the
   * shaders needed once at a precision of shaders per second listed here.
   */

  /**
   * The start color of the gradient, the top color of the linear gradient.
   */
  public static final String COLOR_START_JSON_FIELD = "color_start";
  private final KeyFramedGradient mStartGradient;

  /**
   * The end color of the gradient, the bottom color of the linear gradient.
   */
  public static final String COLOR_END_JSON_FIELD = "color_end";
  private final KeyFramedGradient mEndGradient;

  public static class Builder {
    public ReactionsGradientColor colorStart;
    public ReactionsGradientColor colorEnd;

    public ReactionsGradient build() {
      return new ReactionsGradient(colorStart, colorEnd);
    }
  }

  public ReactionsGradient(ReactionsGradientColor colorStart, ReactionsGradientColor colorEnd) {
    mStartGradient = KeyFramedGradient.fromGradient(
        ArgCheckUtil.checkArg(
            colorStart,
            colorStart != null,
            COLOR_START_JSON_FIELD),
        START);
    mEndGradient = KeyFramedGradient.fromGradient(
        ArgCheckUtil.checkArg(
            colorEnd,
            colorEnd != null,
            COLOR_END_JSON_FIELD),
        END);
  }

  public KeyFramedGradient getStartGradient() {
    return mStartGradient;
  }

  public KeyFramedGradient getEndGradient() {
    return mEndGradient;
  }
}