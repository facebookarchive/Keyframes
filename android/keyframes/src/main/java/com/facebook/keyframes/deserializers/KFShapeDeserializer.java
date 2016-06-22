/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.deserializers;

import java.io.IOException;

import android.graphics.Color;
import android.util.JsonReader;

import com.facebook.keyframes.model.KFShape;

/**
 * Deserializer for {@link KFShape}.
 *
 * Root deserializer starts at {@link KFImageDeserializer}.
 */
public class KFShapeDeserializer {

  static final AbstractListDeserializer<KFShape> LIST_DESERIALIZER =
      new AbstractListDeserializer<KFShape>() {
        @Override
        KFShape readObjectImpl(JsonReader reader) throws IOException {
          return readObject(reader);
        }
      };

  public static KFShape readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    KFShape.Builder builder = new KFShape.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case KFShape.NAME_JSON_FIELD:
          builder.name = reader.nextString();
          break;
        case KFShape.FILL_COLOR_JSON_FIELD:
          builder.fillColor = Color.parseColor(reader.nextString());
          break;
        case KFShape.STROKE_COLOR_JSON_FIELD:
          builder.strokeColor = Color.parseColor(reader.nextString());
          break;
        case KFShape.STROKE_WIDTH_JSON_FIELD:
          builder.strokeWidth= (float) reader.nextDouble();
          break;
        case KFShape.KEY_FRAMES_JSON_FIELD:
          builder.keyFrames = KFShapeFrameDeserializer.LIST_DESERIALIZER.readList(reader);
          break;
        case KFShape.TIMING_CURVES_JSON_FIELD:
          builder.timingCurves = CommonDeserializerHelper.read3DFloatArray(reader);
          break;
        case KFShape.ANIMATION_GROUP_JSON_FIELD:
          builder.animationGroup = reader.nextInt();
          break;
        case KFShape.FEATURE_ANIMATIONS_JSON_FIELD:
          builder.featureAnimations =
              KFAnimationDeserializer.LIST_DESERIALIZER.readList(reader);
          break;
        case KFShape.EFFECT_JSON_FIELD:
          builder.effect = KFShapeEffectDeserializer.readObject(reader);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
