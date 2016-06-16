// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.deserializers;

import java.io.IOException;

import android.util.JsonReader;

import com.facebook.keyframes.data.ReactionsFeatureEffect;

public class ReactionsFeatureEffectDeserializer {

  static final AbstractListDeserializer<ReactionsFeatureEffect> LIST_DESERIALIZER =
      new AbstractListDeserializer<ReactionsFeatureEffect>() {
        @Override
        ReactionsFeatureEffect readObjectImpl(JsonReader reader) throws IOException {
          return readObject(reader);
        }
      };

  public static ReactionsFeatureEffect readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    ReactionsFeatureEffect.Builder builder = new ReactionsFeatureEffect.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case ReactionsFeatureEffect.GRADIENT_JSON_FIELD:
          builder.gradient = ReactionsGradientDeserializer.readObject(reader);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
