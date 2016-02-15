package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProject
import com.stanfy.helium.model.Project

/**
 * Created by paultaykalo on 12/17/15.
 */
class ObjCDefaultProjectBuilder(val typeTransformer: ObjCTypeTransformer,
                                val nameTransformer: ObjCPropertyNameTransformer) : ObjCBuilder<Project, ObjCProject> {

  override fun build(from: Project, options: ObjCEntitiesOptions?): ObjCProject {
    val project = from
    val classStructure = ObjCDefaultClassStructureBuilder(typeTransformer, nameTransformer).build(project, options)
    return ObjCProject(classStructure)
  }

}