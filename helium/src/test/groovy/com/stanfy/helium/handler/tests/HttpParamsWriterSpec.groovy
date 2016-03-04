package com.stanfy.helium.handler.tests

import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.internal.entities.TypedEntity
import com.stanfy.helium.internal.entities.TypedEntityValueBuilder
import spock.lang.Specification

/** Spec for HttpParamsWriter. */
class HttpParamsWriterSpec extends Specification {

  private HttpParamsWriter writer
  private StringWriter output
  private ProjectDsl dsl

  def setup() {
    output = new StringWriter()
    writer = new HttpParamsWriter(output, 'UTF-8')
    dsl = new ProjectDsl()
    dsl.type 'int32'
    dsl.type 'string'
  }

  def 'encode values'() {
    given:
    dsl.type 'A' message {
      foo 'int32'
      bar 'string'
    }
    def type = dsl.messages.first()

    when:
    def value = new TypedEntityValueBuilder(type).from {
      foo 5
      bar 'some value'
    }
    writer.write(new TypedEntity(type, value))

    then:
    output.toString() == 'foo=5&bar=some+value'
  }

  def 'handle sequences'() {
    given:
    dsl.type 'A' message {
      foo 'int32' sequence
    }
    def type = dsl.messages.first()

    when:
    def value = new TypedEntityValueBuilder(type).from {
      foo([1, 2, 3])
    }
    writer.write(new TypedEntity(type, value))

    then:
    output.toString() == 'foo=1&foo=2&foo=3'
  }

  def 'skip null values'() {
    given:
    dsl.type 'A' message {
      foo 'string' optional
      bar 'string'
    }
    def type = dsl.messages.first()

    when:
    def value = new TypedEntityValueBuilder(type).from {
      bar 'value'
    }
    writer.write(new TypedEntity(type, value))

    then:
    output.toString() == 'bar=value'
  }

}
