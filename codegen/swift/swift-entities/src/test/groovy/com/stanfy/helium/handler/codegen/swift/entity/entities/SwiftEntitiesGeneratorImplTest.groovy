package com.stanfy.helium.handler.codegen.swift.entity.entities

import com.stanfy.helium.internal.dsl.ProjectDsl
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
    project.type "string"
    project.type "A" message {
      name 'string'
    };

    when:
    entities = sut.entitiesFromHeliumProject(project)

    then:
    entities != null
    entities.size() == 1
  }

  def "generates entities with valid names"() {
    given:
    project.type "string"
    project.type "Name" message {
      name 'string'
    };

    when:
    entities = sut.entitiesFromHeliumProject(project)

    then:
    entities.get(0).name == "Name"
  }

  def "skip entities for anonymous messages"() {
    given:
    project.type "string"
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

    when:
    entities = sut.entitiesFromHeliumProject(project)

    then:
    entities.size() == 1
  }

  def "generate entity enums for restricted types" () {
    List<SwiftEntityEnum> enums

    given:
    project.type "string"
    project.type "ResourceType" spec {
      constraints("string") {
        enumeration 'dining', 'product'
      }
    }

    when:
    enums = sut.entitiesFromHeliumProject(project).findAll { it instanceof SwiftEntityEnum} as List<SwiftEntityEnum>

    then:
    enums.size() == 1
  }

  def "generate entity enums for restricted types " () {
    List<SwiftEntityEnum> enums

    given:
    project.type "string"
    project.type "ResourceType" spec {
      constraints("string") {
        enumeration 'dining', 'product'
      }
    }

    when:
    enums = sut.entitiesFromHeliumProject(project).findAll { it instanceof SwiftEntityEnum} as List<SwiftEntityEnum>

    then:
    enums.size() == 1
  }


}
