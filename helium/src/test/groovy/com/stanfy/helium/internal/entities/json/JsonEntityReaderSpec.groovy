package com.stanfy.helium.internal.entities.json

import com.google.gson.stream.JsonReader
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.internal.entities.ConvertersPool
import com.stanfy.helium.internal.entities.TypedEntity
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.constraints.ConstrainedType
import spock.lang.Specification

/**
 * Spec for GsonValidator.
 */
class JsonEntityReaderSpec extends Specification {

  ConvertersPool<JsonReader, ?> converters

  ProjectDsl dsl

  Message testMessage
  Sequence listMessage
  Message listWithName
  Message structMessage

  void setup() {
    dsl = new ProjectDsl()
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
    listWithName = dsl.messages[1]

    converters = dsl.types.findConverters(JsonConvertersPool.JSON)
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
    res.validationError.type == testMessage
    !res.validationError.children.empty
    res.validationError.children[0].explanation.contains('not an object')
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
    res.validationError == null
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
    def error = read(testMessage, json).validationError

    expect:
    error.type == testMessage
    !error.children.empty
    error.children[0].field != null
    error.children[0].field.name == 'f1'
  }

  def "tracks float instead of int"() {
    given:
    def json = '''
      {
        "f2" : 1.5,
        "f1" : 2.3
      }
    '''
    def error = read(testMessage, json).validationError

    expect:
    error.type == testMessage
    !error.children.empty
    error.children[0].field != null
    error.children[0].field.name == 'f1'
  }

  def "tracks int instead of string"() {
    given:
    def json = '''
      {
        "f1" : 1,
        "f3" : 1
      }
    '''
    def error = read(testMessage, json).validationError

    expect:
    error.type == testMessage
    !error.children.empty
    error.children[0].field != null
    error.children[0].field.name == 'f3'
  }

  def "reads required floats in arrays"() {
    given:
    ((Message)dsl.typeResolver.byName('A')).fieldByName('f2').required = true
    def error = read(listWithName, '''
      {
        "name" : "name",
        "items" : [
          {
            "f1" : 1,
            "f2" : 4.99
          },
          {
            "f1" : 2
          }
        ]
      }
    ''').validationError

    expect:
    error != null
    error.children[0].children[0] != null
    error.children[0].children[0].index == 1
    error.children[0].children[0].field == null
    error.children[0].children[0].children[0]?.field?.name == 'f2'
  }

  def "reports unknown fields"() {
    given:
    def error = read(testMessage, '{"aha" : "value"}').validationError

    expect:
    error.type == testMessage
    error.children.size() == 2
    error.children[0].explanation.contains("aha")
    error.children[1].field.name == 'f1'
  }

  def "checks required fields"() {
    given:
    def error = read(testMessage, '{}').validationError

    expect:
    error.type == testMessage
    error.children.empty != null
    error.children[0].field != null
    error.children[0].field.name == 'f1'
    error.children[0].explanation.contains("not provided")
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
    res.validationError == null
    res.value.size() == 1
    res.value[0].size() == 3
    res.value[0].f3 == "abc"
  }

  def "accepts empty arrays"() {
    given:
    def res = read(listMessage, '[]')

    expect:
    res != null
    res.validationError == null
    res.value.empty
  }

  def "validates arrays"() {
    given:
    def error = read(listMessage, '''
      [
        {
          "f3" : 2
        }
      ]
    ''').validationError

    expect:
    !error.explanation.empty
    error.children.size() == 1
    error.children[0].index == 0
    error.children[0].children[0].field != null
    error.children[0].children[0].field.name == 'f3'
    error.children[0].children[1].field != null
    error.children[0].children[1].field.name == 'f1'
  }

