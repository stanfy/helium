package com.stanfy.helium.entities.json

import com.google.gson.stream.JsonReader
import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.entities.ConverterFactory
import com.stanfy.helium.entities.TypedEntity
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import spock.lang.Specification

/**
 * Spec for GsonValidator.
 */
class JsonEntityReaderSpec extends Specification {

  ConverterFactory<JsonReader, ?> converters

  Message testMessage
  Sequence listMessage
  Message structMessage

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
      name 'string'
      items 'A' sequence
    }
    dsl.type 'Struct' message {
      a 'A' required
      b 'ListWithName' optional
    }
    testMessage = dsl.messages[0]
    listMessage = dsl.sequences[0]
    structMessage = dsl.messages[2]

    converters = dsl.types.findConverters(JsonConverterFactory.JSON)
  }

  private TypedEntity read(final Type type, final String json) {
    JsonEntityReader reader = new JsonEntityReader(new StringReader(json), converters)
    return reader.read(type)
  }

  def "fails on unsupported type"() {
    when:
    ProjectDsl dsl = new ProjectDsl()
    dsl.type 'custom'
    dsl.type 'C' message {
      field 'custom'
    }
    read(dsl.types.byName('C'), '{"field" : "custom value"}')

    then:
    def e = thrown(UnsupportedOperationException)
    e.message.contains("custom")
  }

  def "checks objects vs arrays"() {
    given:
    def res = read(testMessage, '[]')

    expect:
    res != null
    res.value == null
    res.validationErrors.size() == 1
    res.validationErrors[0].type == testMessage
    res.validationErrors[0].explanation.contains('not an object')
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
    def res = read(testMessage, json)

    expect:
    res != null
    res.validationErrors.empty
    res.value.size() == 3
    res.value.f1 == 2
    res.value.f2 == 1.5
    res.value.f3 == "abc"
  }

  def "tracks string instead of int"() {
    given:
    def json = '''
      {
        "f1" : "abc",
        "f2" : 1.5
      }
    '''
    def errors = read(testMessage, json).validationErrors

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
    def errors = read(testMessage, json).validationErrors

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
    def errors = read(testMessage, json).validationErrors

    expect:
    errors.size() == 1
    errors[0].type == testMessage
    errors[0].field.name == 'f3'
  }

  def "reports unknown fields"() {
    given:
    def errors = read(testMessage, '{"aha" : "value"}').validationErrors

    expect:
    !errors.empty
    errors[0].type == testMessage
    errors[0].explanation.contains("aha")
  }

  def "checks required fields"() {
    given:
    def errors = read(testMessage, '{}').validationErrors

    expect:
    errors.size() == 1
    errors[0].type == testMessage
    errors[0].field != null
    errors[0].field.name == 'f1'
    errors[0].explanation.contains("not provided")
  }

  def "accepts valid arrays"() {
    given:
    def res = read(listMessage, '''
      [
        {
          "f1" : 2,
          "f2" : 1.5,
          "f3" : "abc"
        }
      ]
    ''')

    expect:
    res != null
    res.validationErrors.empty
    res.value.size() == 1
    res.value[0].size() == 3
    res.value[0].f3 == "abc"
  }

  def "accepts empty arrays"() {
    given:
    def res = read(listMessage, '[]')

    expect:
    res != null
    res.validationErrors.empty
    res.value.empty
  }

  def "validates arrays"() {
    given:
    def errors = read(listMessage, '''
      [
        {
          "f3" : 2
        }
      ]
    ''').validationErrors

    expect:
    errors.size() == 1
    !errors[0].children?.empty
    errors[0].children[0].type == testMessage
    errors[0].children[1].type == testMessage
  }

  def "validates primitives only"() {
    given:
    def res1 = read(new Type(name : 'int32'), '2')
    def res2 = read(new Type(name : 'int32'), '"aha"')

    expect:
    res1 != null && res2 != null
    res1.validationErrors.empty
    !res2.validationErrors.empty
    res1.value == 2
    res2.value == null
  }

  def "some complex validations are also possible..."() {
    given:
    def res = read(structMessage, '''
      {
        "a" : {
          "f1" : 1
        },
        "b" : {
          "name" : "test list",
          "items" : [
            {
              "f1" : 2
            },
            {
              "f1" : 3
            }
          ]
        }
      }
    ''')

    expect:
    res != null
    res.validationErrors.empty
    res.value?.a?.f1 == 1
    res.value?.b?.name == "test list"
    res.value?.b?.items[1]?.f1 == 3
  }

  def "and validations works on these complex examples :)"() {
    given:
    def res = read structMessage, '''
      {
        "a" : {
          // "f1" : 1
        },
        "b" : {
          // "name" : "test list",
          "items" : [
            {
              "f1" : 2
            },
            {
              // "f1" : 3
            }
          ]
        }
      }
    '''
    def errors = res?.validationErrors

    expect:
    res != null

    errors.size() == 2
    errors[0].field.name == "a"
    !errors[0].children.empty
    errors[0].children[0].field.name == "f1"

    errors[1].field.name == "b"
    !errors[1].children.empty
    errors[1].children[1].field.name == "name"
    def deepErrors = errors[1].children[0].children
    !deepErrors.empty
    !deepErrors[0].children?.empty
    deepErrors[0].children[0].field.name == 'f1'

    res.value?.containsKey('a')
    res.value?.containsKey('b')
    res.value?.b?.items[0]?.f1 == 2
  }

  def "treats nulls"() {
    given:
    def errors = read(testMessage, '''
      {
        "f1" : null,
        "f2" : null,
        "f3" : null
      }
    ''').validationErrors

    expect:
    errors.size() == 1
    errors[0].explanation.contains("required but got NULL")
    errors[0].field.name == "f1"
  }

  def "treats nulls in complex fields"() {
    given:
    ((Message)structMessage.fieldByName('b').type).fieldByName('items').required = true
    def errors = read(structMessage, '''
      {
        "a" : {
          "f1" : null,
          "f3" : null
        },
        "b" : {
          "name" : "test list",
          "items" : null
        }
      }
    ''').validationErrors

    expect:
    errors.size() == 2

    errors[0].field.name == "a"
    errors[0].children.size() == 1
    errors[0].children[0].field.name == "f1"
    errors[0].children[0].explanation.contains("required but got NULL")

    errors[1]?.field?.name == "b"
    !errors[1].children.empty
    errors[1].children[0]?.field?.name == "items"
    errors[1].children[0].explanation.contains("required but got NULL")
  }

}
