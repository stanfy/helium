package com.stanfy.helium.entities.validation.json;

import java.io.IOException;

/**
 * Someone who can pull next JSON value. In case of error value is not consumed.
 */
public interface JsonValuePuller {

  float expectFloat() throws IOException;

  double expectDouble() throws IOException;

  int expectInt() throws IOException;

  long expectLong() throws IOException;

  String expectString() throws IOException;

  boolean expectBoolean() throws IOException;

  byte[] expectBytes() throws IOException;

  boolean checkNull() throws IOException;

  void skipValue() throws IOException;

}
