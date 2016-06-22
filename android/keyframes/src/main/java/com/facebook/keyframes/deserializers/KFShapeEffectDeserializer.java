/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.deserializers;

import java.io.IOException;

import android.util.JsonReader;

import com.facebook.keyframes.model.KFShapeEffect;

/**
 * Deserializer for {@link KFShapeEffect}.
 *
 * Root deserializer starts at {@link KFImageDeserializer}.
 */
public class KFShapeEffectDeserializer {

  public static KFShapeEffect readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    KFShapeEffect.Builder builder = new KFShapeEffect.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case KFShapeEffect.GRADIENT_JSON_FIELD:
          builder.gradient = KFGradientDeserializer.readObject(reader);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
