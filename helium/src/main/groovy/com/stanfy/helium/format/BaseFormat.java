package com.stanfy.helium.format;

import com.stanfy.helium.model.Type;

import java.util.HashMap;
import java.util.Map;

abstract class BaseFormat<Core, Primitive> {

  final Core core;
  @SuppressWarnings("unchecked")
  final Map<String, Primitive> primitives = new HashMap(5);

  private final String verb;

  protected BaseFormat(Core core, String verb) {
    this.core = core;
    this.verb = verb;
  }

  public void registerPrimitiveAdapter(final Type type, final Primitive primitive) {
    primitives.put(type.getCanonicalName(), primitive);
  }

  Primitive findAdapter(final Type type) {
    Primitive result = primitives.get(type.getCanonicalName());
    if (result == null) {
      throw new UnsupportedOperationException("Cannot find out how to " + verb + " " + type);
    }
    return result;
  }

}
