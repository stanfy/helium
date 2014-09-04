package com.stanfy.helium.gradle.tasks

import com.stanfy.helium.handler.codegen.java.retrofit.RetrofitGeneratorOptions
import com.stanfy.helium.handler.codegen.java.retrofit.RetrofitInterfaceGenerator

/**
 * Task for generating Retrofit interfaces from a specification.
 */
class GenerateRetrofitTask extends BaseHeliumTask<RetrofitGeneratorOptions> {

  @Override
  protected void doIt() {
    helium.processBy new RetrofitInterfaceGenerator(output, options)
  }

}
