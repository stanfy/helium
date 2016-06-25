package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntity
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
    files = sut.filesFromEntities([new SwiftEntityStruct("Name1", []), new SwiftEntityStruct("Name2", [])])

    then:
    files.first().name() != ""
    files.first().contents().contains("struct Name1 {")
    files.first().contents().contains("struct Name2 {")
  }

  def "generate files with entities properties description"() {
    def property = new SwiftProperty("name", new SwiftEntityPrimitive("Good"))
    when:
    files = sut.filesFromEntities([new SwiftEntityStruct("Entiry", [property])])

    then:
    files.first().name() != ""
    files.first().contents().contains("struct Entiry {")
    files.first().contents().contains("let name: Good")
  }


}
