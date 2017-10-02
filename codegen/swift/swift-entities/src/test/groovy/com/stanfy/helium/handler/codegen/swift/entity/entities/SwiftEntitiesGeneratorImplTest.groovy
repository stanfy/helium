package com.stanfy.helium.handler.codegen.swift.entity.entities

import com.stanfy.helium.handler.codegen.swift.entity.SwiftGenerationOptions
import com.stanfy.helium.internal.dsl.ProjectDsl
import spock.lang.Specification

class SwiftEntitiesGeneratorImplTest extends Specification {
  ProjectDsl project
  SwiftGenerationOptions generationOptions
  List<SwiftEntity> entities
  SwiftEntitiesGenerator sut

  def setup() {
    project = new ProjectDsl()
    generationOptions = new SwiftGenerationOptions()
    sut = new SwiftEntitiesGeneratorImpl()
  }

  def "generates entities"() {
    given:
    project.type "string"
    project.type "A" message {
      name 'string'
    };

    when:
    entities = sut.entitiesFromHeliumProject(project, generationOptions)

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
    entities = sut.entitiesFromHeliumProject(project, generationOptions)

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
    entities = sut.entitiesFromHeliumProject(project, generationOptions)

    then:
    entities.size() == 1
    entities.first().name == "Name"
  }

  def "generate enums entities for restricted types" () {
    List<SwiftEntityEnum> enums

    given:
    project.type "string"
    project.type "ResourceType" spec {
      constraints("string") {
        enumeration 'dining', 'product'
      }
    }
    project.type "nonCapitalizedType" spec {
      constraints("string") {
        enumeration 'dining', 'product'
      }
    }
    project.type "type_with_underscores" spec {
      constraints("string") {
        enumeration 'under_scores', '_un_der_sco_res'
      }
    }

    when:
    enums = sut.entitiesFromHeliumProject(project, generationOptions).findAll { it instanceof SwiftEntityEnum} as List<SwiftEntityEnum>

    then:
    enums.size() == 3
    enums.first().values.name == ["Dining", "Product"]
    enums.first().values.value == ["dining", "product"]
    enums.first().name == "ResourceType"
    enums.any { it.name == "NonCapitalizedType" }
    enums.any { it.name == "TypeWithUnderscores" }
    enums.last().values.name == ["UnderScores", "UnDerScoRes"]
    enums.last().values.value == ["under_scores", "_un_der_sco_res"]
  }

  def "generate associated types for named sequences" () {
    List<SwiftEntityArray> namedArrays

    given:
    project.type "string"
    project.type 'AffiliateMenu' sequence 'string'
    project.type 'AffiliateMenuList' sequence 'AffiliateMenu'

    when:
    namedArrays = sut.entitiesFromHeliumProject(project, generationOptions).findAll { it instanceof SwiftEntityArray} as List<SwiftEntityArray>

    then:
    namedArrays.size() == 2
    namedArrays.first().itemType.name == "String"
    namedArrays.first().name == "AffiliateMenu"
    !namedArrays.first().optional
    namedArrays.find { it.name == "AffiliateMenuList" }.itemType.name == "AffiliateMenu"
    !namedArrays.find { it.name == "AffiliateMenuList" }.optional
    !namedArrays.find { it.name == "AffiliateMenuList" }.itemType.optional
  }


  def "reuse entity names once found" () {
    SwiftEntityStruct entityA
    SwiftEntityEnum   enumEntity

    given:
    project.type "string"
    project.type "enum_type" spec {
      constraints("string") {
        enumeration 'dining', 'product'
      }
    }
    project.type "A" message {
      prop 'enum_type'
    }

    when:
    entities = sut.entitiesFromHeliumProject(project, generationOptions)
    entityA = entities.find { it.name == "A"} as SwiftEntityStruct
    enumEntity = entities.find { it instanceof SwiftEntityEnum} as SwiftEntityEnum

    then:
    entityA.properties.first().type == enumEntity
  }

  def "Use custom mappings if provided" () {
    given:
    project.type "string"
    project.type "customType"
    project.type "customMessage" message {
      prop 'customType'
    }
    generationOptions.customTypesMappings = ["customType" : "replacedType"]

    when:
    entities = sut.entitiesFromHeliumProject(project, generationOptions)

    then:
    !entities.flatten { it.properties }.any { it.name == "customType"}
    entities.flatten { it.properties }.any { it.type.name == "replacedType"}
  }

}
