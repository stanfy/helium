package com.stanfy.helium.model

import spock.lang.Specification

/**
 * Spec for HttpHeader.
 */
class HttpHeaderSpec extends Specification {

  private HttpHeader header

  def setup() {
    header = new HttpHeader(name: 'name')
  }

  def "headers with value are constants"() {
    when:
    header.value = 'a'
    then:
    header.constant
  }

  def "headers without value are not constants"() {
    when:
    header.value = ''
    then:
    !header.constant
  }

}
