/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.deserializers;

import java.io.IOException;

import android.graphics.Color;
import android.util.JsonReader;

import com.facebook.keyframes.model.KFColorFrame;

/**
 * Deserializer for {@link KFColorFrame}.
 *
 * Root deserializer starts at {@link KFImageDeserializer}.
 */
public class KFColorFrameDeserializer {

  static final AbstractListDeserializer<KFColorFrame> LIST_DESERIALIZER =
      new AbstractListDeserializer<KFColorFrame>() {
        @Override
        KFColorFrame readObjectImpl(JsonReader reader) throws IOException {
          return readObject(reader);
        }
      };

  public static KFColorFrame readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    KFColorFrame.Builder builder = new KFColorFrame.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case KFColorFrame.START_FRAME_JSON_FIELD:
          builder.startFrame = reader.nextInt();
          break;
        case KFColorFrame.COLOR_JSON_FIELD:
          builder.color = Color.parseColor(reader.nextString());
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
