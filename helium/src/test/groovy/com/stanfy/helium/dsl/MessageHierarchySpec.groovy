package com.stanfy.helium.dsl

import com.stanfy.helium.model.Message
import spock.lang.Specification

/**
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
class MessageHierarchySpec extends Specification {

  def "Can create right string representation" () {
    given:
    def i = 0
    LinkedHashSet<MessageHierarchy.Node> nodes = new LinkedHashSet<>()
    3.times {
      Message msg = new Message()
      msg.name = "Type" + i++
      nodes.add new MessageHierarchy.Node(msg)
    }

    expect:
    MessageHierarchy.cycleToString(nodes) == "Type0 -> Type1 -> Type2"
  }

  def "Can not accept wrong parent" () {
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

  def "Can find simple cycle" () {
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
