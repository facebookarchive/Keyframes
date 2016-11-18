/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.deserializers;

import java.io.IOException;

import android.util.JsonReader;

import com.facebook.keyframes.model.KFAnimationGroup;

/**
 * Deserializer for {@link KFAnimationGroup}.
 *
 * Root deserializer starts at {@link KFImageDeserializer}.
 */
public class KFAnimationGroupDeserializer {

  static final AbstractListDeserializer<KFAnimationGroup> LIST_DESERIALIZER =
      new AbstractListDeserializer<KFAnimationGroup>() {
        @Override
        KFAnimationGroup readObjectImpl(JsonReader reader) throws IOException {
          return readObject(reader);
        }
      };

  public static KFAnimationGroup readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    KFAnimationGroup.Builder builder = new KFAnimationGroup.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case KFAnimationGroup.GROUP_ID_JSON_FIELD:
          builder.groupId = reader.nextInt();
          break;
        case KFAnimationGroup.PARENT_GROUP_JSON_FIELD:
          builder.parentGroup = reader.nextInt();
          break;
        case KFAnimationGroup.ANIMATIONS_JSON_FIELD:
          builder.animations = KFAnimationDeserializer.LIST_DESERIALIZER.readList(reader);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
