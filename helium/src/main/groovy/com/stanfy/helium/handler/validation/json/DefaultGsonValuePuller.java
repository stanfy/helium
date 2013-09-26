package com.stanfy.helium.handler.validation.json;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.stanfy.helium.handler.validation.json.JsonValuePuller;

import java.io.IOException;

/**
 * Gson wrapper that implements JsonValuePuller.
 */
public class DefaultGsonValuePuller implements JsonValuePuller {

  /** Gson reader. */
  private final JsonReader reader;

  public DefaultGsonValuePuller(final JsonReader reader) {
    this.reader = reader;
  }

  @Override
  public float expectFloat() throws IOException {
    double doubleValue = expectDouble();
    float floatValue = (float)doubleValue;
    if (floatValue != doubleValue) {
      throw new IllegalArgumentException("value " + doubleValue + " is too big for float");
    }
    return floatValue;
  }

  @Override
  public double expectDouble() throws IOException {
    return reader.nextDouble();
  }

  @Override
  public int expectInt() throws IOException {
    return reader.nextInt();
  }

  @Override
  public long expectLong() throws IOException {
    return reader.nextLong();
  }

  @Override
  public String expectString() throws IOException {
    if (reader.peek() != JsonToken.STRING) {
      throw new IllegalArgumentException("not a string");
    }
    return reader.nextString();
  }

  @Override
  public boolean expectBoolean() throws IOException {
    return reader.nextBoolean();
  }

  public void skipValue() throws IOException {
    reader.skipValue();
  }

  @Override
  public byte[] expectBytes() throws IOException {
    String str = expectString();
    return str.getBytes("UTF-8");
  }

}
