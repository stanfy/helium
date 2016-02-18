package com.stanfy.helium.gradle.tasks

import com.stanfy.helium.swagger.SwaggerHandler

/** Task for generating Swagger spec. */
class SwaggerTask extends BaseHeliumTask<Object> {

  @Override
  protected void doIt() {
    helium.processBy(new SwaggerHandler(output))
  }

}
