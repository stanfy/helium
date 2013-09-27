package com.stanfy.helium

import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.model.Note
import com.stanfy.helium.model.tests.MethodTestInfo
import com.stanfy.helium.model.tests.TestsInfo
import spock.lang.Specification

/**
 * Spec for HeliumWriter.
 */
class HeliumWriterSpec extends Specification {

  StringWriter out = new StringWriter()

  HeliumWriter writer = new HeliumWriter(out)

  ProjectDsl dsl = new ProjectDsl()

  def "can write notes"() {
    when:
    writer.writeNote(new Note(value : "test"))

    then:
    out.toString() == "note '''\n  test\n'''\n"
  }

  def "can write service test info"() {
    when:
    writer.writeTestsInfo(new TestsInfo(useExamples: true))

    then:
    out.toString() == '''
tests {
  useExamples true
}
'''.trim() + '\n'
  }

  def "can write method test info"() {
    when:
    writer.writeMethodTestsInfo(new MethodTestInfo(pathExample: [p1 : 'v1', p2 : 'v2']))

    then:
    out.toString() == '''
tests {
  useExamples false
  pathExample [
    'p1': 'v1',
    'p2': 'v2'
  ]
}
'''.trim() + '\n'
  }

  // TODO: check examples and descriptions

  def "can write projects"() {
    when:
    dsl.note "note1"
    dsl.type 'int32'
    dsl.type 'bool'
    dsl.type 'A' message {
      id 'int32'
      count 'int32' optional
      ex {
        type 'bool'
        sequence true
      }
    }
    dsl.service {
      name "Service name"
      description "Service description"
      location "http://host/"
      version 1

      post "/user/@id" spec {
        name "Update user profile"
        description "123"
        parameters {
          dob 'bool' optional
        }
        response {
          code 'int32'
        }
        body 'A'
        tests {
          useExamples true
          pathExample id: '123'
        }
      }
    }
    writer.writeProject(dsl)

    then:
    out.toString() == """
note '''
  note1
'''
type 'int32'
type 'bool'
type 'A' message {
  id {
    type 'int32'
    required true
  }
  count {
    type 'int32'
    required false
  }
  ex {
    type 'bool'
    required true
    sequence true
  }
}
service {
  name 'Service name'
  version '1'
  location 'http://host/'
  post '/user/@id' spec {
    name 'Update user profile'
    parameters {
      dob {
        type 'bool'
        required false
      }
    }
    body 'A'
    response {
      code {
        type 'int32'
        required true
      }
    }
    tests {
      useExamples true
      pathExample [
        'id': '123'
      ]
    }
  }
  tests {
    useExamples false
  }
}
""".trim() + '\n'
  }

}
