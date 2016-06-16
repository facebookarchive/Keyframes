// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.keyframes.deserializers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.util.JsonReader;

public class CommonDeserializerHelper {

  public static final AbstractListDeserializer<String> STRING_LIST_DESERIALIZER =
      new AbstractListDeserializer<String>() {
        @Override
        String readObjectImpl(JsonReader reader) throws IOException {
          return reader.nextString();
        }
      };

  private static final AbstractListDeserializer<Float> FLOAT_LIST_DESERIALIZER =
      new AbstractListDeserializer<Float>() {
        @Override
        Float readObjectImpl(JsonReader reader) throws IOException {
          return (float) reader.nextDouble();
        }
      };

  private static final AbstractListDeserializer<List<Float>> FLOAT_LIST_2D_DESERIALIZER =
      new AbstractListDeserializer<List<Float>>() {
        @Override
        List<Float> readObjectImpl(JsonReader reader) throws IOException {
          return FLOAT_LIST_DESERIALIZER.readList(reader);
        }
      };

  private static final AbstractListDeserializer<List<List<Float>>> FLOAT_LIST_3D_DESERIALIZER =
      new AbstractListDeserializer<List<List<Float>>>() {
        @Override
        List<List<Float>> readObjectImpl(JsonReader reader) throws IOException {
          return FLOAT_LIST_2D_DESERIALIZER.readList(reader);
        }
      };

  public static float[] readFloatArray(JsonReader reader) throws IOException {
    return convertListToPrimitiveArray(FLOAT_LIST_DESERIALIZER.readList(reader));
  }

  public static float[][][] read3DFloatArray(JsonReader reader) throws IOException {
    return convert3DListToPrimitiveArray(FLOAT_LIST_3D_DESERIALIZER.readList(reader));
  }

  private static float[] convertListToPrimitiveArray(List<Float> list) {
    float[] result = new float[list.size()];
    for (int i = 0, len = list.size(); i < len; i++) {
      result[i] = list.get(i);
    }
    return result;
  }

  private static float[][][] convert3DListToPrimitiveArray(List<List<List<Float>>> list) {
    float[][][] primaryArray = new float[list.size()][][];
    for (int i = 0, primaryLen = list.size(); i < primaryLen; i++) {
      float[][] secondaryArray = new float[list.get(0).size()][];
      for (int j = 0, secondaryLen = secondaryArray.length; j < secondaryLen; j++) {
        secondaryArray[j] = convertListToPrimitiveArray(list.get(i).get(j));
      }
      primaryArray[i] = secondaryArray;
    }
    return primaryArray;
  }
}
