package com.stanfy.helium.handler.codegen.swift.entity.entities

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

  def "generates entities"() {
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

  def "generates entities with valid names"() {
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

  def "skip entities for anonymous messages"() {
    given:
    project.typeResolver.registerNewType(new Type(name: "string"));

    when:
    project.type "Name" message {
      name 'string'
    };
    project.service {
      get "/person/@id" spec {
        parameters {
          full 'string' required
        }
        response "string"
      }
    }

    entities = sut.entitiesFromHeliumProject(project)

    then:
    entities.size() == 1
  }

}
