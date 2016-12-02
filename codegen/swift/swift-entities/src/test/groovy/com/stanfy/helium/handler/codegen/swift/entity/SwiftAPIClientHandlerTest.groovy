package com.stanfy.helium.handler.codegen.swift.entity

import com.stanfy.helium.handler.codegen.swift.entity.client.SwiftAPIClientGenerator
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntitiesGenerator
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftOutputGenerator
import com.stanfy.helium.internal.dsl.ProjectDsl
import spock.lang.Specification

class SwiftAPIClientHandlerTest extends Specification {

  SwiftAPIClientHandler sut
  ProjectDsl project
  SwiftAPIClientGenerator apiGenerator
  SwiftOutputGenerator outputGenerator
  SwiftEntitiesGenerator entitiesGenerator
  SwiftGenerationOptions options

  File output

  def setup() {
    project = new ProjectDsl()
    output = File.createTempDir()
    apiGenerator = Mock(SwiftAPIClientGenerator)
    outputGenerator = Mock(SwiftOutputGenerator)
    entitiesGenerator = Mock(SwiftEntitiesGenerator)
    options = new SwiftGenerationOptions()
    sut = new SwiftAPIClientHandler(output, options , apiGenerator, entitiesGenerator, outputGenerator)
  }

  def "should generate files"() {
    when:
    sut.handle(project)

    then:
    1 * apiGenerator.clientFilesFromHeliumProject(project, _)
  }
}
