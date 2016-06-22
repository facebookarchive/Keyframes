/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.deserializers;

import java.io.IOException;

import android.util.JsonReader;

import com.facebook.keyframes.model.KFShapeFrame;

/**
 * Deserializer for {@link KFShapeFrame}.
 *
 * Root deserializer starts at {@link KFImageDeserializer}.
 */
public class KFShapeFrameDeserializer {

  static final AbstractListDeserializer<KFShapeFrame> LIST_DESERIALIZER =
      new AbstractListDeserializer<KFShapeFrame>() {
        @Override
        KFShapeFrame readObjectImpl(JsonReader reader) throws IOException {
          return readObject(reader);
        }
      };

  public static KFShapeFrame readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    KFShapeFrame.Builder builder = new KFShapeFrame.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case KFShapeFrame.START_FRAME_JSON_FIELD:
          builder.startFrame = reader.nextInt();
          break;
        case KFShapeFrame.DATA_JSON_FIELD:
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
