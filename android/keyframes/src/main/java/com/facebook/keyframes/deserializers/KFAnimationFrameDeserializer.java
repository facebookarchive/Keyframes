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

import com.facebook.keyframes.model.KFAnimationFrame;

/**
 * Deserializer for {@link KFAnimationFrame}.
 *
 * Root deserializer starts at {@link KFImageDeserializer}.
 */
public class KFAnimationFrameDeserializer {

  static final AbstractListDeserializer<KFAnimationFrame> LIST_DESERIALIZER =
      new AbstractListDeserializer<KFAnimationFrame>() {
        @Override
        KFAnimationFrame readObjectImpl(JsonReader reader) throws IOException {
          return readObject(reader);
        }
      };

  public static KFAnimationFrame readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    KFAnimationFrame.Builder builder = new KFAnimationFrame.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case KFAnimationFrame.START_FRAME_JSON_FIELD:
          builder.startFrame = reader.nextInt();
          break;
        case KFAnimationFrame.DATA_JSON_FIELD:
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
