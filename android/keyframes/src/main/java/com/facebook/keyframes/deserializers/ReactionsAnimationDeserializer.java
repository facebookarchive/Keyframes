// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.deserializers;

import java.io.IOException;
import java.util.Locale;

import android.util.JsonReader;

import com.facebook.keyframes.data.ReactionsAnimation;

public class ReactionsAnimationDeserializer {

  static final AbstractListDeserializer<ReactionsAnimation> LIST_DESERIALIZER =
      new AbstractListDeserializer<ReactionsAnimation>() {
    @Override
    ReactionsAnimation readObjectImpl(JsonReader reader) throws IOException {
      return readObject(reader);
    }
  };

  public static ReactionsAnimation readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    ReactionsAnimation.Builder builder = new ReactionsAnimation.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case ReactionsAnimation.PROPERTY_TYPE_JSON_FIELD:
          builder.propertyType = ReactionsAnimation.PropertyType.valueOf(
              reader.nextString().toUpperCase(Locale.US));
          break;
        case ReactionsAnimation.ANIMATION_FRAMES_JSON_FIELD:
          builder.animationFrames =
              ReactionsAnimationFrameDeserializer.LIST_DESERIALIZER.readList(reader);
          break;
        case ReactionsAnimation.TIMING_CURVES_JSON_FIELD:
          builder.timingCurves = CommonDeserializerHelper.read3DFloatArray(reader);
          break;
        case ReactionsAnimation.ANCHOR_JSON_FIELD:
          builder.anchor = CommonDeserializerHelper.readFloatArray(reader);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
