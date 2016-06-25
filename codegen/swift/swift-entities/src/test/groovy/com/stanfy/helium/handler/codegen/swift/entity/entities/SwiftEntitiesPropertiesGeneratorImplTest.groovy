package com.stanfy.helium.handler.codegen.swift.entity.entities

import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import spock.lang.Specification

class SwiftEntitiesPropertiesGeneratorImplTest extends Specification {
  ProjectDsl dsl

  Message testMessage
  Sequence listMessage
  Message listWithName
  Message structMessage

  SwiftEntitiesGenerator sut
  List<SwiftEntity> entities

  void setup() {
    dsl = new ProjectDsl()
    dsl.type 'int32'
    dsl.type 'float'
    dsl.type 'string'
    dsl.type 'A' message {
      f1 'int32' required
      f2 'float' optional
      f3 'string' optional
      f4(skip: true)
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
    sut = new SwiftEntitiesGeneratorImpl()
  }

  def "parse entity names"() {
    when:
      entities = sut.entitiesFromHeliumProject(dsl)

    then:
      entities.findResult { it.name =="A"} != null
      entities.findResult { it.name =="List"} != null
      entities.findResult { it.name =="ListWithName"} != null
      entities.findResult { it.name =="Struct"} != null
  }

  def "parse entity properties names"() {
    SwiftEntity A
    SwiftEntity L
    SwiftEntity ListWihName
    SwiftEntity Struct

    when:
    entities = sut.entitiesFromHeliumProject(dsl)
    A = entities.find { it.name == "A"}
    L = entities.find { it.name == "List"}
    ListWihName = entities.find { it.name == "ListWithName"}
    Struct = entities.find { it.name =="Struct"}

    then:
    A.properties != null
    A.properties.any { it.name == "f1" }
    A.properties.any { it.name == "f2" }
    A.properties.any { it.name == "f3" }
    L.properties.size() == 0
    ListWihName.properties.any { it.name == "name" }
    ListWihName.properties.any { it.name == "items" }
    Struct.properties.any { it.name == "a" }
    Struct.properties.any { it.name == "b" }
  }

  def "skip entity properties if needed"() {
    SwiftEntity A

    when:
    entities = sut.entitiesFromHeliumProject(dsl)
    A = entities.find { it.name == "A"}

    then:
    A.properties != null
    !A.properties.any { it.name == "f4" }
  }

  def "maps property types to Swift types"() {
    given:
    Project prj = new ProjectDsl()
    prj.type heliumType
    prj.type "A" message {
      prop heliumType
    }
    def propertyType = sut.entitiesFromHeliumProject(prj).first().properties.first().type.name

    expect:
    swiftType == propertyType

    where:
    heliumType | swiftType
    "int32"    | "Int"
    "integer"  | "Int"
    "int64"    | "Int"
    "long"     | "Int"
    "bool"     | "Bool"
    "boolean"  | "Bool"
    "float"    | "Double"
    "float32"  | "Double"
    "float64"  | "Double"
    "double"   | "Double"
    "string"   | "String"
  }

}
