/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */
package com.facebook.keyframes;

import android.test.AndroidTestCase;

import com.facebook.keyframes.model.KFImage;
import com.facebook.keyframes.deserializers.KFImageDeserializer;

import junit.framework.Assert;

import java.io.InputStream;

public class DeserializerTest extends AndroidTestCase {

  public void testDeserializeValidFile() throws Exception {
    InputStream stream = getContext().getResources().getAssets().open("sample_like");
    KFImage deserializedModel = KFImageDeserializer.deserialize(stream);
    Assert.assertNotNull(deserializedModel);
  }

  // TODO: Test and catch various arg errors
}
