package com.stanfy.helium.swagger;

import com.stanfy.helium.handler.codegen.json.schema.JsonSchemaEntity;
import com.stanfy.helium.model.ServiceMethod;

import java.util.ArrayList;
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
    String summary, description, operationId;
    final List<Parameter> parameters = new ArrayList<>();
    Map<String, Response> responses;
  }

  static final class Response {

    final JsonSchemaEntity schema;

    final String description;

    Response(JsonSchemaEntity schema, String description) {
      this.schema = schema;
      this.description = description;
    }

  }

}
