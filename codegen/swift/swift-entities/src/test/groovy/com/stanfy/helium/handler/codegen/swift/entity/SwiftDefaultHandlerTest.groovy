package com.stanfy.helium.handler.codegen.swift.entity

import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntitiesGenerator
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntity
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityStruct
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFile
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFilesGenerator
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftOutputGenerator
import com.stanfy.helium.internal.dsl.ProjectDsl
import spock.lang.Specification

class SwiftDefaultHandlerTest extends Specification {
  SwiftDefaultHandler sut
  ProjectDsl project
  SwiftGenerationOptions options
  File output
  SwiftFilesGenerator filesGenerator
  SwiftFilesGenerator filesGenerator2
  SwiftEntitiesGenerator entitiesGenerator
  SwiftOutputGenerator outputGenerator

  def setup() {
    project = new ProjectDsl()
    options = new SwiftGenerationOptions()
    filesGenerator = Mock(SwiftFilesGenerator)
    filesGenerator2 = Mock(SwiftFilesGenerator)
    entitiesGenerator = Mock(SwiftEntitiesGenerator)
    outputGenerator = Mock(SwiftOutputGenerator)
    output = File.createTempDir()
    sut = new SwiftDefaultHandler(output, options, entitiesGenerator, [filesGenerator] as SwiftFilesGenerator[], outputGenerator)
  }

  def "should generate entities"() {
    when:
    sut.handle(project)

    then:
    1 * entitiesGenerator.entitiesFromHeliumProject(project, options)
  }

  def "should generate files from generated entities"() {
    List<SwiftEntity> entities

    given:
    entities = [new SwiftEntityStruct("Name", [], false)]
    entitiesGenerator.entitiesFromHeliumProject(project, options) >> entities

    when:
    sut.handle(project)

    then:
    1 * filesGenerator.filesFromEntities(entities, options)
  }

  def "should generate files with all from generated entities"() {
    List<SwiftEntity> entities

    given:
    entities = [new SwiftEntityStruct("Name", [], false)]
    entitiesGenerator.entitiesFromHeliumProject(project, options) >> entities

    when:
    sut = new SwiftDefaultHandler(output, options, entitiesGenerator, [filesGenerator, filesGenerator2] as SwiftFilesGenerator[], outputGenerator)

    sut.handle(project)

    then:
    1 * filesGenerator.filesFromEntities(entities, options)
    1 * filesGenerator2.filesFromEntities(entities, options)
  }


  def "should output files for return swift files"() {
    List<SwiftEntity> entities
    List<SwiftFile> files

    given:
    entities = [new SwiftEntityStruct("Name", [], false)]
    files = [new SwiftFile() {
      String name() { return "FileName" }

      String contents() { return "Contents" }
    }]
    entitiesGenerator.entitiesFromHeliumProject(project, options) >> entities
    filesGenerator.filesFromEntities(entities, options) >> files

    when:
    sut.handle(project)

    then:
    1 * outputGenerator.generate(_, files)
  }


}
