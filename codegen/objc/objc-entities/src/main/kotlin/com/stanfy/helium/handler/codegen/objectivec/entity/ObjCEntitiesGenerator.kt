package com.stanfy.helium.handler.codegen.objectivec.entity;

import com.stanfy.helium.handler.Handler
import com.stanfy.helium.handler.codegen.BaseGenerator
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.DefaultObjCProjectBuilder
import com.stanfy.helium.handler.codegen.objectivec.entity.mapper.sfobjectmapping.ObjCSFObjectMapper
import com.stanfy.helium.model.Project
import java.io.File

/**
 * Created by ptaykalo on 8/25/14.
 */
public class ObjCEntitiesGenerator(outputDirectory: File?, options: ObjCEntitiesOptions?) : BaseGenerator<ObjCEntitiesOptions>(outputDirectory, options), Handler {

  private val projectBuilder = DefaultObjCProjectBuilder()
  private val mapper = ObjCSFObjectMapper()

  override fun handle(project: Project?) {
    val objCProject = projectBuilder.build(project!!, options)
    mapper.generateMappings(objCProject, project, options)
    ObjCProjectGenerator(outputDirectory, objCProject.fileStructure).generate()
  }

}
