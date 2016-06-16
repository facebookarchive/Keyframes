// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.deserializers;

import java.io.IOException;

import android.util.JsonReader;

import com.facebook.keyframes.data.ReactionsAnimationFrame;

public class ReactionsAnimationFrameDeserializer {

  static final AbstractListDeserializer<ReactionsAnimationFrame> LIST_DESERIALIZER =
      new AbstractListDeserializer<ReactionsAnimationFrame>() {
        @Override
        ReactionsAnimationFrame readObjectImpl(JsonReader reader) throws IOException {
          return readObject(reader);
        }
      };

  public static ReactionsAnimationFrame readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    ReactionsAnimationFrame.Builder builder = new ReactionsAnimationFrame.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case ReactionsAnimationFrame.START_FRAME_JSON_FIELD:
          builder.startFrame = reader.nextInt();
          break;
        case ReactionsAnimationFrame.DATA_JSON_FIELD:
          builder.data = CommonDeserializerHelper.readFloatArray(reader);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
