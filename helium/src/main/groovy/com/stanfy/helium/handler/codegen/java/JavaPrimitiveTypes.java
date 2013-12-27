package com.stanfy.helium.handler.codegen.java;

import com.stanfy.helium.DefaultType;
import com.stanfy.helium.model.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * Primitive types mapping for Java.
 */
public class JavaPrimitiveTypes {

  /** Predefined names mapping. */
  private static final Map<String, Class<?>> NAMES_MAP = new HashMap<String, Class<?>>();
  static {
    NAMES_MAP.put(DefaultType.STRING.getLangName(), String.class);
    NAMES_MAP.put(DefaultType.DOUBLE.getLangName(), double.class);
    NAMES_MAP.put(DefaultType.FLOAT.getLangName(), float.class);
    NAMES_MAP.put(DefaultType.INT32.getLangName(), int.class);
    NAMES_MAP.put(DefaultType.INT64.getLangName(), long.class);
    NAMES_MAP.put(DefaultType.BYTES.getLangName(), byte[].class);
    NAMES_MAP.put(DefaultType.BOOL.getLangName(), boolean.class);
  }

  public static Class<?> javaClass(final Type type) {
    if (!type.isPrimitive()) {
      throw new IllegalArgumentException(type + " is not a primitive type");
    }
    return NAMES_MAP.get(type.getCanonicalName());
  }

  public static Class<?> box(final Class<?> primitive) {
    if (!primitive.isPrimitive()) {
      return primitive;
    }

    if (primitive == int.class) {
      return Integer.class;
    }
    if (primitive == long.class) {
      return Long.class;
    }
    if (primitive == float.class) {
      return Float.class;
    }
    if (primitive == double.class) {
      return Double.class;
    }
    if (primitive == boolean.class) {
      return Boolean.class;
    }
    throw new IllegalArgumentException("Cannot handle " + primitive);
  }

  private JavaPrimitiveTypes() {
    throw new UnsupportedOperationException("no instances");
  }

}
