package com.stanfy.helium.dsl

import com.stanfy.helium.model.tests.MethodTestInfo

/**
 * Extension of test info for service method. Provides pathExample syntax.
 */
class ConfigurableMethodTestsInfo extends ConfigurableTestsInfo<MethodTestInfo> {

  ConfigurableMethodTestsInfo(final MethodTestInfo core, final ProjectDsl project) {
    super(core, project)
  }

  void pathExample(final Closure<?> body) {
    MethodTestInfo info = getCore()
    if (info.pathExample == null) {
      info.pathExample = new LinkedHashMap<>()
    }
    ProjectDsl.callConfigurationSpec(new ConfigurableStringMap(info.pathExample, "Path parameters example"), body)
  }


}
