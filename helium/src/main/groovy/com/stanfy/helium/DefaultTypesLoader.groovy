package com.stanfy.helium

import com.stanfy.helium.handler.ClosureExtender
import com.stanfy.helium.model.Project
import groovy.transform.PackageScope

/**
 * Default types loader.
 */
@PackageScope
class DefaultTypesLoader {

  private static def spec = {
    DefaultType.values().each {
      type it.toString().toLowerCase(Locale.US)
    }
  }

  static void loadFor(Project project) {
    new ClosureExtender(spec).handle(project)
  }

}
