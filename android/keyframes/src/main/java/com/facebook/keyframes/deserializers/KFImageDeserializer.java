/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.deserializers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
