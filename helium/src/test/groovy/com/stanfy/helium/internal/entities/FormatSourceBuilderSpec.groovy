package com.stanfy.helium.internal.entities

import com.squareup.okhttp.MediaType
import com.stanfy.helium.internal.dsl.ProjectDsl
import okio.Buffer
import spock.lang.Specification

class FormatSourceBuilderSpec extends Specification {

  def "build source"() {
    when:
    def project = new ProjectDsl()
    def result = new FormatSource.Builder()
        .from(new Buffer())
        .mediaType(MediaType.parse("application/json"))
        .types(project.types)
        .build()
    then:
    result != null
  }

}
