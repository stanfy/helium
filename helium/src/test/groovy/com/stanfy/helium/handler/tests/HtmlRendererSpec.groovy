package com.stanfy.helium.handler.tests

import com.stanfy.helium.model.tests.BehaviourCheck
import com.stanfy.helium.model.tests.BehaviourSuite
import spock.lang.Specification

class HtmlRendererSpec extends Specification {

  HtmlRenderer renderer

  def setup() {
    renderer = new HtmlRenderer()
  }

  def "has logs for checks"() {
    given:
    BehaviourSuite suite = new BehaviourSuite(
        name: "l1",
        children: [
            new BehaviourSuite(
                name: "l2",
                children: [new BehaviourCheck(name: "c1")]
            )
        ]
    )

    when:
    renderer.onSuiteStarted(suite)
    renderer.write("l1 log")
    renderer.onSuiteStarted(suite.children[0] as BehaviourSuite)
    renderer.write("l2 log")
    renderer.onCheckStarted((suite.children[0] as BehaviourSuite).children[0])
    renderer.write("c log")
    renderer.onCheckDone((suite.children[0] as BehaviourSuite).children[0])
    renderer.write("l2 log 2")
    renderer.onSuiteDone(suite.children[0] as BehaviourSuite)
    renderer.write("l1 log 2")
    renderer.onSuiteDone(suite)
    def scope = renderer.buildScope(suite)

    then:
    scope.children[0].details.contains("l1 log")
    scope.children[0].details.contains("l1 log 2")
    scope.children[0].details.contains("l2 log")
    scope.children[0].details.contains("l2 log 2")
    scope.children[0].children[0].details.contains("c log")
  }

}
