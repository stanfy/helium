package com.stanfy.helium.handler.codegen.swift.entity

import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFile
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.model.Type
import org.apache.commons.io.FileUtils
import spock.lang.Specification

/**
 * Created by paultaykalo on 6/25/16.
 */
class SwiftEntitiesGeneratorTest extends Specification {
  SwiftEntitiesGenerator sut;
  ProjectDsl project;
  SwiftGenerationOptions options;
  List<SwiftEntity> entities
  List<SwiftFile> files
  File output;

  def setup() {
    project = new ProjectDsl()
    options = new SwiftGenerationOptions();
    output = File.createTempDir()
    sut = new SwiftEntitiesGenerator(output, options)
  }

  def "should be able to generate swift entities"() {
    given:
    project.typeResolver.registerNewType(new Type(name: "string"));

    when:
    project.type "A" message {
      name 'string'
    };
    entities = sut.entities(project)

    then:
    entities != null
    entities.size() == 1
  }

  def "should be able to generate entities with valid names"() {
    given:
    project.typeResolver.registerNewType(new Type(name: "string"));

    when:
    project.type "Name" message {
      name 'string'
    };
    entities = sut.entities(project)

    then:
    entities.get(0).name == "Name"
  }

  def "should be able to files from entities"() {
    when:
    files = sut.filesFromEntities([new SwiftEntity("Cooler")] as List<SwiftEntity>)

    then:
    files.size() == 1
  }

  def "should be able to files with entities description"() {
    when:
    files = sut.filesFromEntities([new SwiftEntity("Name1"), new SwiftEntity("Name2")])

    then:
    files.first().name() != ""
    files.first().contents().contains("struct Name1 {")
    files.first().contents().contains("struct Name2 {")
  }

  def "should bacutally generate swift files"() {
    given:
    project.typeResolver.registerNewType(new Type(name: "string"));

    when:
    project.type "Name" message {
      name 'string'
    };
    sut.handle(project)

    then:
    new File("$output/Entities.swift").exists()
    FileUtils.readFileToString(new File("$output/Entities.swift")).contains("struct Name {")
  }

}
