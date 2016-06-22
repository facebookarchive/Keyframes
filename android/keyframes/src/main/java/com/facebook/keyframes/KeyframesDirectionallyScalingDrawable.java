/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes;

public interface KeyframesDirectionallyScalingDrawable {

  /**
   * The direction to scale an image from the end.  If direction UP is used, the origin of the
   * scaling will be the bottom edge of the image.  Likewise, if DOWN is used, the origin will be
   * the top edge of the image.
   */
  enum ScaleDirection {
    UP,
    DOWN
  }

  /**
   * Sets special directional scale for the drawable.  This can cause the drawable to draw outside
   * of the initially set bounds, but retains image quality when scaling up/down and allows
   * fractional scaling not bound by integers.
   * @param scaleFromCenter Scale value from the center of the image
   * @param scaleFromEnd Scale value from the end of the image, either the bottom or top.
   * @param direction The {@link ScaleDirection} to scale towards, used for scaleFromEnd param
   */
  void setDirectionalScale(float scaleFromCenter, float scaleFromEnd, ScaleDirection direction);
}
