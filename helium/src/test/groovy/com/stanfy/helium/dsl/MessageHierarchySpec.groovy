package com.stanfy.helium.dsl

import com.stanfy.helium.model.Message
import spock.lang.Specification

/**
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
class MessageHierarchySpec extends Specification {

  MessageHierarchy hierarchy
  ArrayList<Message> messages

  def setup() {
    hierarchy = new MessageHierarchy()
    messages = new ArrayList<Message>()
  }

  def "Can create right string representation" () {
    given:
    LinkedHashSet<String> nodes = new LinkedHashSet<>()
    3.times {
      nodes.add "Type" + it
    }

    expect:
    MessageHierarchy.cycleToString(nodes) == "Type0 -> Type1 -> Type2"
  }

  def "Can not accept wrong parent" () {
    setup:
    messages << new Message(name: "M1")
    messages << new Message(name: "M2", parent: "M3")

    when:
    hierarchy.buildAndValidate(messages)

    then:
    def ex = thrown(IllegalArgumentException)
    ex.getMessage() =~ MessageHierarchy.PREFIX_PARENT_TYPE_NOT_FOUND
  }

  def "Can find simple cycle" () {
    setup:
    messages << new Message(name: "M1", parent: "M2")
    messages << new Message(name: "M2", parent: "M1")

    when:
    hierarchy.buildAndValidate(messages)

    then:
    thrown(IllegalArgumentException.class)
  }

  def "Can find cycle in hierarchy with leaves" () {
    given: "rhombus cycle hierarchy"
    messages << new Message(name: "a", parent: "d")
    messages << new Message(name: "b", parent: "a")
    messages << new Message(name: "c", parent: "b")
    messages << new Message(name: "d", parent: "c")

    and: "terminal nodes"
    messages << new Message(name: "b1", parent: "a")
    messages << new Message(name: "e", parent: "b")
    messages << new Message(name: "f1", parent: "e")
    messages << new Message(name: "f2", parent: "e")

    when:
    hierarchy.buildAndValidate(messages)

    then:
    def ex = thrown(IllegalArgumentException)
    def exMsg = ex.getMessage()
    exMsg =~ MessageHierarchy.PREFIX_CYCLE_DEPENDENCIES
    exMsg.contains " a "
    exMsg.contains " b "
    exMsg.contains " c "
    exMsg.contains " d "

  }
}
