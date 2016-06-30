package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntity
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityArray
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityEnum
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityEnumCase
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityPrimitive
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityStruct
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftProperty
import spock.lang.Specification

class SwiftDecodableMappingsFilesGeneratorImplTest extends Specification {

  List<SwiftFile> files
  SwiftFilesGenerator sut
  def setup() {
    sut = new SwiftDecodableMappingsFilesGeneratorImpl()
  }
  def "generate files from entities"() {
    when:
    files = sut.filesFromEntities([new SwiftEntityPrimitive("Cooler")] as List<SwiftEntity>)

    then:
    files.size() == 1
  }

  def "include import declaration"() {
    when:
    files = sut.filesFromEntities([new SwiftEntityPrimitive("Cooler")] as List<SwiftEntity>)

    then:
    files.every { it.contents().contains("import Decodable")}
  }


  def "generate files with entities mappings"() {
    when:
    files = sut.filesFromEntities([new SwiftEntityStruct("SomeStruct"), new SwiftEntityStruct("SomeAnotherStruct")])

    then:
    files.first().name() != ""
    files.first().contents().contains("extension SomeStruct: Decodable {")
    files.first().contents().contains("extension SomeAnotherStruct: Decodable {")
  }

  def "generate files with entities properties description"() {
    def property = new SwiftProperty("name", new SwiftEntityPrimitive("Good"), "original_name")
    def optionalProperty =
        new SwiftProperty("anotherName", new SwiftEntityPrimitive("Good").toOptional(), "another_original_name")

    when:
    files = sut.filesFromEntities([new SwiftEntityStruct("Entiry", [property, optionalProperty])])

    then:
    files.first().name() != ""
    files.first().contents().contains("public static func decode(json: AnyObject) throws -> Entiry {")
    files.first().contents().contains("return try Entiry(")
    files.first().contents().contains("name: json => \"original_name\"")
    files.first().contents().contains("anotherName: json =>? \"another_original_name\"")
  }

  def "generate files with mappings for enums"() {
    def enumEntity = new SwiftEntityEnum("nonCapitalizedName", [])
    def enumEntity2 = new SwiftEntityEnum("Capitalized_with_underlines", [])
    when:
    files = sut.filesFromEntities([enumEntity,enumEntity2])

    then:
    files.first().name() != ""
    files.first().contents().contains("extension nonCapitalizedName: Decodable {}")
    files.first().contents().contains("extension Capitalized_with_underlines: Decodable {}")
  }

  def "generate correct mappings for entities that have array fields"() {
    def enumEntity = new SwiftEntityEnum("WeekDays",
        [new SwiftEntityEnumCase("Wed","wed"),
         new SwiftEntityEnumCase("Fri", "fri")])
    def arrayEntity = new SwiftEntityArray("", enumEntity)
    def optionalArrayEntity = new SwiftEntityArray("", enumEntity.toOptional())
    def structEntity = new SwiftEntityStruct("Schedule", [
        new SwiftProperty("days", arrayEntity, "_days"),
        new SwiftProperty("optionalDays", arrayEntity.toOptional(), "_optional_days"),
        new SwiftProperty("daysWithOptionals", optionalArrayEntity, "_days_with_opts"),
        new SwiftProperty("optionalDaysWithOptionals", optionalArrayEntity.toOptional(), "_opt_days_with_opts")

    ])

    when:
    files = sut.filesFromEntities([arrayEntity,enumEntity,structEntity,optionalArrayEntity])

    then:
    files.first().name() != ""
    files.first().contents().contains("days: json => \"_days\"")
    files.first().contents().contains("optionalDays: json =>? \"_optional_days\"")
    files.first().contents().contains("daysWithOptionals: json => \"_days_with_opts\"")
    files.first().contents().contains("optionalDaysWithOptionals: json =>? \"_opt_days_with_opts\"")

  }



}
