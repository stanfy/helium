package com.stanfy.helium.gradle.tasks

import com.stanfy.helium.gradle.internal.SelectionRulesBuilder
import com.stanfy.helium.internal.utils.DslUtils
import com.stanfy.helium.swagger.SwaggerHandler
import com.stanfy.helium.swagger.SwaggerOptions

/** Task for generating Swagger spec. */
class SwaggerTask extends BaseHeliumTask<SwaggerOptions> {

  {
    options = new SwaggerOptions()
  }

  void endpoints(Closure<?> config) {
    DslUtils.runWithProxy(options.endpoints, config)
  }

  void types(Closure<?> config) {
    DslUtils.runWithProxy(new SelectionRulesBuilder(options.types), config)
  }

  @Override
  protected void doIt() {
    helium.processBy(new SwaggerHandler(output, options))
  }


}
