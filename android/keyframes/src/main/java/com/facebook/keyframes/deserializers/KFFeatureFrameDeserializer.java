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

import com.facebook.keyframes.model.KFFeatureFrame;

/**
 * Deserializer for {@link KFFeatureFrame}.
 *
 * Root deserializer starts at {@link KFImageDeserializer}.
 */
public class KFFeatureFrameDeserializer {

  static final AbstractListDeserializer<KFFeatureFrame> LIST_DESERIALIZER =
      new AbstractListDeserializer<KFFeatureFrame>() {
        @Override
        KFFeatureFrame readObjectImpl(JsonReader reader) throws IOException {
          return readObject(reader);
        }
      };

  public static KFFeatureFrame readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    KFFeatureFrame.Builder builder = new KFFeatureFrame.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case KFFeatureFrame.START_FRAME_JSON_FIELD:
          builder.startFrame = reader.nextInt();
          break;
        case KFFeatureFrame.DATA_JSON_FIELD:
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
