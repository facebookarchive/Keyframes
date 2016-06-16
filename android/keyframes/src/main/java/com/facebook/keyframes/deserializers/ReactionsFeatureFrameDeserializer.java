// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.deserializers;

import java.io.IOException;

import android.util.JsonReader;

import com.facebook.keyframes.data.ReactionsFeatureFrame;

public class ReactionsFeatureFrameDeserializer {

  static final AbstractListDeserializer<ReactionsFeatureFrame> LIST_DESERIALIZER =
      new AbstractListDeserializer<ReactionsFeatureFrame>() {
        @Override
        ReactionsFeatureFrame readObjectImpl(JsonReader reader) throws IOException {
          return readObject(reader);
        }
      };

  public static ReactionsFeatureFrame readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    ReactionsFeatureFrame.Builder builder = new ReactionsFeatureFrame.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case ReactionsFeatureFrame.START_FRAME_JSON_FIELD:
          builder.startFrame = reader.nextInt();
          break;
        case ReactionsFeatureFrame.DATA_JSON_FIELD:
          builder.data = CommonDeserializerHelper.STRING_LIST_DESERIALIZER.readList(reader);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
