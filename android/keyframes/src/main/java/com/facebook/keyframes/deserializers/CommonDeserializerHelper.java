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
import java.util.Arrays;
import java.util.List;

import android.util.JsonReader;

/**
 * Some useful common deserializers that are used by multiple Keyframes model objects.
 */
public class CommonDeserializerHelper {

  /**
   * {@link AbstractListDeserializer} implementation for a list of {@link String}s.
   */
  public static final AbstractListDeserializer<String> STRING_LIST_DESERIALIZER =
      new AbstractListDeserializer<String>() {
        @Override
        String readObjectImpl(JsonReader reader) throws IOException {
          return reader.nextString();
        }
      };

  /**
   * {@link AbstractListDeserializer} implementation for a list of {@link Float}s.
   */
  private static final AbstractListDeserializer<Float> FLOAT_LIST_DESERIALIZER =
      new AbstractListDeserializer<Float>() {
        @Override
        Float readObjectImpl(JsonReader reader) throws IOException {
          return (float) reader.nextDouble();
        }
      };

  /**
   * {@link AbstractListDeserializer} implementation for a List<List<Float>>.
   */
  private static final AbstractListDeserializer<List<Float>> FLOAT_LIST_2D_DESERIALIZER =
      new AbstractListDeserializer<List<Float>>() {
        @Override
        List<Float> readObjectImpl(JsonReader reader) throws IOException {
          return FLOAT_LIST_DESERIALIZER.readList(reader);
        }
      };

  /**
   * {@link AbstractListDeserializer} implementation for a List<List<List<Float>>>.
   */
  private static final AbstractListDeserializer<List<List<Float>>> FLOAT_LIST_3D_DESERIALIZER =
      new AbstractListDeserializer<List<List<Float>>>() {
        @Override
        List<List<Float>> readObjectImpl(JsonReader reader) throws IOException {
          return FLOAT_LIST_2D_DESERIALIZER.readList(reader);
        }
      };

  /**
   * Reads in a float array from {@link JsonReader} and returns a primitive float array
   * @param reader The current {@link JsonReader}
   * @return a float[], containing floats parsed from {@link JsonReader}
   * @throws IOException
   */
  public static float[] readFloatArray(JsonReader reader) throws IOException {
    return convertListToPrimitiveArray(FLOAT_LIST_DESERIALIZER.readList(reader));
  }

  /**
   * Reads in a 3D float array from {@link JsonReader}, and returns a 3D primitive float array
   * @param reader The currnet {@link JsonReader}
   * @return a float[][][], containing the floats parsed from {@link JsonReader}
   * @throws IOException
   */
  public static float[][][] read3DFloatArray(JsonReader reader) throws IOException {
    return convert3DListToPrimitiveArray(FLOAT_LIST_3D_DESERIALIZER.readList(reader));
  }

  /**
   * A helper method which converts a List<Float> to a primitive array float[].
   * @param list The List<Float> to convert
   * @return a float[], representing the {@param list} passed in
   */
  private static float[] convertListToPrimitiveArray(List<Float> list) {
    float[] result = new float[list.size()];
    for (int i = 0, len = list.size(); i < len; i++) {
      result[i] = list.get(i);
    }
    return result;
  }

  /**
   * A helper method which converts List<List<List<Float>>> to a primitive array float[][][].
   * @param list The List<List<List<Float>>> to convert
   * @return a float[][][], representing the {@param list} passed in
   */
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
