package com.stanfy.helium

import com.stanfy.helium.handler.ScriptExtender
import com.stanfy.helium.model.Project
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

/**
 * Default types loader.
 */
@CompileStatic
@PackageScope
class DefaultTypesLoader {

  static Reader openScript() {
    return new InputStreamReader(DefaultTypesLoader.classLoader.getResourceAsStream("com/stanfy/helium/dsl/def-types.spec"), "UTF-8")
  }

  static void loadFor(Project project) {
    new ScriptExtender(openScript(), "DefTypes.spec", "/helium/default").handle(project)
  }

}
