package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntity
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityEnum
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityEnumCase
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityPrimitive
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityStruct
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftProperty
import spock.lang.Specification

class SwiftRandomFilesGeneratorImplTest extends Specification {

  List<SwiftFile> files
  SwiftFilesGenerator sut
  def setup() {
    sut = new SwiftRandomEntitiesFilesGeneratorImpl()
  }
  def "generate files from entities"() {
    when:
    files = sut.filesFromEntities([new SwiftEntityPrimitive("Cooler")] as List<SwiftEntity>)

    then:
    files.size() == 1
  }

  def "generate files with entities those implements random protocol"() {
    when:
    files = sut.filesFromEntities([new SwiftEntityStruct("SomeStruct"), new SwiftEntityStruct("SomeAnotherStruct")])

    then:
    files.first().name() != ""
    files.first().contents().contains("// MARK: - SomeStruct Random")
    files.first().contents().contains("// MARK: - SomeAnotherStruct Random")
    files.first().contents().contains("extension SomeStruct: Random {")
    files.first().contents().contains("extension SomeAnotherStruct: Random {")
    files.first().contents().contains("static func random() -> SomeStruct")
    files.first().contents().contains("static func random() -> SomeAnotherStruct")
    files.first().contents().contains("static func restrictedRandom(")
  }

  def "generate files with entities those using restricted random call" () {
    when:
    files = sut.filesFromEntities([new SwiftEntityStruct("SomeStruct"), new SwiftEntityStruct("SomeAnotherStruct")])

    then:
    files.first().name() != ""
    files.first().contents().contains("static func random() -> SomeStruct {")
    files.first().contents().contains("return restrictedRandom()")
  }

  def "generate files with entities restricted random with all predefined values for properties"() {
    def property = new SwiftProperty("name", new SwiftEntityPrimitive("Good"), "original_name")
    def optionalProperty =
        new SwiftProperty("anotherName", new SwiftEntityPrimitive("Good").toOptional(), "another_original_name")

    when:
    files = sut.filesFromEntities([new SwiftEntityStruct("Entiry", [property, optionalProperty])])

    then:
    files.first().name() != ""
    files.first().contents().contains("restrictedRandom(name name: Good = .random()")
    files.first().contents().contains("anotherName: Good? = .random()")
    files.first().contents().contains("return Entiry(")
  }

  def "generate files with random implementations for enums"() {
    def enumEntity = new SwiftEntityEnum("WeekDays",
        [new SwiftEntityEnumCase("Wed","wed"),
         new SwiftEntityEnumCase("Fri", "fri")])

    when:
    files = sut.filesFromEntities([enumEntity])

    then:
    files.first().name() != ""
    files.first().contents().contains("static func random() -> WeekDays")
    files.first().contents().contains("switch (arc4random() % 2)")
    files.first().contents().contains("case 0: return .Wed")
    files.first().contents().contains("case 1: return .Fri")
    files.first().contents().contains("default: return .Wed")
  }

}
