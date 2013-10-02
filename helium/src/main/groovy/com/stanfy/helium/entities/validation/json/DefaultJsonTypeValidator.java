package com.stanfy.helium.entities.validation.json;

import com.stanfy.helium.DefaultType;
import com.stanfy.helium.entities.ValuePuller;
import com.stanfy.helium.model.Type;

import java.io.IOException;
import java.util.Locale;

/**
 * Implements GsonValidator for DefaultTy
 */
public class DefaultJsonTypeValidator implements JsonTypeValidator {

  @Override
  public String validateNextValue(final ValuePuller puller, final Type type, final boolean required) throws IOException {

    if (required && puller.checkNull()) {
      puller.skipValue();
      return "value of type " + type.getName() + " is required but got NULL";
    }

    final DefaultType defType;
    try {
      defType = DefaultType.valueOf(type.getName().toUpperCase(Locale.US));
    } catch (IllegalArgumentException e) {
      throw new UnsupportedOperationException("Unknown type: " + type.getName());
    }

    try {
      switch (defType) {
        case FLOAT:
          puller.pullFloat();
          break;
        case DOUBLE:
          puller.pullDouble();
          break;
        case INT64:
          puller.pullLong();
          break;
        case INT32:
          puller.pullInt();
          break;
        case BOOL:
          puller.pullBoolean();
          break;
        case STRING:
          puller.pullString();
          break;
        case BYTES:
          puller.pullBytes();
          break;
        default:
          throw new UnsupportedOperationException("Unknown type " + defType);
      }
    } catch (IllegalStateException e) {
      puller.skipValue();
      return "bad format: " + e.getMessage();
    } catch (IllegalArgumentException e) {
      puller.skipValue();
      return "bad format: " + e.getMessage();
    }

    return null;
  }

}
