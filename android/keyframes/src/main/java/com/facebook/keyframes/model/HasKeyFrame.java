/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.model;

/**
 * An interface to identify an object which has a key frame associated with it.
 */
public interface HasKeyFrame {
  int getKeyFrame();
}
