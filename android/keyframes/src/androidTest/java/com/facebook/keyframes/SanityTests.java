/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */
package com.facebook.keyframes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.test.AndroidTestCase;

import com.facebook.keyframes.model.KFImage;
import com.facebook.keyframes.deserializers.KFImageDeserializer;

import junit.framework.Assert;

import java.io.InputStream;

public class SanityTests extends AndroidTestCase {

  private static final int TEST_CANVAS_SIZE_PX = 500;

  /**
   * Sanity deserializer test to make sure a valid file deserializes into a proper object.
   */
  public void testDeserializeValidFile() throws Exception {
    InputStream stream = getContext().getResources().getAssets().open("sample_file");
    KFImage deserializedModel = KFImageDeserializer.deserialize(stream);
    Assert.assertNotNull(deserializedModel);
  }

  /**
   * Sanity rendering test that renders the sample image and checks frames at 10% progress
   * increments, comparing images with a pre-generated set.
   */
  public void testFramesForSample() throws Exception {
    InputStream stream = getContext().getResources().getAssets().open("sample_file");
    KFImage kfImage = KFImageDeserializer.deserialize(stream);
    Bitmap testBitmap =
        Bitmap.createBitmap(TEST_CANVAS_SIZE_PX, TEST_CANVAS_SIZE_PX, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(testBitmap);

    KeyframesDrawable testDrawable = new KeyframesDrawableBuilder().withImage(kfImage).build();
    testDrawable.setBounds(0, 0, TEST_CANVAS_SIZE_PX, TEST_CANVAS_SIZE_PX);

    float step = .1f;
    for (float progress = 0; progress <= 1; progress += step) {
      int resIdentifier =
          getContext().getResources().getIdentifier(
              "test_" + (int) (progress / step),
              "drawable",
              getContext().getPackageName());
      Bitmap compareBitmap =
          BitmapFactory.decodeStream(getContext().getResources().openRawResource(resIdentifier));

      testDrawable.setFrameProgress(kfImage.getFrameCount() * progress);
      canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
      testDrawable.draw(canvas);
      Assert.assertTrue(testBitmap.sameAs(compareBitmap));
    }
  }
}
