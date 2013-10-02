package com.stanfy.helium.entities.validation.json;

import com.stanfy.helium.DefaultType;
import com.stanfy.helium.model.Type;

import java.io.IOException;
import java.util.Locale;

/**
 * Implements GsonValidator for DefaultTy
 */
public class DefaultJsonTypeValidator implements JsonTypeValidator {

  @Override
  public String validateNextValue(final JsonValuePuller json, final Type type) throws IOException {

    final DefaultType defType;
    try {
      defType = DefaultType.valueOf(type.getName().toUpperCase(Locale.US));
    } catch (IllegalArgumentException e) {
      throw new UnsupportedOperationException("Unknown type: " + type.getName());
    }

    try {
      switch (defType) {
        case FLOAT:
          json.expectFloat();
          break;
        case DOUBLE:
          json.expectDouble();
          break;
        case INT64:
          json.expectLong();
          break;
        case INT32:
          json.expectInt();
          break;
        case BOOL:
          json.expectBoolean();
          break;
        case STRING:
          json.expectString();
          break;
        case BYTES:
          json.expectBytes();
          break;
        default:
          throw new UnsupportedOperationException("Unknown type " + defType);
      }
    } catch (IllegalArgumentException e) {
      json.skipValue();
      return "bad format: " + e.getMessage();
    }

    return null;
  }

}
