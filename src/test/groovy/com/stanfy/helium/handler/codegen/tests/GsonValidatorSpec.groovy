package com.stanfy.helium.handler.codegen.tests

import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.handler.codegen.tests.json.GsonValidator
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import spock.lang.Specification

/**
 * Spec for GsonValidator.
 */
class GsonValidatorSpec extends Specification {
  
  Message testMessage
  Sequence listMessage
  Message structMessage

  GsonValidator testValidator

  void setup() {
    ProjectDsl dsl = new ProjectDsl()
    dsl.type 'int32'
    dsl.type 'float'
    dsl.type 'string'
    dsl.type 'A' message {
      f1 'int32' required
      f2 'float' optional
      f3 'string' optional
    }
    dsl.type 'List' sequence 'A'
    dsl.type 'ListWithName' message {
      items 'A' sequence
    }
    dsl.type 'Struct' message {
      a 'A' required
      b 'ListWithName' optional
    }
    testMessage = dsl.messages[0]
    listMessage = dsl.sequences[0]
    structMessage = dsl.messages[2]
    testValidator = new GsonValidator(testMessage)
  }

  def "fails on unsupported type"() {
    when:
    ProjectDsl dsl = new ProjectDsl()
    dsl.type 'custom'
    dsl.type 'C' message {
      field 'custom'
    }
    def validator = new GsonValidator(dsl.messages[0])
    validator.validate('{"field" : "custom value"}')

    then:
    def e = thrown(UnsupportedOperationException)
    e.message.contains("custom")
  }

  def "checks objects vs arrays"() {
    given:
    def errors = testValidator.validate('[]')

    expect:
    errors.size() == 1
    errors[0].type == testMessage
    errors[0].explanation.contains('is not')
  }

  def "accepts valid primitive types"() {
    given:
    def json = '''
      {
        "f1" : 2,
        "f2" : 1.5,
        "f3" : "abc"
      }
    '''

    expect:
    testValidator.validate(json).empty
  }

  def "tracks string instead of int"() {
    given:
    def json = '''
      {
        "f1" : "abc",
        "f2" : 1.5
      }
    '''
    def errors = testValidator.validate(json)

    expect:
    errors.size() == 1
    errors[0].type == testMessage
    errors[0].field != null
    errors[0].field.name == 'f1'
  }

  def "tracks float instead of int"() {
    given:
    def json = '''
      {
        "f2" : 1.5,
        "f1" : 2.3
      }
    '''
    def errors = testValidator.validate(json)

    expect:
    errors.size() == 1
    errors[0].type == testMessage
    errors[0].field != null
    errors[0].field.name == 'f1'
  }

  def "tracks int instead of string"() {
    given:
    def json = '''
      {
        "f1" : 1,
        "f3" : 1
      }
    '''
    def errors = testValidator.validate(json)

    expect:
    errors.size() == 1
    errors[0].type == testMessage
    errors[0].field.name == 'f3'
  }

  def "reports unknown fields"() {
    given:
    def errors = testValidator.validate('{"aha" : "value"}')

    expect:
    !errors.empty
    errors[0].type == testMessage
    errors[0].explanation.contains("aha")
  }

  def "checks required fields"() {
    given:
    def errors = testValidator.validate('{}')

    expect:
    errors.size() == 1
    errors[0].type == testMessage
    errors[0].field != null
    errors[0].field.name == 'f1'
    errors[0].explanation.contains("not provided")
  }

  def "accepts valid arrays"() {
    given:
    testValidator = new GsonValidator(listMessage)
    def errors = testValidator.validate('''
      [
        {
          "f1" : 2,
          "f2" : 1.5,
          "f3" : "abc"
        }
      ]
    ''')

    expect:
    errors.empty
  }

  def "accepts empty arrays"() {
    given:
    testValidator = new GsonValidator(listMessage)
    def errors = testValidator.validate('[]')

    expect:
    errors.empty
  }

  def "validates arrays"() {
    given:
    testValidator = new GsonValidator(listMessage)
    def errors = testValidator.validate('''
      [
        {
          "f3" : 2
        }
      ]
    ''')

    expect:
    errors.size() == 2
    errors[0].type == testMessage
    errors[1].type == testMessage
  }

  def "validates primitives only"() {
    given:
    def errors1 = new GsonValidator(new Type(name : 'int32')).validate('2')
    def errors2 = new GsonValidator(new Type(name : 'int32')).validate('"aha"')

    expect:
    errors1.empty
    !errors2.empty
  }
  
}
