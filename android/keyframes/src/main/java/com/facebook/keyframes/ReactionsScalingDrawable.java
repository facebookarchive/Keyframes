// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes;

public interface ReactionsScalingDrawable {

  enum ScaleDirection {
    UP,
    DOWN
  }

  void setScaleForDrawing(float scaleFromCenter, float scaleFromEnd, ScaleDirection direction);
}
