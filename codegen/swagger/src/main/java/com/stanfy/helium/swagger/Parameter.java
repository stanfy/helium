package com.stanfy.helium.swagger;

import com.stanfy.helium.handler.codegen.json.schema.JsonSchemaEntity;
import com.stanfy.helium.handler.codegen.json.schema.JsonType;

public class Parameter {
  String name, in, description;
  boolean required;

  JsonType type;
  String format;

  JsonSchemaEntity schema;
}
