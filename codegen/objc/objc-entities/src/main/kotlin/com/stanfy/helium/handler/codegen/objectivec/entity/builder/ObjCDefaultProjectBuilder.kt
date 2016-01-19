package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProject
import com.stanfy.helium.model.Project

/**
 * Created by paultaykalo on 12/17/15.
 */
class ObjCDefaultProjectBuilder : ObjCProjectBuilder {

  private val typeTransformer: ObjCTypeTransformer
  private val nameTransformer: ObjCPropertyNameTransformer
  private val classStructureBuilder: ObjCDefaultClassStructureBuilder

  public constructor(typeTransformer: ObjCTypeTransformer, nameTransformer: ObjCPropertyNameTransformer) : super() {
    this.typeTransformer = typeTransformer;
    this.nameTransformer = nameTransformer;
    this.classStructureBuilder = ObjCDefaultClassStructureBuilder(this.typeTransformer, nameTransformer);
  }

  override fun build(from: Project): ObjCProject {
    return this.build(from, null)
  }

  override fun build(from: Project, options: ObjCEntitiesOptions?): ObjCProject {
    val project = from
    val classStructure = classStructureBuilder.build(project, options)
    return ObjCProject(classStructure)
  }

}