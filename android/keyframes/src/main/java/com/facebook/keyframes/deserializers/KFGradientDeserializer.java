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

import android.util.JsonReader;

import com.facebook.keyframes.model.KFGradient;

/**
 * Deserializer for {@link KFGradient}.
 *
 * Root deserializer starts at {@link KFImageDeserializer}.
 */
public class KFGradientDeserializer {

  static final AbstractListDeserializer<KFGradient> LIST_DESERIALIZER =
      new AbstractListDeserializer<KFGradient>() {
        @Override
        KFGradient readObjectImpl(JsonReader reader) throws IOException {
          return readObject(reader);
        }
      };

  public static KFGradient readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    KFGradient.Builder builder = new KFGradient.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case KFGradient.COLOR_START_JSON_FIELD:
          builder.colorStart = KFGradientColorDeserializer.readObject(reader);
          break;
        case KFGradient.COLOR_END_JSON_FIELD:
          builder.colorEnd = KFGradientColorDeserializer.readObject(reader);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
