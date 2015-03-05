package com.stanfy.helium.dsl

import com.stanfy.helium.model.Message
import spock.lang.Specification

/**
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
class MessageHierarchySpec extends Specification {

  def "Should create right string representation" () {
    given:
    MessageHierarchy hierarchy = new MessageHierarchy()

    def i = 0
    LinkedHashSet<Node> nodes = new LinkedHashSet<>()
    3.times {
      Message msg = new Message()
      msg.name = "Type" + i++
      nodes << new MessageHierarchy.Node(msg)
    }

    expect:
    hierarchy.cycleToString(nodes) == "Type0 -> Type1 -> Type2"
  }

  def "Should not accept wrong parent" () {
    setup:
    MessageHierarchy hierarchy = new MessageHierarchy()
    def messages = new ArrayList<Message>()
    messages << new Message(name: "M1")
    messages << new Message(name: "M2", parent: "M3")

    when:
    hierarchy.buildAndValidate(messages)

    then:
    thrown(IllegalArgumentException.class)
  }

  def "Should find simple cycle" () {
    setup:
    MessageHierarchy hierarchy = new MessageHierarchy()
    def messages = new ArrayList<Message>()
    messages << new Message(name: "M1", parent: "M2")
    messages << new Message(name: "M2", parent: "M1")

    when:
    hierarchy.buildAndValidate(messages)

    then:
    thrown(IllegalArgumentException.class)
  }
}
