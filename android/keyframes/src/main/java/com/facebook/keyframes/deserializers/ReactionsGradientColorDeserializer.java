// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.deserializers;

import java.io.IOException;

import android.util.JsonReader;

import com.facebook.keyframes.data.ReactionsGradientColor;

public class ReactionsGradientColorDeserializer {

  static final AbstractListDeserializer<ReactionsGradientColor> LIST_DESERIALIZER =
      new AbstractListDeserializer<ReactionsGradientColor>() {
        @Override
        ReactionsGradientColor readObjectImpl(JsonReader reader) throws IOException {
          return readObject(reader);
        }
      };

  public static ReactionsGradientColor readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    ReactionsGradientColor.Builder builder = new ReactionsGradientColor.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case ReactionsGradientColor.KEY_VALUES_JSON_FIELD:
          builder.keyValues = ReactionsColorFrameDeserializer.LIST_DESERIALIZER.readList(reader);
          break;
        case ReactionsGradientColor.TIMING_CURVES_JSON_FIELD:
          builder.timingCurves = CommonDeserializerHelper.read3DFloatArray(reader);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
