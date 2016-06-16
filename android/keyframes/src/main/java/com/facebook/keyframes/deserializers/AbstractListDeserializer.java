// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.deserializers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.JsonReader;

public abstract class AbstractListDeserializer<T> {
  public List<T> readList(JsonReader reader) throws IOException {
    List<T> list = new ArrayList<>();
    reader.beginArray();
    while (reader.hasNext()) {
      list.add(this.readObjectImpl(reader));
    }
    reader.endArray();
    return list;
  }

  abstract T readObjectImpl(JsonReader reader) throws IOException;
}
