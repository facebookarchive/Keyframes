// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.deserializers;

import java.io.IOException;

import android.util.JsonReader;

import com.facebook.keyframes.data.ReactionsAnimationGroup;

public class ReactionsAnimationGroupDeserializer {

  static final AbstractListDeserializer<ReactionsAnimationGroup> LIST_DESERIALIZER =
      new AbstractListDeserializer<ReactionsAnimationGroup>() {
        @Override
        ReactionsAnimationGroup readObjectImpl(JsonReader reader) throws IOException {
          return readObject(reader);
        }
      };

  public static ReactionsAnimationGroup readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    ReactionsAnimationGroup.Builder builder = new ReactionsAnimationGroup.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case ReactionsAnimationGroup.GROUP_ID_JSON_FIELD:
          builder.groupId = reader.nextInt();
          break;
        case ReactionsAnimationGroup.PARENT_GROUP_JSON_FIELD:
          builder.parentGroup = reader.nextInt();
          break;
        case ReactionsAnimationGroup.ANIMATIONS_JSON_FIELD:
          builder.animations = ReactionsAnimationDeserializer.LIST_DESERIALIZER.readList(reader);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
