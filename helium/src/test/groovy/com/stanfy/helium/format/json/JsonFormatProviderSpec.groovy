package com.stanfy.helium.format.json

import com.squareup.okhttp.MediaType
import spock.lang.Specification

class JsonFormatProviderSpec extends Specification {

  def "supported media types"() {
    expect:
    supported == JsonFormatProvider.supportsMediaType(MediaType.parse(type))

    where:
    supported | type
    true      | 'application/json'
    true      | 'application/vnd.api+json'
    true      | 'application/vnd.collection+json'
    false     | 'json/invalid'
  }

}
