package com.stanfy.helium.handler.codegen.swift.entity.entities

import com.stanfy.helium.handler.codegen.swift.entity.SwiftEntity
import com.stanfy.helium.model.Project

interface SwiftEntitiesGenerator {
  fun entitiesFromHeliumProject(project: Project): List<SwiftEntity>;
}

class SwiftEntitiesGeneratorImpl : SwiftEntitiesGenerator {
  override fun entitiesFromHeliumProject(project: Project): List<SwiftEntity> {
    return project.messages.map { message ->
      SwiftEntity(message.name)
    }
  }
}