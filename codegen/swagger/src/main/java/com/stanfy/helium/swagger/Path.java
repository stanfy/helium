package com.stanfy.helium.swagger;

import com.stanfy.helium.model.ServiceMethod;

import java.util.HashMap;
import java.util.List;

/** Path definition in swagger. */
final class Path extends HashMap<String, Path.Method> {

  Method swaggerMethod(ServiceMethod m) {
    Method method = get(m.getType().getName());
    if (method == null) {
      method = new Method();
      put(m.getType().getName(), method);
    }
    return method;
  }

  static class Method {
    String summary, description;
    List<Parameter> parameters;
  }
}
