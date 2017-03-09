/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.keyframes.deserializers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.JsonReader;

import com.facebook.keyframes.model.KFImage;

/**
 * Deserializer for {@link KFImage}.  This is the root image object deserializer and contains the
 * {@link #deserialize(InputStream)} method, which takes an {@link InputStream} and attempts to
 * deserialize a full {@link KFImage} object from it.
 */
public class KFImageDeserializer {

  /**
   * Given an input stream, attempt to deserialize a {@link KFImage} and return it.
   * @param inputStream An input stream containing JSON for a {@link KFImage}
   * @return A deserialized {@link KFImage} object
   * @throws IOException
   */
  public static KFImage deserialize(InputStream inputStream) throws IOException {
    JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
    return readObject(reader);
  }

  static KFImage readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    KFImage.Builder builder = new KFImage.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case KFImage.FRAME_RATE_JSON_FIELD:
          builder.frameRate = reader.nextInt();
          break;
        case KFImage.FRAME_COUNT_JSON_FIELD:
          builder.frameCount = reader.nextInt();
          break;
        case KFImage.FEATURES_JSON_FIELD:
          builder.features =
              KFFeatureDeserializer.LIST_DESERIALIZER.readList(reader);
          break;
        case KFImage.ANIMATION_GROUPS_JSON_FIELD:
          builder.animationGroups =
              KFAnimationGroupDeserializer.LIST_DESERIALIZER.readList(reader);
          break;
        case KFImage.CANVAS_SIZE_JSON_FIELD:
          builder.canvasSize = CommonDeserializerHelper.readFloatArray(reader);
          break;
        case KFImage.KEY_JSON_FIELD:
          builder.key = reader.nextInt();
          break;
        case KFImage.BITMAPS_JSON_FIELD:
          builder.bitmaps = readBitmaps(reader);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }

  private static Map<String, Bitmap> readBitmaps(JsonReader reader) throws IOException {
    reader.beginObject();
    Map<String, Bitmap> bitmaps = new HashMap<>();
    while (reader.hasNext()) {
      String name = reader.nextName();
      byte[] bytes = Base64.decode(reader.nextString(), Base64.DEFAULT);
      Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
      bitmaps.put(name, bitmap);
    }
    reader.endObject();
    return bitmaps;
  }
}
