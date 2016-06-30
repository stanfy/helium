package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

import com.stanfy.helium.handler.codegen.swift.entity.SwiftEntitiesAccessLevel
import com.stanfy.helium.handler.codegen.swift.entity.SwiftGenerationOptions
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntity
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityArray
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
    sut = new SwiftEntityFilesGeneratorImpl()
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

  def "generate files with named arrays"() {
    def enumEntity2 = new SwiftEntityEnum("Capitalized_with_underlines",
        [new SwiftEntityEnumCase("Wed","wed"),
         new SwiftEntityEnumCase("Fri", "fri")])
    def arrayEntity = new SwiftEntityArray("arrayEntity", enumEntity2)
    def structEntity = new SwiftEntityStruct("SomeStruct", [
        new SwiftProperty("array", arrayEntity)
    ])

    when:
    files = sut.filesFromEntities([arrayEntity,enumEntity2,structEntity])

    then:
    files.first().name() != ""
    files.first().contents().contains("enum Capitalized_with_underlines: String {")
    files.first().contents().contains("typealias arrayEntity = [Capitalized_with_underlines]")
    files.first().contents().contains("struct SomeStruct {")
    files.first().contents().contains("let array: arrayEntity")
  }

  def "generate files with entities that have array fields"() {
    def enumEntity = new SwiftEntityEnum("WeekDays",
        [new SwiftEntityEnumCase("Wed","wed"),
         new SwiftEntityEnumCase("Fri", "fri")])
    def arrayEntity = new SwiftEntityArray("", enumEntity)
    def optionalArrayEntity = new SwiftEntityArray("", enumEntity.toOptional())
    def structEntity = new SwiftEntityStruct("Schedule", [
        new SwiftProperty("days", arrayEntity),
        new SwiftProperty("optionalDays", arrayEntity.toOptional()),
        new SwiftProperty("daysWithOptionals", optionalArrayEntity),
        new SwiftProperty("optionalDaysWithOptionals", optionalArrayEntity.toOptional())

    ])

    when:
    files = sut.filesFromEntities([arrayEntity,enumEntity,structEntity,optionalArrayEntity])

    then:
    files.first().name() != ""
    files.first().contents().contains("enum WeekDays: String {")
    files.first().contents().contains("struct Schedule {")
    files.first().contents().contains("let days: [WeekDays]")
    files.first().contents().contains("let optionalDays: [WeekDays]?")
    files.first().contents().contains("let daysWithOptionals: [WeekDays?]")
    files.first().contents().contains("let optionalDaysWithOptionals: [WeekDays?]?")
  }

  def "generate files with entities with different access control"() {
    given:
    SwiftGenerationOptions options = new SwiftGenerationOptions()
    options.entitiesAccessLevel = accessLevel
    SwiftEntityEnum enumEntity = new SwiftEntityEnum("nonCapitalizedName", [])
    SwiftEntityEnum enumEntity2 = new SwiftEntityEnum("Capitalized_with_underlines", [])
    SwiftEntityStruct structEntity = new SwiftEntityStruct("Name1")
    SwiftEntityStruct structEntity2 = new SwiftEntityStruct("Name2")
    SwiftEntityArray arrayEntity = new SwiftEntityArray("arrayEntity", enumEntity2)
    files = sut.filesFromEntities([enumEntity, enumEntity2, structEntity, structEntity2, arrayEntity], options)

    expect:
    files.first().name() != ""
    files.first().contents().contains(accessLevelString + " struct Name1 {")
    files.first().contents().contains(accessLevelString + " struct Name2 {")
    files.first().contents().contains(accessLevelString + " enum nonCapitalizedName")
    files.first().contents().contains(accessLevelString + " enum Capitalized_with_underlines")
    files.first().contents().contains(accessLevelString + " typealias arrayEntity")

    where:
    accessLevelString | accessLevel
    "public"          | SwiftEntitiesAccessLevel.PUBLIC
    ""                | SwiftEntitiesAccessLevel.INTERNAL

  }

}
