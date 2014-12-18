package com.stanfy.helium.handler.codegen.tests

import com.stanfy.helium.model.HttpHeader
import com.stanfy.helium.model.ServiceMethod
import spock.lang.Specification

class UtilsSpec extends Specification {

  def "header predefined values are counted when unresolved headers are detected"() {
    given:
    ServiceMethod method = new ServiceMethod()
    method.httpHeaders.add(new HttpHeader(name: "h1", value: "v1"))
    method.httpHeaders.add(new HttpHeader(name: "h2"))
    method.httpHeaders.add(new HttpHeader(name: "h3"))

    when:
    def unresolved = Utils.findUnresolvedHeaders(method, [h2: 'v2'])

    then:
    unresolved.size() == 1
    unresolved[0] == "h3"
  }

}
