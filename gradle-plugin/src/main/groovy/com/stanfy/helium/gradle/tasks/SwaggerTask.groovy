package com.stanfy.helium.gradle.tasks

import com.stanfy.helium.swagger.SwaggerHandler
import com.stanfy.helium.swagger.SwaggerOptions
import org.gradle.api.tasks.Input

/** Task for generating Swagger spec. */
class SwaggerTask extends BaseHeliumTask<SwaggerOptions> {

  private List<String> includes

  @Input
  void includes(String... includes) {
    this.includes = Arrays.asList(includes)
  }

  @Override
  protected void doIt() {
    options = new SwaggerOptions()
    if (this.includes) {
      options.includes(this.includes)
    }
    helium.processBy(new SwaggerHandler(output, options))
  }

}
