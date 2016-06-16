// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.deserializers;

import java.io.IOException;

import android.graphics.Color;
import android.util.JsonReader;

import com.facebook.keyframes.data.ReactionsColorFrame;

public class ReactionsColorFrameDeserializer {

  static final AbstractListDeserializer<ReactionsColorFrame> LIST_DESERIALIZER =
      new AbstractListDeserializer<ReactionsColorFrame>() {
        @Override
        ReactionsColorFrame readObjectImpl(JsonReader reader) throws IOException {
          return readObject(reader);
        }
      };

  public static ReactionsColorFrame readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    ReactionsColorFrame.Builder builder = new ReactionsColorFrame.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case ReactionsColorFrame.START_FRAME_JSON_FIELD:
          builder.startFrame = reader.nextInt();
          break;
        case ReactionsColorFrame.COLOR_JSON_FIELD:
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
