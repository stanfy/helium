package com.stanfy.helium.utils

import com.stanfy.helium.dsl.Project
import com.stanfy.helium.handler.ScriptExtender
import groovy.transform.CompileStatic

/**
 * Default types loader.
 */
@CompileStatic
class DefaultTypesLoader {

  static Reader openScript() {
    return new InputStreamReader(DefaultTypesLoader.classLoader.getResourceAsStream("com/stanfy/helium/dsl/def-types.spec"), "UTF-8")
  }

  static void loadFor(Project project) {
    new ScriptExtender(openScript(), "DefTypes.spec", "/helium/default").handle(project)
  }

}
