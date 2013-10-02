package com.stanfy.helium.entities.json;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.stanfy.helium.entities.ValuePuller;

import java.io.IOException;

/**
 * Gson wrapper that implements ValuePuller.
 */
public class GsonValuePuller implements ValuePuller {

  /** Gson reader. */
  private final JsonReader reader;

  public GsonValuePuller(final JsonReader reader) {
    this.reader = reader;
  }

  @Override
  public float pullFloat() throws IOException {
    double doubleValue = pullDouble();
    float floatValue = (float)doubleValue;
    if (floatValue != doubleValue) {
      throw new IllegalArgumentException("value " + doubleValue + " is too big for float");
    }
    return floatValue;
  }

  @Override
  public double pullDouble() throws IOException {
    return reader.nextDouble();
  }

  @Override
  public int pullInt() throws IOException {
    return reader.nextInt();
  }

  @Override
  public long pullLong() throws IOException {
    return reader.nextLong();
  }

  @Override
  public String pullString() throws IOException {
    JsonToken nextToken = reader.peek();
    if (nextToken == JsonToken.NULL) {
      reader.nextNull();
      return null;
    }
    if (nextToken != JsonToken.STRING) {
      throw new IllegalArgumentException("not a string");
    }
    return reader.nextString();
  }

  @Override
  public boolean pullBoolean() throws IOException {
    return reader.nextBoolean();
  }

  public void skipValue() throws IOException {
    reader.skipValue();
  }

  @Override
  public byte[] pullBytes() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean checkNull() throws IOException {
    return reader.peek() == JsonToken.NULL;
  }

}
