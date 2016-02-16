package com.stanfy.helium.internal.entities.json;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.stanfy.helium.internal.entities.Converter;
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
    return JsonConvertersFactory.JSON;
  }

  @Override
  public Type getType() {
    return type;
  }

}
