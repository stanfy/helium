package com.stanfy.helium.swagger;

import com.google.gson.annotations.SerializedName;
import com.stanfy.helium.model.ServiceMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    Map<String, Response> responses;
  }

  static final class Response {

    final Schema schema;

    Response(String ref) {
      this.schema = new Schema(ref);
    }

    static final class Schema {
      @SerializedName("$ref")
      final String ref;

      private Schema(String ref) {
        this.ref = ref;
      }
    }
  }
}
