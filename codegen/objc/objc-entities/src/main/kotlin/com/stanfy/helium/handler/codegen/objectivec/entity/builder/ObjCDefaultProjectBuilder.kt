package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProjectComplex
import com.stanfy.helium.model.Project

/**
 * Created by paultaykalo on 12/17/15.
 */
@Deprecated("Use newer version of it which is @see ObjCProjectBuilder")
class ObjCDefaultProjectBuilder(val typeTransformer: ObjCTypeTransformer,
                                val nameTransformer: ObjCPropertyNameTransformer) : ObjCBuilder<Project, ObjCProjectComplex> {

  override fun build(from: Project, options: ObjCEntitiesOptions?): ObjCProjectComplex {
    val project = from
    val classStructure = ObjCDefaultClassStructureBuilder(typeTransformer, nameTransformer).build(project, options)
    return ObjCProjectComplex(classStructure)
  }

}