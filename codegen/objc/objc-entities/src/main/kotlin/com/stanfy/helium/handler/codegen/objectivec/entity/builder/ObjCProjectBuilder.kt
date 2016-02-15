package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.model.ObjCClass
import com.stanfy.helium.handler.codegen.objectivec.entity.model.ObjCProject
import com.stanfy.helium.handler.codegen.objectivec.entity.model.ObjCType
import com.stanfy.helium.model.Project

/**
 * Created by paultaykalo on 2/12/16.
 *
 */
public class ObjCProjectBuilder {

  fun build(from: Project, context: ObjCProjectBuildContext): ObjCProject {
    val project = ObjCProject()
    val filteredMessages =
        from.messages.filter { message ->
          !message.anonymous
        }
    project.classes.addAll(filteredMessages.map { message ->
      val className = (context.options?.prefix ?: "") + message.name
      context.typeMappingRegistry.registerMapping(message, ObjCType(className, isReference = true))
      ObjCClass(className)
    })
    return project
  }

}