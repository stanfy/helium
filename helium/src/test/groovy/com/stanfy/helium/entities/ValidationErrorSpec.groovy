package com.stanfy.helium.entities

import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type
import spock.lang.Specification

/**
 * Validation error specification.
 */
class ValidationErrorSpec extends Specification {

  Message type1, type2

  def setup() {
    Message msg1 = new Message(name: "Type1")
    Message msg2 = new Message(name: "Type2")
    msg2.addField(new Field(name: "f1", type: new Type(name: "string")))
    msg2.addField(new Field(name: "f2", type: new Type(name: "int")))
    msg1.addField(new Field(name: "f1", type: new Type(name: "string")))
    msg1.addField(new Field(name: "f2", type: msg1))
    msg1.addField(new Field(name: "f3", type: msg1))
    type1 = msg1
    type2 = msg2
  }

  def "should render explanation"() {
    given:
    ValidationError error = new ValidationError("explain")
    expect:
    error.toString() == "explain"
  }

  def "should render field error"() {
    given:
    ValidationError error = new ValidationError(type1, type1.fields[0], "explain")
    expect:
    error.toString() == "'f1': explain"
  }

  def "should render errors tree"() {
    given:
    ValidationError root = new ValidationError(type1, "parent explain")
    def e1 = new ValidationError(type2, type2.fields[0], "e1")
    root.setChildren([
        e1,
        new ValidationError(type2, type2.fields[1], "e2")
    ])
    e1.setChildren([
        new ValidationError(type2, type2.fields[0], "e3"),
        new ValidationError(type1, type1.fields[0], "e4")
    ])

    expect:
    root.toString() == '''
Type1: parent explain
  - 'f1': e1
    - 'f1': e3
    - 'f1': e4
  - 'f2': e2
'''.trim()
  }

  def "should render indexes"() {
    given:
    ValidationError root = new ValidationError(type1, "parent explain")
    def e1 = new ValidationError(type2, 0, "e1")
    root.setChildren([
        e1,
        new ValidationError(type2, 1, "e2")
    ])
    e1.setChildren([
        new ValidationError(type2, type2.fields[0], "e3"),
        new ValidationError(type1, type1.fields[0], "e4")
    ])

    expect:
    root.toString() == '''
Type1: parent explain
  [0] Type2: e1
      - 'f1': e3
      - 'f1': e4
  [1] Type2: e2
'''.trim()
  }

}
