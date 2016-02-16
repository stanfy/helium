package com.stanfy.helium.internal.entities

import com.squareup.okhttp.MediaType
import com.stanfy.helium.internal.dsl.ProjectDsl
import okio.Buffer
import spock.lang.Specification

class FormatSinkBuilderSpec extends Specification {

  def "build source"() {
    when:
    def project = new ProjectDsl()
    def result = new FormatSink.Builder()
        .into(new Buffer())
        .mediaType(MediaType.parse("application/json"))
        .types(project.types)
        .build()
    then:
    result != null
  }

}
