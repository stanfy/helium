package com.stanfy.helium.dsl

import com.stanfy.helium.model.tests.TestsInfo

/**
 * Proxy for TestsInfo.
 */
abstract class ConfigurableTestsInfo<T extends TestsInfo> extends ConfigurableProxy<T> {

  ConfigurableTestsInfo(TestsInfo core, ProjectDsl project) {
    super(core, project)
  }

  void httpHeaders(final Closure<?> body) {
    TestsInfo info = getCore()
    ProjectDsl.callConfigurationSpec(new ConfigurableStringMap(info.httpHeaders, "HTTP headers"), body)
  }

}
