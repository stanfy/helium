package com.stanfy.helium.swagger;

import com.stanfy.helium.internal.utils.SelectionRules;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.ServiceMethod;
import com.stanfy.helium.model.Type;

/** Options for SwaggerHandler. */
public class SwaggerOptions {

  /** Endpoint rules. */
  private final SelectionRules endpoints = new SelectionRules("endpoints");

  /** Type rules. */
  private final SelectionRules types = new SelectionRules("types");

  public SelectionRules getEndpoints() {
    return endpoints;
  }

  public SelectionRules getTypes() {
    return types;
  }

  boolean checkIncludes(ServiceMethod m) {
    String name = m.getType().name() + " " + m.getPath();
    return endpoints.check(name);
  }

  boolean checkIncludes(Type type) {
    return types.check(type.getName());
  }

  boolean checkIncludes(Message message, Field field) {
    SelectionRules nested = types.nested(message.getName());
    return nested == null || nested.check(field.getName());
  }

}
