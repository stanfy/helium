package com.stanfy.helium.dsl

import com.stanfy.helium.model.tests.TestsInfo

/**
 * Proxy for TestsInfo.
 */
class ConfigurableTestsInfo extends ConfigurableProxy<TestsInfo> {

  ConfigurableTestsInfo(TestsInfo core, ProjectDsl project) {
    super(core, project)
  }

  void httpHeaders(final Closure<?> body) {
    TestsInfo info = getCore()
    ProjectDsl.callConfigurationSpec(new ConfigurableStringMap(info.httpHeaders, "HTTP headers"), body)
  }

}
