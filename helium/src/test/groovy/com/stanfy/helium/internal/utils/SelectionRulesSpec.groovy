package com.stanfy.helium.internal.utils

import spock.lang.Specification

import java.util.regex.Pattern

/** Spec for selection rules. */
class SelectionRulesSpec extends Specification {

  def "empty rules"() {
    given:
    def rules = new SelectionRules("test")

    expect:
    rules.check("any")
  }

  def "include rules"() {
    given:
    def rules = new SelectionRules("test")
    rules.includes 'test 1', 'test 2'

    expect:
    !rules.check("any")
    rules.check("test 1")
    rules.check("test 2")
  }

  def "exclude rules"() {
    given:
    def rules = new SelectionRules("test")
    rules.excludes 'test 1', 'test 2'

    expect:
    rules.check("any")
    !rules.check("test 1")
    !rules.check("test 2")
  }

  def "exclude patterns"() {
    given:
    def rules = new SelectionRules("test")
    rules.excludes '\\w+ 1'

    expect:
    rules.check("any")
    !rules.check("test 1")
    rules.check("test 2")
    rules.check("test")
  }

  def "nesting"() {
    given:
    def root = new SelectionRules("root")
    root.nest(new SelectionRules("foo"))
    root.nest(new SelectionRules("bar"))

    expect:
    root.nested("foo").check("any")
    root.nested("bar").check("any")
  }

  def "use strings for patterns"() {
    given:
    def rules = new SelectionRules("test")

    when:
    rules.includes '.*/some/path/.+', '/'

    then:
    rules.includes.size() == 2
  }

  def "use string list for patterns"() {
    given:
    def rules = new SelectionRules("test")

    when:
    rules.includes(['.*/some/path/.+', '/'])

    then:
    rules.includes.size() == 2
  }

  def "use string list for exclude patterns"() {
    given:
    def rules = new SelectionRules("test")

    when:
    rules.excludes(['.*/some/path/.+', '/'])

    then:
    rules.excludes.size() == 2
  }

  def "use patterns"() {
    given:
    def rules = new SelectionRules("test")

    when:
    rules.includePatterns([Pattern.compile('.*/some/path/.+')])

    then:
    rules.includes.size() == 1
  }

  def "use patterns for excludes"() {
    given:
    def rules = new SelectionRules("test")

    when:
    rules.excludePatterns([Pattern.compile('.*/some/path/.+')])

    then:
    rules.excludes.size() == 1
  }
}
