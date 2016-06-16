// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.deserializers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.JsonReader;

import com.facebook.keyframes.data.ReactionsFace;

public class ReactionsFaceDeserializer {

  public static ReactionsFace deserialize(InputStream inputStream) throws IOException {
    JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
    return readObject(reader);
  }

  static ReactionsFace readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    ReactionsFace.Builder builder = new ReactionsFace.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case ReactionsFace.FRAME_RATE_JSON_FIELD:
          builder.frameRate = reader.nextInt();
          break;
        case ReactionsFace.FRAME_COUNT_JSON_FIELD:
          builder.frameCount = reader.nextInt();
          break;
        case ReactionsFace.FEATURES_JSON_FIELD:
          builder.features =
              ReactionsFeatureDeserializer.LIST_DESERIALIZER.readList(reader);
          break;
        case ReactionsFace.ANIMATION_GROUPS_JSON_FIELD:
          builder.animationGroups =
              ReactionsAnimationGroupDeserializer.LIST_DESERIALIZER.readList(reader);
          break;
        case ReactionsFace.CANVAS_SIZE_JSON_FIELD:
          builder.canvasSize = CommonDeserializerHelper.readFloatArray(reader);
          break;
        case ReactionsFace.KEY_JSON_FIELD:
          builder.key = reader.nextInt();
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
