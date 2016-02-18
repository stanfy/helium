package com.stanfy.helium.internal.entities

import com.squareup.okhttp.MediaType
import okio.Buffer
import spock.lang.Specification

class EntitiesSinkBuilderSpec extends Specification {

  def "build sink"() {
    when:
    def result = new EntitiesSink.Builder()
        .into(new Buffer())
        .mediaType(MediaType.parse("application/json"))
        .build()
    then:
    result != null
  }

}
