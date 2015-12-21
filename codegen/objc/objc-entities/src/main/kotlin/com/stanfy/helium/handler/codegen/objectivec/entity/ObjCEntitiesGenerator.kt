package com.stanfy.helium.handler.codegen.objectivec.entity;

import com.stanfy.helium.handler.Handler
import com.stanfy.helium.handler.codegen.BaseGenerator
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCDefaultFileStructureBuilder
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCDefaultProjectBuilder
import com.stanfy.helium.handler.codegen.objectivec.entity.httpclient.urlsession.ObjCHTTPClientGenerator
import com.stanfy.helium.handler.codegen.objectivec.entity.mapper.mantle.ObjCMantleMappingsGenerator
import com.stanfy.helium.handler.codegen.objectivec.entity.mapper.sfobjectmapping.ObjCSFObjectMappingsGenerator
import com.stanfy.helium.model.Project
import java.io.File

/**
 * Created by ptaykalo on 8/25/14.
 */
public class ObjCEntitiesGenerator(outputDirectory: File?, options: ObjCEntitiesOptions?) : BaseGenerator<ObjCEntitiesOptions>(outputDirectory, options), Handler {

  private val projectBuilder = ObjCDefaultProjectBuilder()
  private val mapper = ObjCSFObjectMappingsGenerator()
  private val mantleMapper = ObjCMantleMappingsGenerator()
  private val client = ObjCHTTPClientGenerator()

  override fun handle(project: Project?) {
    val objCProject = projectBuilder.build(project!!, options)
//    mapper.generate(objCProject, project, options)
    client.generate(objCProject, project, options)
    mantleMapper.generate(objCProject,project, options)
    val fileStructureBuilder = ObjCDefaultFileStructureBuilder()
    val filesStructure = fileStructureBuilder.build(objCProject.classStructure)
    ObjCProjectGenerator(outputDirectory, filesStructure).generate()
  }

}
