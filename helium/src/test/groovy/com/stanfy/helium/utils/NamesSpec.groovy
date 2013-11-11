package com.stanfy.helium.utils

import spock.lang.Specification

/**
 * Tests for Names.
 */
class NamesSpec extends Specification {

  def "should replace spaces, dots, and slashes and ignore others"() {
    expect:
    Names.canonicalName(name) == canonicalName

    where:
    name                                    | canonicalName
    "  "                                    | ""
    "_"                                     | ""
    "!"                                     | ""
    "/some/path"                            | "some_path"
    "combination/of/symbols+something else" | "combination_of_symbolssomething_else"
    "one.two"                               | "one_two"
  }

}
