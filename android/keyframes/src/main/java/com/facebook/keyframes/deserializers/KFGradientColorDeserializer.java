/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.keyframes.deserializers;

import java.io.IOException;

import android.util.JsonReader;

import com.facebook.keyframes.model.KFGradientColor;

/**
 * Deserializer for {@link KFGradientColor}.
 *
 * Root deserializer starts at {@link KFImageDeserializer}.
 */
public class KFGradientColorDeserializer {

  public static KFGradientColor readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    KFGradientColor.Builder builder = new KFGradientColor.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case KFGradientColor.KEY_VALUES_JSON_FIELD:
          builder.keyValues = KFColorFrameDeserializer.LIST_DESERIALIZER.readList(reader);
          break;
        case KFGradientColor.TIMING_CURVES_JSON_FIELD:
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
