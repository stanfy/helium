package com.stanfy.helium.dsl

import com.stanfy.helium.model.tests.TestsInfo
import groovy.transform.CompileStatic

/**
 * Proxy for TestsInfo.
 */
@CompileStatic
class ConfigurableTestsInfo extends ConfigurableProxy<TestsInfo> {

  ConfigurableTestsInfo(TestsInfo core, ProjectDsl project) {
    super(core, project)
  }

  void httpHeaders(final Closure<?> body) {
    ProjectDsl.callConfigurationSpec(new ConfigurableStringMap(getCore().httpHeaders, "HTTP headers"), body)
  }

}
