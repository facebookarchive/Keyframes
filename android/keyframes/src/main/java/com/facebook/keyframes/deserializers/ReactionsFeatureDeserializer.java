// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.deserializers;

import java.io.IOException;

import android.graphics.Color;
import android.util.JsonReader;

import com.facebook.keyframes.data.ReactionsFeature;

public class ReactionsFeatureDeserializer {

  static final AbstractListDeserializer<ReactionsFeature> LIST_DESERIALIZER =
      new AbstractListDeserializer<ReactionsFeature>() {
        @Override
        ReactionsFeature readObjectImpl(JsonReader reader) throws IOException {
          return readObject(reader);
        }
      };

  public static ReactionsFeature readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    ReactionsFeature.Builder builder = new ReactionsFeature.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case ReactionsFeature.NAME_JSON_FIELD:
          builder.name = reader.nextString();
          break;
        case ReactionsFeature.FILL_COLOR_JSON_FIELD:
          builder.fillColor = Color.parseColor(reader.nextString());
          break;
        case ReactionsFeature.STROKE_COLOR_JSON_FIELD:
          builder.strokeColor = Color.parseColor(reader.nextString());
          break;
        case ReactionsFeature.STROKE_WIDTH_JSON_FIELD:
          builder.strokeWidth= (float) reader.nextDouble();
          break;
        case ReactionsFeature.KEY_FRAMES_JSON_FIELD:
          builder.keyFrames = ReactionsFeatureFrameDeserializer.LIST_DESERIALIZER.readList(reader);
          break;
        case ReactionsFeature.TIMING_CURVES_JSON_FIELD:
          builder.timingCurves = CommonDeserializerHelper.read3DFloatArray(reader);
          break;
        case ReactionsFeature.ANIMATION_GROUP_JSON_FIELD:
          builder.animationGroup = reader.nextInt();
          break;
        case ReactionsFeature.FEATURE_ANIMATIONS_JSON_FIELD:
          builder.featureAnimations =
              ReactionsAnimationDeserializer.LIST_DESERIALIZER.readList(reader);
          break;
        case ReactionsFeature.EFFECT_JSON_FIELD:
          builder.effect = ReactionsFeatureEffectDeserializer.readObject(reader);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
