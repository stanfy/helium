package com.stanfy.helium.internal.dsl

import com.stanfy.helium.utils.ConfigurableProxy
import com.stanfy.helium.utils.ConfigurableStringMap
import com.stanfy.helium.model.tests.TestsInfo

import static com.stanfy.helium.utils.DslUtils.runWithProxy

/**
 * Proxy for TestsInfo.
 */
abstract class ConfigurableTestsInfo<T extends TestsInfo> extends ConfigurableProxy<T> {

  ConfigurableTestsInfo(TestsInfo core, ProjectDsl project) {
    super(core, project)
  }

  void httpHeaders(final Closure<?> body) {
    TestsInfo info = getCore()
    runWithProxy(new ConfigurableStringMap(info.httpHeaders, "HTTP httpHeaders"), body)
  }

}
