package com.stanfy.helium.gradle.internal

import com.stanfy.helium.gradle.tasks.GenerateJavaConstantsTask
import com.stanfy.helium.gradle.tasks.GenerateJavaEntitiesTask
import com.stanfy.helium.gradle.tasks.GenerateRetrofitTask
import com.stanfy.helium.handler.codegen.java.constants.ConstantsGeneratorOptions
import com.stanfy.helium.handler.codegen.java.entity.EntitiesGeneratorOptions
import com.stanfy.helium.handler.codegen.java.retrofit.RetrofitGeneratorOptions
import groovy.transform.CompileStatic

/**
 * Description of source generators.
 */
@CompileStatic
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
      ]
  ]

  private SourceCodeGenerators() { }

}
