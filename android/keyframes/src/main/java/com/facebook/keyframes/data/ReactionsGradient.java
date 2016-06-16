// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.data;

import android.graphics.LinearGradient;
import android.graphics.Shader;

import com.facebook.keyframes.data.keyframedmodels.KeyFramedGradient;

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
   * TODO: markpeng we can eliminate shaders needed by capping the ends
   */
  private static final float GRADIENT_PRECISION_PER_SECOND = 30;

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

  /**
   * The cached shaders which will be used for this animation.
   */
  private LinearGradient[] mShaders;

  /**
   * The total number of frames for this animation.
   * TODO: markpeng model state cleanup
   */
  private int mTotalFrames;

  public static class Builder {
    public ReactionsGradientColor colorStart;
    public ReactionsGradientColor colorEnd;

    public ReactionsGradient build() {
      return new ReactionsGradient(colorStart, colorEnd);
    }
  }

  public ReactionsGradient(ReactionsGradientColor colorStart, ReactionsGradientColor colorEnd) {
    mStartGradient = KeyFramedGradient.fromGradient(colorStart, START);
    mEndGradient = KeyFramedGradient.fromGradient(colorEnd, END);
  }

  /**
   * Prepares and allocates the shaders needed for this gradient ahead of time.
   * TODO: markpeng model state cleanup
   * @param canvasHeight The height of the canvas from the corresponding ReactionsFace.
   * @param frameRate The framerate from the corresponding ReactionsFace
   * @param numFrames The number of total frames from the corresponding ReactionsFace.
   */
  public void prepareShaders(float canvasHeight, int frameRate, int numFrames) {
    if (mShaders != null) {
      return;
    }

    int precision = Math.round(GRADIENT_PRECISION_PER_SECOND * numFrames / frameRate);
    mShaders = new LinearGradient[precision + 1];
    mTotalFrames = numFrames;
    float progress;
    KeyFramedGradient.GradientColorPair colorPair = new KeyFramedGradient.GradientColorPair();
    for (int i = 0; i < precision; i++) {
      progress = i / (float) (precision) * numFrames;
      getStartGradient().apply(progress, colorPair);
      getEndGradient().apply(progress, colorPair);
      mShaders[i] = new LinearGradient(
          0,
          0,
          0,
          canvasHeight,
          colorPair.getStartColor(),
          colorPair.getEndColor(),
          Shader.TileMode.CLAMP);
    }
  }

  /**
   * Given the progress in the animation in frames, this method returns the nearest shader we have.
   * @param frameProgress The precise frame that we are currently on in the animation
   * @return The nearest LinearGradient shader we have to the one needed for this frame.
   */
  public Shader getNearestShaderForFrame(float frameProgress) {
    int shaderIndex = (int) ((frameProgress / mTotalFrames) * (mShaders.length - 1));
    return mShaders[shaderIndex];
  }

  private KeyFramedGradient getStartGradient() {
    return mStartGradient;
  }

  private KeyFramedGradient getEndGradient() {
    return mEndGradient;
  }
}
