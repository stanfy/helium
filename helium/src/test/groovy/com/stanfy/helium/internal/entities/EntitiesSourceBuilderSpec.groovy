package com.stanfy.helium.internal.entities

import com.squareup.okhttp.MediaType
import okio.Buffer
import spock.lang.Specification

class EntitiesSourceBuilderSpec extends Specification {

  def "build source"() {
    when:
    def result = new EntitiesSource.Builder()
        .from(new Buffer())
        .mediaType(MediaType.parse("application/json"))
        .build()
    then:
    result != null
  }

}
