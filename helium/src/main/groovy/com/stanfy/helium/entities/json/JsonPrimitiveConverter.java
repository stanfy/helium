package com.stanfy.helium.entities.json;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.stanfy.helium.entities.Converter;
import com.stanfy.helium.model.Type;

/**
 * Base class for JSON primitives converters.
 */
abstract class JsonPrimitiveConverter implements Converter<Type, JsonReader, JsonWriter> {

  /** Type. */
  private final Type type;

  public JsonPrimitiveConverter(final Type type) {
    this.type = type;
  }

  @Override
  public String getFormat() {
    return JsonConvertersPool.JSON;
  }

  @Override
  public Type getType() {
    return type;
  }

}
