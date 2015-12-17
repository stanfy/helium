package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProject
import com.stanfy.helium.model.Project

/**
 * Created by paultaykalo on 12/17/15.
 */
class ObjCDefaultProjectBuilder : ObjCProjectBuilder {

  val classStructureBuilder = ObjCDefaultClassStructureBuilder()
  val fileStructureBuilder = ObjCDefaultFileStructureBuilder()

  override fun build(from: Project): ObjCProject {
    return this.build(from, null)
  }

  override fun build(from: Project, options: ObjCEntitiesOptions?): ObjCProject {
    val project = from
    val classStructure = classStructureBuilder.build(project, options)
    var fileStructure  =fileStructureBuilder.build(classStructure, options)
    return ObjCProject(classStructure, fileStructure)
  }

}