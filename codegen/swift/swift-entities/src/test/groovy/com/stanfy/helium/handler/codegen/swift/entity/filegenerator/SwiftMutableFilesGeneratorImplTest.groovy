package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntity
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityPrimitive
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityStruct
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftProperty
import spock.lang.Specification

class SwiftMutableFilesGeneratorImplTest extends Specification {

  List<SwiftFile> files
  SwiftFilesGenerator sut
  def setup() {
    sut = new SwiftMutableFilesGeneratorImpl()
  }
  def "generate files from entities"() {
    when:
    files = sut.filesFromEntities([new SwiftEntityPrimitive("Cooler")] as List<SwiftEntity>)

    then:
    files.size() == 1
  }

  def "generate files with entities updated"() {
    when:
    files = sut.filesFromEntities([new SwiftEntityStruct("SomeStruct"), new SwiftEntityStruct("SomeAnotherStruct")])

    then:
    files.first().name() != ""
    files.first().contents().contains("// MARK: - SomeStruct Mutable")
    files.first().contents().contains("// MARK: - SomeAnotherStruct Mutable")
    files.first().contents().contains("extension SomeStruct {")
    files.first().contents().contains("extension SomeAnotherStruct {")
  }

  def "generate files updated function"() {
    def property = new SwiftProperty("name", new SwiftEntityPrimitive("Good"), "original_name")
    def optionalProperty =
        new SwiftProperty("anotherName", new SwiftEntityPrimitive("Good").toOptional(), "another_original_name")

    when:
    files = sut.filesFromEntities([new SwiftEntityStruct("Entiry", [property, optionalProperty])])

    then:
    files.first().name() != ""
    files.first().contents().contains("func updated")
    files.first().contents().contains("func updated(")
    files.first().contents().contains("anotherName: Optional<Good?>")
    files.first().contents().contains("name name: Optional<Good>")
    files.first().contents().contains("anotherName: Optional<Good?> = nil")
    files.first().contents().contains("name name: Optional<Good> = nil")
    files.first().contents().contains(") -> Entiry {")

  }

  def "generate files with updated function wich return valid object"() {
    def property = new SwiftProperty("name", new SwiftEntityPrimitive("Good"), "original_name")
    def optionalProperty =
        new SwiftProperty("anotherName", new SwiftEntityPrimitive("Good").toOptional(), "another_original_name")

    when:
    files = sut.filesFromEntities([new SwiftEntityStruct("Entiry", [property, optionalProperty])])

    then:
    files.first().name() != ""
    files.first().contents().contains("return Entiry(")
    files.first().contents().contains("anotherName: anotherName ?? self.anotherName")
    files.first().contents().contains("name: name ?? self.name")
  }

}
