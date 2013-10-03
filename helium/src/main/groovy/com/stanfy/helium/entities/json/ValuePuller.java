package com.stanfy.helium.entities.json;

import java.io.IOException;

/**
 * Someone who can pull next value from somewhere.
 * It may throw IllegalArgumentException/IllegalStateException in case of errors.
 * In case of an error value is not consumed.
 * <p>
 *   Methods correspond to {@link com.stanfy.helium.DefaultType} enum.
 * </p>
 */
interface ValuePuller {

  // TODO: refactor pullers, writers, readers

  float pullFloat() throws IOException;

  double pullDouble() throws IOException;

  int pullInt() throws IOException;

  long pullLong() throws IOException;

  String pullString() throws IOException;

  boolean pullBoolean() throws IOException;

  byte[] pullBytes() throws IOException;

  boolean checkNull() throws IOException;

  void skipValue() throws IOException;

}
