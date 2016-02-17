package com.stanfy.helium.internal.dsl

import com.squareup.okhttp.MediaType
import com.stanfy.helium.format.PrimitiveReader
import com.stanfy.helium.format.PrimitiveWriter
import com.stanfy.helium.model.Type
import spock.lang.Specification

class DefaultTypeResolverSpec extends Specification {

  private DefaultTypeResolver resolver

  void setup() {
    resolver = new DefaultTypeResolver()
  }

  def "cannot register a duplicate"() {
    when:
    resolver.registerNewType(new Type(name: 'test name'))
    resolver.registerNewType(new Type(name: 'test name'))

    then:
    def e = thrown(IllegalArgumentException)
    e.message.contains('test name')
  }

  def "cat get a type by name"() {
    given:
    def type = new Type(name: 'abc')
    resolver.registerNewType(type)

    expect:
    resolver.byName('abc').is type
  }

  def "adding custom reader"() {
    given:
    def reader = Mock(PrimitiveReader)
    def typeA = new Type(name: 'a'), typeB = new Type(name: 'b')

    when:
    resolver.addTypeReader('json', typeA, reader)
    resolver.addTypeReader('json', typeB, reader)
    resolver.addTypeReader('xml', typeB, reader)
    def readers = resolver.customReaders(MediaType.parse('*/json'))

    then:
    readers.containsKey(typeA)
    readers[typeA].is reader
    readers.containsKey(typeB)
    readers[typeB].is reader
    readers.size() == 2
  }

  def "adding custom writer"() {
    given:
    def writer = Mock(PrimitiveWriter)
    def typeA = new Type(name: 'a'), typeB = new Type(name: 'b')

    when:
    resolver.addTypeWriter('xml', typeA, writer)
    resolver.addTypeWriter('json', typeB, writer)
    resolver.addTypeWriter('xml', typeB, writer)
    def writers = resolver.customWriters(MediaType.parse('text/xml'))

    then:
    writers.containsKey(typeA)
    writers[typeA].is writer
    writers.containsKey(typeB)
    writers[typeB].is writer
    writers.size() == 2
  }

}
