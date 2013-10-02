package com.stanfy.helium.dsl

import com.stanfy.helium.utils.ConfigurableStringMap
import com.stanfy.helium.model.tests.MethodTestInfo

import static com.stanfy.helium.utils.DslUtils.runWithProxy;

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
    runWithProxy(new ConfigurableStringMap(info.pathExample, "Path parameters example"), body)
  }


}
