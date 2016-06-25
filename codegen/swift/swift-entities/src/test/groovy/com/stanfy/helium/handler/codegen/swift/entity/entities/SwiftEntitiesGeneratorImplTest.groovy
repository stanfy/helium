package com.stanfy.helium.handler.codegen.swift.entity.entities

import com.stanfy.helium.handler.codegen.swift.entity.SwiftEntity
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.model.Type
import spock.lang.Specification

class SwiftEntitiesGeneratorImplTest extends Specification {
  ProjectDsl project
  List<SwiftEntity> entities
  SwiftEntitiesGenerator sut

  def setup() {
    project = new ProjectDsl()
    sut = new SwiftEntitiesGeneratorImpl()
  }

  def "should be able to generate swift entities"() {
    given:
    project.typeResolver.registerNewType(new Type(name: "string"));

    when:
    project.type "A" message {
      name 'string'
    };
    entities = sut.entitiesFromHeliumProject(project)

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
    entities = sut.entitiesFromHeliumProject(project)

    then:
    entities.get(0).name == "Name"
  }

}
