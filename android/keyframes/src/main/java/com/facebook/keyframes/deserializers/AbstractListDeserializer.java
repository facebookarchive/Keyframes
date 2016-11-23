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
import java.util.ArrayList;
import java.util.List;

import android.util.JsonReader;

/**
 * An generic abstract class to aid with deserializing a list of objects.
 * @param <T>
 */
public abstract class AbstractListDeserializer<T> {

  /**
   * Given a {@link JsonReader} object at the start of a list, read through the list and return
   * a List with the given objects of type {@link T}.
   * @param reader The current {@link JsonReader} at the start of a list of {@link T}
   * @return List of type {@link T} objects
   * @throws IOException
   */
  public final List<T> readList(JsonReader reader) throws IOException {
    List<T> list = new ArrayList<>();
    reader.beginArray();
    while (reader.hasNext()) {
      list.add(this.readObjectImpl(reader));
    }
    reader.endArray();
    return list;
  }

  /**
   * Read one object of type {@link T} from the JsonReader and return it.
   * @param reader The current {@link JsonReader}
   * @return An object of type {@link T}
   * @throws IOException
   */
  abstract T readObjectImpl(JsonReader reader) throws IOException;
}
