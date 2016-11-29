/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.model.keyframedmodels;

import android.util.SparseArray;
import android.view.animation.Interpolator;
import com.facebook.keyframes.model.HasKeyFrame;

import java.util.Collections;
import java.util.List;

/**
 * A generic object which holds key framed data and how to process information given a frame
 * progress.  This object contains a generic method to apply properties of this object to a
 * modifyable object M (e.g. Path / Matrix / Generic Container) given a frame progress.
 * @param <T> The type of animated object held by this KeyFramedObject
 * @param <M> A modifiable object passed into the apply function to
 */
public abstract class KeyFramedObject<T extends HasKeyFrame, M> {

  private final SparseArray<T> mObjects;
  private final int mFirstDescribedFrame;
  private final int mLastDescribedFrame;
  private final int[] mKeyFrames;

  private List<Interpolator> mInterpolators;
  private float[][][] mTimingCurves;

  public KeyFramedObject(List<T> objects, float[][][] timingCurves) {
    int listSize = objects.size();
    mObjects = new SparseArray<>(listSize);
    mKeyFrames = new int[listSize];
    T object;
    for (int i = 0; i < listSize; i++) {
      object = objects.get(i);
      mObjects.put(object.getKeyFrame(), object);
      mKeyFrames[i] = object.getKeyFrame();
    }

    mFirstDescribedFrame = mObjects.keyAt(0);
    mLastDescribedFrame = mObjects.keyAt(listSize - 1);
    mTimingCurves = timingCurves;
    mInterpolators = Collections.emptyList();  // This is populated when frame rate is available
  }

  /**
   * Constructor for creating empty/invalid KeyFramedObjects.
   */
  protected KeyFramedObject() {
    mObjects = null;
    mInterpolators = null;
    mFirstDescribedFrame = 0;
    mLastDescribedFrame = 0;
    mKeyFrames = null;
  }

  /**
   * Updates this object with the integer {@code frameRate} when available. This allows the object
   * to calculate an accurate list of interpolators internally based on the available timing curve
   * of each key frame.
   *
   * @param frameRate {@code int} that specifies the frame rate of the entire animation
   */
  public void updateWithFrameRate(int frameRate) {
    if (frameRate < 0 || mTimingCurves == null || mKeyFrames == null) {
      return;
    }

    mInterpolators =
            KeyFrameAnimationHelper.buildInterpolatorList(mTimingCurves, mKeyFrames, frameRate);
  }

  /**
   * Applies the corresponding values determined by frameProgress to the modifiable object.  This
   * method does some simple calculations to determine which key frames to use, as well as the
   * interpolation value between them based on the timing curve information, then delegates to
   * {@link #applyImpl(HasKeyFrame, HasKeyFrame, float, Object)} for the actual application to
   * the modifiable object.
   * @param frameProgress The progress, described in frames, of the animation.
   * @param modifiable The object to insert values into.
   */
  public void apply(float frameProgress, M modifiable) {
    if (mInterpolators.isEmpty() ||
        frameProgress <= mFirstDescribedFrame) {
      applyImpl(mObjects.get(mFirstDescribedFrame), null, 0, modifiable);
      return;
    }
    if (frameProgress >= mLastDescribedFrame) {
      applyImpl(mObjects.get(mLastDescribedFrame), null, 0, modifiable);
      return;
    }
    T thisFrame = null;
    T nextFrame = null;
    int interpolatorIndex;
    int len = mInterpolators.size();
    for (interpolatorIndex = 0; interpolatorIndex < len; interpolatorIndex++) {
      if (mObjects.keyAt(interpolatorIndex) == frameProgress ||
          (mObjects.keyAt(interpolatorIndex) < frameProgress &&
              mObjects.keyAt(interpolatorIndex + 1) > frameProgress)) {
        thisFrame = mObjects.valueAt(interpolatorIndex);
        nextFrame = mObjects.valueAt(interpolatorIndex + 1);
        break;
      }
    }
    float progress = (frameProgress - thisFrame.getKeyFrame()) /
        (nextFrame.getKeyFrame() - thisFrame.getKeyFrame());
    applyImpl(
        thisFrame,
        nextFrame,
        mInterpolators.get(interpolatorIndex).getInterpolation(progress),
        modifiable);
  }

  /**
   * Apply the given state to a modifiable.
   * @param stateA Initial state
   * @param stateB End state
   * @param interpolationValue Progress [0..1] between stateA and stateB
   * @param modifiable The modifiable object to apply the values to
   */
  protected abstract void applyImpl(
      T stateA,
      T stateB,
      float interpolationValue,
      M modifiable);

  /**
   * Given two values and the progress from valueA to valueB, returns the transitional value in
   * between.
   */
  protected static float interpolateValue(float valueA, float valueB, float progress) {
    return valueA + (valueB - valueA) * progress;
  }
}
