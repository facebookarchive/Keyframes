/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.keyframes;

import android.graphics.Bitmap;

import com.facebook.keyframes.model.KFImage;

import java.util.Map;

/**
 * A class for building an instance of the Keyframes drawable.  This class includes setting configs
 * allowed on a KeyframesDrawable, as well as access to experimental features via
 * {@link ExperimentalFeatures}.
 */
public class KeyframesDrawableBuilder {

  private KFImage mImage;
  private int mMaxFrameRate = 60;
  private ExperimentalFeatures mExperimentalFeatures = new ExperimentalFeatures();

  public KeyframesDrawable build() {
    if (mImage == null) {
      throw new IllegalArgumentException("No KFImage provided!");
    }
    return new KeyframesDrawable(this);
  }

  public KeyframesDrawableBuilder withImage(KFImage image) {
    mImage = image;
    return this;
  }

  KFImage getImage() {
    return mImage;
  }

  public KeyframesDrawableBuilder withMaxFrameRate(int maxFrameRate) {
    mMaxFrameRate = maxFrameRate;
    return this;
  }

  int getMaxFrameRate() {
    return mMaxFrameRate;
  }

  // Experimental features below.  APIs are volatile and subject to change.  Use with care!

  public ExperimentalFeatures withExperimentalFeatures() {
    return mExperimentalFeatures;
  }

  ExperimentalFeatures getExperimentalFeatures() {
    return mExperimentalFeatures;
  }

  /**
   * Experimental features that are subject to drastic changes.  Use with care!
   */
  public class ExperimentalFeatures {
    private Map<String, Bitmap> mBitmaps;
    private boolean mClipToAECanvas = false;

    public KeyframesDrawable build() {
      return KeyframesDrawableBuilder.this.build();
    }

    public ExperimentalFeatures withBitmaps() {
      mBitmaps = mImage.getBitmaps();
      return ExperimentalFeatures.this;
    }

    Map<String, Bitmap> getBitmaps() {
      return mBitmaps;
    }

    public ExperimentalFeatures clipToAECanvas() {
      mClipToAECanvas = true;
      return ExperimentalFeatures.this;
    }

    boolean getClipToAECanvas() {
      return mClipToAECanvas;
    }
  }
}
