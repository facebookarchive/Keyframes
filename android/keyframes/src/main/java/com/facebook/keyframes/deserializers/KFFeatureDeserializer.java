/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.keyframes.deserializers;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.JsonReader;

import java.io.IOException;
import java.util.Locale;

import com.facebook.keyframes.model.KFFeature;

/**
 * Deserializer for {@link KFFeature}.
 *
 * Root deserializer starts at {@link KFImageDeserializer}.
 */
public class KFFeatureDeserializer {

  static final AbstractListDeserializer<KFFeature> LIST_DESERIALIZER =
      new AbstractListDeserializer<KFFeature>() {
        @Override
        KFFeature readObjectImpl(JsonReader reader) throws IOException {
          return readObject(reader);
        }
      };

  public static KFFeature readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    KFFeature.Builder builder = new KFFeature.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case KFFeature.NAME_JSON_FIELD:
          builder.name = reader.nextString();
          break;
        case KFFeature.FILL_COLOR_JSON_FIELD:
          builder.fillColor = Color.parseColor(reader.nextString());
          break;
        case KFFeature.STROKE_COLOR_JSON_FIELD:
          builder.strokeColor = Color.parseColor(reader.nextString());
          break;
        case KFFeature.STROKE_WIDTH_JSON_FIELD:
          builder.strokeWidth = (float) reader.nextDouble();
          break;
        case KFFeature.FROM_FRAME_JSON_FIELD:
          builder.fromFrame = (float) reader.nextDouble();
          break;
        case KFFeature.TO_FRAME_JSON_FIELD:
          builder.toFrame = (float) reader.nextDouble();
          break;
        case KFFeature.KEY_FRAMES_JSON_FIELD:
          builder.keyFrames = KFFeatureFrameDeserializer.LIST_DESERIALIZER.readList(reader);
          break;
        case KFFeature.TIMING_CURVES_JSON_FIELD:
          builder.timingCurves = CommonDeserializerHelper.read3DFloatArray(reader);
          break;
        case KFFeature.ANIMATION_GROUP_JSON_FIELD:
          builder.animationGroup = reader.nextInt();
          break;
        case KFFeature.FEATURE_ANIMATIONS_JSON_FIELD:
          builder.featureAnimations =
              KFAnimationDeserializer.LIST_DESERIALIZER.readList(reader);
          break;
        case KFFeature.EFFECT_JSON_FIELD:
          builder.effect = KFFeatureEffectDeserializer.readObject(reader);
          break;
        case KFFeature.STROKE_LINE_CAP_JSON_FIELD:
          builder.strokeLineCap = Paint.Cap.valueOf(reader.nextString().toUpperCase(Locale.US));
          break;
        case KFFeature.BACKED_IMAGE_NAME_JSON_FIELD:
          builder.backedImageName = reader.nextString();
          break;
        case KFFeature.FEATURE_MASK_JSON_FIELD:
          builder.featureMask = KFFeatureDeserializer.readObject(reader);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
