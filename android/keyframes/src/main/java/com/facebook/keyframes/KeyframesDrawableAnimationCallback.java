/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.keyframes;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Choreographer;

import com.facebook.keyframes.model.KFImage;

import java.lang.ref.WeakReference;

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
  // This flag is used to prevent posting callbacks after the animation is stopped.
  private boolean mStopped;
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
   * Starts this animation callback and resets the start time.
   *
   * !IMPORTANT! This animator will run indefinitely, so it must be cancelled via #stop()
   * or #pause() when no longer in use!
   */
  public void start() {
    mStopped = false;
    mStopAtLoopEnd = false;
    mStartTimeMillis = 0;
    mCurrentLoopNumber = 0;
    cancelCallback();
    postCallback();
  }

  /**
   * Starts the animation and plays it once
   */
  public void playOnce() {
    mStopped = false;
    mStopAtLoopEnd = true;
    mStartTimeMillis = 0;
    mCurrentLoopNumber = 0;
    cancelCallback();
    postCallback();
  }

  /**
   * Stops the callbacks animation and resets the start time.
   */
  public void stop() {
    mStopped = true;
    cancelCallback();
    mStartTimeMillis = 0;
    mCurrentLoopNumber = -1;
    mListener.get().onStop();
  }

  /**
   * Pauses the callbacks animation and saves start time.
   */
  public void pause() {
    cancelCallback();
    mStartTimeMillis *= -1;
  }

  /**
   * Resumes this animation callback.
   *
   * !IMPORTANT! This animator will run indefinitely, so it must be cancelled via #stop()
   * or #pause() when no longer in use!
   */
  public void resume() {
    mStopAtLoopEnd = false;
    cancelCallback();
    postCallback();
  }


  /**
   * Stops looping the animation, but finishes the current animation.
   */
  public void stopAtLoopEnd() {
    mStopAtLoopEnd = true;
  }

  protected void advanceAnimation(final long frameTimeMillis) {
    // hold a strong reference to the listener to prevent getting a null during this method.
    FrameListener listener = mListener.get();
    if (listener == null) {
      cancelCallback();
      mStartTimeMillis = 0;
      mPreviousProgressMillis = 0;
      mCurrentLoopNumber = -1;
      return;
    }
    if (mStartTimeMillis == 0) {
      mStartTimeMillis = frameTimeMillis;
    } else if (mStartTimeMillis < 0) {
      long pausedTimeMillis = frameTimeMillis - mPreviousProgressMillis;
      mStartTimeMillis = mStartTimeMillis * -1 + pausedTimeMillis;
      mPreviousProgressMillis += pausedTimeMillis;
    }

    int currentLoopNumber = (int) (frameTimeMillis - mStartTimeMillis) / mMillisPerLoop;
    final boolean loopHasEnded = currentLoopNumber > mCurrentLoopNumber;
    if (mStopAtLoopEnd && loopHasEnded) {
      listener.onProgressUpdate(mFrameCount);
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
      listener.onProgressUpdate((float) currentProgressMillis / mMillisPerLoop * mFrameCount);
    }
    mCurrentLoopNumber = (int) (frameTimeMillis - mStartTimeMillis) / mMillisPerLoop;
    if (!mStopped) {
      postCallback();
    }
  }

  @TargetApi(16)
  private static class FrameCallbackFaceAnimationCallback extends KeyframesDrawableAnimationCallback
      implements Choreographer.FrameCallback {

    private static Choreographer sUIChoreographer;

    private FrameCallbackFaceAnimationCallback(
        FrameListener listener,
        int frameRate,
        int frameCount) {
      super(listener, frameRate, frameCount);
    }

    @Override
    public void doFrame(long frameTimeNanos) {
      advanceAnimation(frameTimeNanos / 1000000); // nanoseconds per millisecond
    }

    @Override
    protected void postCallback() {
      if (sUIChoreographer == null) {
        sUIChoreographer = Choreographer.getInstance();
      }
      sUIChoreographer.postFrameCallback(this);
    }

    @Override
    protected void cancelCallback() {
      if (sUIChoreographer == null) {
        sUIChoreographer = Choreographer.getInstance();
      }
      sUIChoreographer.removeFrameCallback(this);
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
