package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntity
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityArray
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityEnum
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityEnumCase
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityPrimitive
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityStruct
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftProperty
import spock.lang.Specification

class SwiftEquatableFilesGeneratorImplTest extends Specification {

  List<SwiftFile> files
  SwiftFilesGenerator sut
  def setup() {
    sut = new SwiftEquatableFilesGeneratorImpl()
  }
  def "generate files from entities"() {
    when:
    files = sut.filesFromEntities([new SwiftEntityPrimitive("Cooler")] as List<SwiftEntity>)

    then:
    files.size() == 1
  }

  def "generate files with entities equatables"() {
    when:
    files = sut.filesFromEntities([new SwiftEntityStruct("SomeStruct"), new SwiftEntityStruct("SomeAnotherStruct")])

    then:
    files.first().name() != ""
    files.first().contents().contains("// MARK: - SomeStruct Equatable")
    files.first().contents().contains("// MARK: - SomeAnotherStruct Equatable")
    files.first().contents().contains("extension SomeStruct: Equatable {}")
    files.first().contents().contains("extension SomeAnotherStruct: Equatable {}")
    files.first().contents().contains("public func == (lhs: SomeStruct, rhs: SomeStruct) -> Bool {")
    files.first().contents().contains("public func == (lhs: SomeAnotherStruct, rhs: SomeAnotherStruct) -> Bool {")
  }

  def "generate files with entities == function for evert property"() {
    def property = new SwiftProperty("name", new SwiftEntityPrimitive("Good"), "original_name")
    def optionalProperty =
        new SwiftProperty("anotherName", new SwiftEntityPrimitive("Good").toOptional(), "another_original_name")

    when:
    files = sut.filesFromEntities([new SwiftEntityStruct("Entiry", [property, optionalProperty])])

    then:
    files.first().name() != ""
    files.first().contents().contains("return lhs.name == rhs.name")
    files.first().contents().contains("&& lhs.anotherName == rhs.anotherName")
  }

  def "generate files with enum equatables"() {
    def enumEntity = new SwiftEntityEnum("nonCapitalizedName", [])
    def enumEntity2 = new SwiftEntityEnum("Capitalized_with_underlines", [])
    when:
    files = sut.filesFromEntities([enumEntity,enumEntity2])

    then:
    files.first().name() != ""
    files.first().contents().contains("// MARK: - nonCapitalizedName Equatable")
    files.first().contents().contains("// MARK: - Capitalized_with_underlines Equatable")
    files.first().contents().contains("extension nonCapitalizedName: Equatable {}")
    files.first().contents().contains("extension Capitalized_with_underlines: Equatable {}")
    files.first().contents().contains("public func == (lhs: nonCapitalizedName, rhs: nonCapitalizedName) -> Bool {")
    files.first().contents().contains("public func == (lhs: Capitalized_with_underlines, rhs: Capitalized_with_underlines) -> Bool {")
    files.first().contents().contains("switch (lhs, rhs) {")
    files.first().contents().contains("default: return false")
  }

  def "generate correct equatables for enums"() {
    def enumEntity = new SwiftEntityEnum("WeekDays",
        [new SwiftEntityEnumCase("Wed","wed"),
         new SwiftEntityEnumCase("Fri", "fri")])

    when:
    files = sut.filesFromEntities([enumEntity])

    then:
    files.first().name() != ""
    files.first().contents().contains("case (.Wed, .Wed): return true")
    files.first().contents().contains("case (.Fri, .Fri): return true")
  }

}
