package com.stanfy.helium.handler.codegen.java

import com.stanfy.helium.model.Type
import spock.lang.Specification

/**
 * Created by roman on 1/18/14.
 */
class JavaGeneratorOptionsSpec extends Specification {

  def Options options

  def setup() {
    options = new Options()
  }

  def "should allow processing if rules are empty"() {
    expect:
    options.isTypeIncluded(new Type(name: 'any name'))
  }

  def "should filter when include rules are defined"() {
    given:
    options.include << 'Include.*'

    expect:
    options.isTypeIncluded(new Type(name: 'IncludeMe'))
    options.isTypeIncluded(new Type(name: 'IncludeHim'))
    !options.isTypeIncluded(new Type(name: 'no1'))
    !options.isTypeIncluded(new Type(name: 'no2'))
  }

  def "should filter when exclude rules are defined"() {
    given:
    options.exclude << 'Exclude.*'

    expect:
    !options.isTypeIncluded(new Type(name: 'ExcludeMe'))
    !options.isTypeIncluded(new Type(name: 'ExcludeHim'))
    options.isTypeIncluded(new Type(name: 'yes1'))
    options.isTypeIncluded(new Type(name: 'yes2'))
  }

  def "should exclude and include"() {
    given:
    options.exclude << '.*Exclude.*'
    options.include << '.*Include.*'

    expect:
    !options.isTypeIncluded(new Type(name: 'ExcludeMe'))
    !options.isTypeIncluded(new Type(name: 'ExcludeSomethingElse'))
    options.isTypeIncluded(new Type(name: 'IncludeMe'))
    !options.isTypeIncluded(new Type(name: 'ExcludeAndNotIncludeMe'))
    !options.isTypeIncluded(new Type(name: 'another'))
  }

  /** Test options. */
  private static class Options extends JavaGeneratorOptions {

  }

}
