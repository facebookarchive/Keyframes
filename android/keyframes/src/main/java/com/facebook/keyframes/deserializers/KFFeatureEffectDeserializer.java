/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.deserializers;

import java.io.IOException;

import android.util.JsonReader;

import com.facebook.keyframes.model.KFFeatureEffect;

/**
 * Deserializer for {@link KFFeatureEffect}.
 *
 * Root deserializer starts at {@link KFImageDeserializer}.
 */
public class KFFeatureEffectDeserializer {

  public static KFFeatureEffect readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    KFFeatureEffect.Builder builder = new KFFeatureEffect.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case KFFeatureEffect.GRADIENT_JSON_FIELD:
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
