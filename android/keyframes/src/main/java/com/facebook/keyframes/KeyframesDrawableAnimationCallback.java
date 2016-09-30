/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Choreographer;

import java.lang.ref.WeakReference;

import com.facebook.keyframes.model.KFImage;

/**
 * A simple callback that when run, will call back indefinitely with progress updates until
 * cancelled.  This will continuously feed back progress data from [0, 1] calculated by millis
 * per loop.
 */
public abstract class KeyframesDrawableAnimationCallback {

  /**
   * And interface for a class which wants to listen to progress updates.
   */
  public interface FrameListener {
    void onProgressUpdate(float frameProgress);
    void onStop();
  }

  private final WeakReference<FrameListener> mListener;
  private final int mFrameCount;
  private final int mMillisPerLoop;

  private long mStartTimeMillis;
  private boolean mStopAtLoopEnd;
  private int mCurrentLoopNumber;

  private long mMinimumMillisBetweenProgressUpdates = -1;
  private long mPreviousProgressMillis = 0;

  /**
   * Creates a KeyframesDrawableAnimationCallback appropriate for the API level of the device.
   * @param listener The listener that will receive callbacks on updates to the value
   * @return A KeyframesDrawableAnimationCallback implementation
   */
  public static KeyframesDrawableAnimationCallback create(
      FrameListener listener,
      KFImage face) {
    if (hasChoreographer()) {
      return new FrameCallbackFaceAnimationCallback(
          listener,
          face.getFrameRate(),
          face.getFrameCount());
    } else {
      return new RunnableFaceAnimationCallback(
          listener,
          face.getFrameRate(),
          face.getFrameCount());
    }
  }

  private static boolean hasChoreographer() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
  }

  private KeyframesDrawableAnimationCallback(FrameListener listener, int frameRate, int frameCount) {
    mListener = new WeakReference<>(listener);
    mFrameCount = frameCount;
    mMillisPerLoop = Math.round(1000 * ((float) frameCount / frameRate));
  }

  /**
   * Set the maximum frame rate for this animation.
   * Consider using this for low end devices.
   * @param maxFrameRate
   */
  public void setMaxFrameRate(int maxFrameRate) {
    mMinimumMillisBetweenProgressUpdates = 1000 / maxFrameRate;
  }

  protected abstract void postCallback();

  protected abstract void cancelCallback();

  /**
   * Starts this animation callback.
   *
   * !IMPORTANT! This animator will run indefinitely, so it must be cancelled via #stop() when no
   * longer in use!
   */
  public void start() {
    mStopAtLoopEnd = false;
    cancelCallback();
    postCallback();
  }

  /**
   * Stops the callbacks animation and resets the start time.
   */
  public void stop() {
    cancelCallback();
    mStartTimeMillis = 0;
    mCurrentLoopNumber = -1;
    mListener.get().onStop();
  }

  /**
   * Stops looping the animation, but finishes the current animation.
   */
  public void stopAtLoopEnd() {
    mStopAtLoopEnd = true;
  }

  protected void advanceAnimation(final long frameTimeMillis) {
    if (mListener.get() == null) {
      cancelCallback();
      mStartTimeMillis = 0;
      mPreviousProgressMillis = 0;
      mCurrentLoopNumber = -1;
      return;
    }
    if (mStartTimeMillis == 0) {
      mStartTimeMillis = frameTimeMillis;
    }
    int currentLoopNumber = (int) (frameTimeMillis - mStartTimeMillis) / mMillisPerLoop;
    final boolean loopHasEnded = currentLoopNumber > mCurrentLoopNumber;
    if (mStopAtLoopEnd && loopHasEnded) {
      mListener.get().onProgressUpdate(mFrameCount);
      stop();
      return;
    }
    long currentProgressMillis = (frameTimeMillis - mStartTimeMillis) % mMillisPerLoop;

    boolean shouldUpdateProgress = true;
    if (frameTimeMillis - mPreviousProgressMillis < mMinimumMillisBetweenProgressUpdates) {
      shouldUpdateProgress = false;
    } else {
      mPreviousProgressMillis = frameTimeMillis;
    }
    if (shouldUpdateProgress) {
      mListener.get().onProgressUpdate((float) currentProgressMillis / mMillisPerLoop * mFrameCount);
    }
    mCurrentLoopNumber = (int) (frameTimeMillis - mStartTimeMillis) / mMillisPerLoop;
    postCallback();
  }

  @TargetApi(16)
  private static class FrameCallbackFaceAnimationCallback extends KeyframesDrawableAnimationCallback
      implements Choreographer.FrameCallback {

    private final Choreographer mChoreographer;

    private FrameCallbackFaceAnimationCallback(
        FrameListener listener,
        int frameRate,
        int frameCount) {
      super(listener, frameRate, frameCount);
      mChoreographer = Choreographer.getInstance();
    }

    @Override
    public void doFrame(long frameTimeNanos) {
      advanceAnimation(frameTimeNanos / 1000000); // nanoseconds per millisecond
    }

    @Override
    protected void postCallback() {
      mChoreographer.postFrameCallback(this);
    }

    @Override
    protected void cancelCallback() {
      mChoreographer.removeFrameCallback(this);
    }
  }

  private static class RunnableFaceAnimationCallback extends KeyframesDrawableAnimationCallback
      implements Runnable {
    private static final int ANIMATION_MIN_STEP_TIME_MS = 25; // 40 fps

    private final Handler mHandler;

    private RunnableFaceAnimationCallback(
        FrameListener listener,
        int frameRate,
        int frameCount) {
      super(listener, frameRate, frameCount);
      mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void run() {
      advanceAnimation(SystemClock.uptimeMillis());
    }

    @Override
    protected void postCallback() {
      mHandler.postDelayed(this, ANIMATION_MIN_STEP_TIME_MS);
    }

    @Override
    protected void cancelCallback() {
      mHandler.removeCallbacks(this);
    }
  }
}
