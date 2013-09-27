package com.stanfy.helium.dsl

import com.stanfy.helium.model.tests.MethodTestInfo
import groovy.transform.CompileStatic

/**
 * Extension of test info for service method. Provides pathExample syntax.
 */
@CompileStatic
class ConfigurableMethodTestsInfo extends ConfigurableTestsInfo {

  ConfigurableMethodTestsInfo(final MethodTestInfo core, final ProjectDsl project) {
    super(core, project)
  }

  void pathExample(final Closure<?> body) {
    MethodTestInfo info = (MethodTestInfo) getCore()
    if (info.pathExample == null) {
      info.pathExample = new LinkedHashMap<>()
    }
    ProjectDsl.callConfigurationSpec(new ConfigurableStringMap(info.pathExample, "Path parameters example"), body)
  }


}
