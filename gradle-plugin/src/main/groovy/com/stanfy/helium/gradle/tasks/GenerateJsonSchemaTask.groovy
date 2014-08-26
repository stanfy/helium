package com.stanfy.helium.gradle.tasks

import com.stanfy.helium.handler.codegen.json.schema.JsonSchemaGeneratorOptions
import com.stanfy.helium.handler.codegen.json.schema.JsonSchemaGenerator

/**
 * Json scheme generation task.
 */
class GenerateJsonSchemaTask extends BaseHeliumTask {
  /** Generator options. */
  JsonSchemaGeneratorOptions options

  @Override
  protected void doIt() {
    helium.processBy(new JsonSchemaGenerator(output, options))
  }
}
