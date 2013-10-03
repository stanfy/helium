package com.stanfy.helium.entities.json;

import com.stanfy.helium.DefaultType;
import com.stanfy.helium.entities.json.ValuePuller;
import com.stanfy.helium.model.Type;

import java.io.IOException;
import java.util.Locale;

/**
 * Implements GsonValidator for DefaultTy
 */
class DefaultJsonTypeValidator implements JsonTypedValuePuller {

  @Override
  public Object validateNextValue(final ValuePuller puller, final Type type, final boolean required) throws IOException {

    if (required && puller.checkNull()) {
      puller.skipValue();
      throw new IllegalStateException("value of type " + type.getName() + " is required but got NULL");
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
          return puller.pullFloat();
        case DOUBLE:
          return puller.pullDouble();
        case INT64:
          return puller.pullLong();
        case INT32:
          return puller.pullInt();
        case BOOL:
          return puller.pullBoolean();
        case STRING:
          return puller.pullString();
        case BYTES:
          return puller.pullBytes();
        default:
          throw new UnsupportedOperationException("Unknown type " + defType);
      }
    } catch (IllegalStateException e) {
      puller.skipValue();
      throw e;
    } catch (IllegalArgumentException e) {
      puller.skipValue();
      throw new IllegalStateException("bad format: " + e.getMessage());
    }
  }

}