  def "validates primitives only"() {
    given:
    def res1 = read(new Type(name : 'int32'), '2')
    def res2 = read(new Type(name : 'int32'), '"aha"')

    expect:
    res1 != null && res2 != null

    res1.validationError == null
    res2.validationError != null
    res2.validationError.children == null
    !res2.validationError.explanation.empty

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
    res.validationError == null
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
    def error = res?.validationError

    expect:
    res != null
    !error.explanation.empty

    error.children.size() == 2
    error.children[0].field.name == "a"
    !error.children[0].children.empty
    error.children[0].children[0].field.name == "f1"

    error.children[1].field.name == "b"
    !error.children[1].children.empty
    error.children[1].children[0].field.name == "items"
    !error.children[1].children[0].children.empty
    error.children[1].children[0].children[0].index == 1
    error.children[1].children[1].field.name == "name"

    res.value?.containsKey('a')
    res.value?.containsKey('b')
    res.value?.b?.items[0]?.f1 == 2
  }

  def "treats nulls"() {
    given:
    def error = read(testMessage, '''
      {
        "f1" : null,
        "f2" : null,
        "f3" : null
      }
    ''').validationError
    def errors = error.children

    expect:
    !error.explanation.empty
    errors.size() == 1
    errors[0].explanation.contains("required but got NULL")
    errors[0].field.name == "f1"
  }

  def "treats nulls in complex fields"() {
    given:
    ((Message)structMessage.fieldByName('b').type).fieldByName('items').required = true
    def error = read(structMessage, '''
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
    ''').validationError
    def errors = error.children

    expect:
    !error.explanation.empty
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

  def "can read dates"() {
    given:
    dsl.type "foo" spec {
      description "bar"
      from("json") { asDate("yyyy-MM-dd") }
    }
    dsl.type "FooMsg" message {
      bar 'foo'
    }
    def res = read(dsl.types.byName('FooMsg'), '''
      {
        "bar" : "2013-07-11"
      }
    ''')

    expect:
    res.value.bar instanceof Date
    ((Date) res.value.bar).format("dd-MM-yyyy") == '11-07-2013'
    res.validationError == null
  }

  def "can validate dates"() {
    given:
    dsl.type "foo" spec {
      description "bar"
      from("json") { asDate("yyyy-MM-dd") }
    }
    dsl.type "FooMsg" message {
      bar 'foo'
      field 'string'
      field2 'int32'
    }
    dsl.type "Container" message {
      items 'FooMsg' sequence
    }
    def error = read(dsl.types.byName('Container'), '''
      {
        "items" : [
          {"bar" : "bad string", "field" : "abc", "field2" : '2'},
          {"bar" : "2013-07-11", "field2" : '3'}
        ]
      }
    ''').validationError

    expect:
    !error.explanation.empty
    error.children.size() == 1
    error.children[0].children[0]?.children[0]?.explanation?.contains("bad string")
    error.children[0].children.size() > 1
  }

  def "ignores skipped fields"() {
    given:
    dsl.type "FooMsg" message {
      normal 'int32'
      ignored1(skip: true)
      ignored2(skip: true)
    }
    def error = read(dsl.types.byName('FooMsg'), '''
      {
        "normal": 23,
        "ignored1": 'any value'
      }
    ''').validationError

    expect:
    error == null
  }

  def "respects skipUnknown option in messages"() {
    given:
    dsl.type "FooMsg" message(skipUnknownFields: true) {
      normal 'int32'
    }
    def error = read(dsl.types.byName('FooMsg'), '''
      {
        "normal": 23,
        "ignored1": 'any value',
        "foo": "bar",
        "blabla": false
      }
    ''').validationError

    expect:
    error == null
  }

  def "applies constraints"() {
    given:
    dsl.type "weekendDay" spec {
      constraints("string") {
        enumeration "Sat", "Sun"
      }
    }
    def okResult = read(dsl.types.byName("weekendDay"), '"Sat"')
    def errResult = read(dsl.types.byName("weekendDay"), '"Mon"')

    expect:
    okResult.type instanceof ConstrainedType
    okResult.value == "Sat"
    okResult.validationError == null
    errResult.type instanceof ConstrainedType
    errResult.value == "Mon"
    errResult.validationError.explanation.contains("Sun")
    errResult.validationError.explanation.contains("Mon")
  }

}
