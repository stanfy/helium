package com.stanfy.helium.gradle.internal

import com.stanfy.helium.gradle.tasks.GenerateJavaConstantsTask
import com.stanfy.helium.gradle.tasks.GenerateJavaEntitiesTask
import com.stanfy.helium.gradle.tasks.GenerateJsonSchemaTask
import com.stanfy.helium.gradle.tasks.GenerateObjcEntitiesTask
import com.stanfy.helium.gradle.tasks.GenerateRetrofitTask
import com.stanfy.helium.handler.codegen.java.constants.ConstantsGeneratorOptions
import com.stanfy.helium.handler.codegen.java.entity.EntitiesGeneratorOptions
import com.stanfy.helium.handler.codegen.java.retrofit.RetrofitGeneratorOptions
import com.stanfy.helium.handler.codegen.json.schema.JsonSchemaGeneratorOptions
import com.stanfy.helium.handler.codegen.objectivec.ObjcEntitiesOptions

/**
 * Description of source generators.
 */
class SourceCodeGenerators {

  public static final String DEFAULT_PACKAGE = "api"

  public static final Map<String, ? extends Map<String, Object>> GENERATORS = [
      entities: [
          optionsFactory: { EntitiesGeneratorOptions.defaultOptions(DEFAULT_PACKAGE) },
          task: GenerateJavaEntitiesTask
      ],
      constants: [
          optionsFactory: { ConstantsGeneratorOptions.defaultOptions(DEFAULT_PACKAGE) },
          task: GenerateJavaConstantsTask
      ],
      retrofit: [
          optionsFactory: { RetrofitGeneratorOptions.defaultOptions(DEFAULT_PACKAGE) },
          task: GenerateRetrofitTask
      ],
      objc: [
          optionsFactory: { new ObjcEntitiesOptions() },
          task: GenerateObjcEntitiesTask
      ],
      jsonSchema: [
          optionsFactory: { JsonSchemaGeneratorOptions.defaultOptions() },
          task: GenerateJsonSchemaTask
      ]
  ]

  public static Collection<String> java() {
    // TODO make something smart
    return ["entities", "constants", "retrofit"]
  }

  private SourceCodeGenerators() { }

}
