package com.stanfy.helium.handler.codegen.swift.entity.entities

import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Sequence
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
    SwiftEntity entityA
    SwiftEntityArray entityL
    SwiftEntity entityListWihName
    SwiftEntity entityStruct

    when:
    entities = sut.entitiesFromHeliumProject(dsl)
    entityA = entities.find { it.name == "A"}
    entityL = entities.find { it.name == "List"} as SwiftEntityArray
    entityListWihName = entities.find { it.name == "ListWithName"}
    entityStruct = entities.find { it.name =="Struct"}

    then:
    entityA.properties != null
    entityA.properties.any { it.name == "f1" }
    entityA.properties.any { it.name == "f2" }
    entityA.properties.any { it.name == "f3" }
    entityL.itemType.name == "A"
    entityListWihName.properties.any { it.name == "name" }
    entityListWihName.properties.any { it.name == "items" }
    entityStruct.properties.any { it.name == "a" }
    entityStruct.properties.any { it.name == "b" }
  }

  def "skip entity properties if needed"() {
    SwiftEntity entityA

    when:
    entities = sut.entitiesFromHeliumProject(dsl)
    entityA = entities.find { it.name == "A"}

    then:
    entityA.properties != null
    !entityA.properties.any { it.name == "f4" }
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

  def "should camelize underscored names"() {
    given:
    Project prj = new ProjectDsl()
    prj.type "string"
    prj.type "A" message {
      "$fieldName" 'string'
    }
    def actualPropertyName = sut.entitiesFromHeliumProject(prj).first().properties.first().name

    expect:
    actualPropertyName == propertyName

    where:
    fieldName | propertyName
    "abc"     | "abc"
    "ab_c"    | "abC"
    "a_bc"    | "aBc"
  }

  def "should handle keywords"() {
    given:
    Project prj = new ProjectDsl()
    prj.type "string"
    prj.type "A" message {
      "$fieldName" 'string'
    }
    def actualPropertyName = sut.entitiesFromHeliumProject(prj).first().properties.first().name

    expect:
    actualPropertyName == propertyName

    where:
    fieldName     | propertyName
    "default"     | "defaultValue"
    "case"        | "caseValue"
    "let"         | "letValue"
    "enum"        | "enumValue"
  }

  def "should handle optional fields"() {
    SwiftEntityStruct entityA

    given:
    Project prj = new ProjectDsl()
    prj.type "string"
    prj.type "A" message {
      optionalField 'string' optional
      nonOptionalField 'string'
    }

    when:
    entityA = (sut.entitiesFromHeliumProject(prj).first() as SwiftEntityStruct)

    then:
    entityA.properties.find { it.name == "optionalField" }.type.optional
    !entityA.properties.find { it.name == "nonOptionalField"}.type.optional
  }

  def "should handle sequence fields with optional vlues"() {
    SwiftEntityStruct entityA
    SwiftEntityStruct entityB

    given:
    Project prj = new ProjectDsl()
    prj.type "string"
    prj.type "B" message {
      sequenceField 'string' sequence
    }

    prj.type "A" message {
      sequenceOfSequence 'B' sequence
      nonSequenceField 'B'
      nonSequenceFieldOptional 'B' optional
    }

    when:
    entityA = (sut.entitiesFromHeliumProject(prj, null).find { it.name == "A" } as SwiftEntityStruct)
    entityB = (sut.entitiesFromHeliumProject(prj, null).find { it.name == "B" } as SwiftEntityStruct)

    then:
    entityB.properties.find { it.name == "sequenceField" }.type instanceof SwiftEntityArray
    entityA.properties.find { it.name == "nonSequenceField" }.type instanceof SwiftEntityStruct
    entityA.properties.find { it.name == "sequenceOfSequence" }.type instanceof SwiftEntityArray
    entityA.properties.find { it.name == "nonSequenceFieldOptional" }.type instanceof SwiftEntityStruct
    !entityA.properties.find { it.name == "nonSequenceField" }.type.optional
    !entityB.properties.find { it.name == "sequenceField" }.type.optional
    !entityA.properties.find { it.name == "sequenceOfSequence" }.type.optional
    entityA.properties.find { it.name == "nonSequenceFieldOptional" }.type.optional

  }


}
