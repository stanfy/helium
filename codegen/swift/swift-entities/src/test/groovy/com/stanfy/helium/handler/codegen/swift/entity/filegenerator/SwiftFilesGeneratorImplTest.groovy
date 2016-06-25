package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntity
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityEnum
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityEnumCase
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityPrimitive
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityStruct
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftProperty
import spock.lang.Specification

class SwiftFilesGeneratorImplTest extends Specification {

  List<SwiftFile> files
  SwiftFilesGenerator sut
  def setup() {
    sut = new SwiftFilesGeneratorImpl()
  }
  def "generate files from entities"() {
    when:
    files = sut.filesFromEntities([new SwiftEntityPrimitive("Cooler")] as List<SwiftEntity>)

    then:
    files.size() == 1
  }

  def "generate files with entities description"() {
    when:
    files = sut.filesFromEntities([new SwiftEntityStruct("Name1"), new SwiftEntityStruct("Name2")])

    then:
    files.first().name() != ""
    files.first().contents().contains("struct Name1 {")
    files.first().contents().contains("struct Name2 {")
  }

  def "generate files with entities properties description"() {
    def property = new SwiftProperty("name", new SwiftEntityPrimitive("Good"))
    def optionalPropery = new SwiftProperty("anotherName", new SwiftEntityPrimitive("Good").toOptional())
    when:
    files = sut.filesFromEntities([new SwiftEntityStruct("Entiry", [property, optionalPropery])])

    then:
    files.first().name() != ""
    files.first().contents().contains("struct Entiry {")
    files.first().contents().contains("let name: Good")
    files.first().contents().contains("let anotherName: Good?")
  }

  def "generate files with enums"() {
    def enumEntity = new SwiftEntityEnum("nonCapitalizedName",
        [new SwiftEntityEnumCase("Monday","monday",),
         new SwiftEntityEnumCase("Tuesday", "tuesday")])
    def enumEntity2 = new SwiftEntityEnum("Capitalized_with_underlines",
        [new SwiftEntityEnumCase("Wed","wed"),
         new SwiftEntityEnumCase("Fri", "fri")])
    when:
    files = sut.filesFromEntities([enumEntity,enumEntity2])

    then:
    files.first().name() != ""
    files.first().contents().contains("enum nonCapitalizedName: String {")
    files.first().contents().contains("enum Capitalized_with_underlines: String {")
    files.first().contents().contains('case Monday = "monday"')
    files.first().contents().contains('case Tuesday = "tuesday"')
    files.first().contents().contains('case Wed = "wed"')
    files.first().contents().contains('case Fri = "fri"')
  }


}
