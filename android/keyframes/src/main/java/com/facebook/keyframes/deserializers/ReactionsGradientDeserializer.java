// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.deserializers;

import java.io.IOException;

import android.util.JsonReader;

import com.facebook.keyframes.data.ReactionsGradient;

public class ReactionsGradientDeserializer {

  static final AbstractListDeserializer<ReactionsGradient> LIST_DESERIALIZER =
      new AbstractListDeserializer<ReactionsGradient>() {
        @Override
        ReactionsGradient readObjectImpl(JsonReader reader) throws IOException {
          return readObject(reader);
        }
      };

  public static ReactionsGradient readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    ReactionsGradient.Builder builder = new ReactionsGradient.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case ReactionsGradient.COLOR_START_JSON_FIELD:
          builder.colorStart = ReactionsGradientColorDeserializer.readObject(reader);
          break;
        case ReactionsGradient.COLOR_END_JSON_FIELD:
          builder.colorEnd = ReactionsGradientColorDeserializer.readObject(reader);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
