package com.stanfy.helium.utils

import spock.lang.Specification

/**
 * Tests for Names.
 */
class NamesSpec extends Specification {

  def "canonical name should replace spaces, dots, and slashes and ignore others"() {
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

  def "packageNameToPath"() {
    expect:
    Names.packageNameToPath(name) == path

    where:
    name                  | path
    "com.stanfy"          | "com/stanfy"
    "a.b.c.d.e.f"         | "a/b/c/d/e/f"
    "com.another_example" | "com/another_example"
  }

}
