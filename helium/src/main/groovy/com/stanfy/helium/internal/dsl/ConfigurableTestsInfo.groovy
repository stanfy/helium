package com.stanfy.helium.internal.dsl

import com.stanfy.helium.internal.utils.ConfigurableProxy
import com.stanfy.helium.model.tests.Oauth2AuthenticationParams
import com.stanfy.helium.model.tests.TestsInfo

import static com.stanfy.helium.internal.utils.DslUtils.runWithProxy
import static com.stanfy.helium.internal.utils.DslUtils.stringMapProxy

/**
 * Proxy for TestsInfo.
 */
abstract class ConfigurableTestsInfo<T extends TestsInfo> extends ConfigurableProxy<T> {

  ConfigurableTestsInfo(TestsInfo core, ProjectDsl project) {
    super(core, project)
  }

  void httpHeaders(final Closure<?> body) {
    TestsInfo info = getCore()
    runWithProxy(stringMapProxy(info.httpHeaders, "HTTP httpHeaders"), body)
  }

  void oauth2(final Closure<?> body) {
    def params = new Oauth2AuthenticationParams()
    TestsInfo info = getCore()
    info.authParams = params
    def proxy = new ConfigurableProxy<>(params, getProject())
    runWithProxy(proxy, body)
  }

}
