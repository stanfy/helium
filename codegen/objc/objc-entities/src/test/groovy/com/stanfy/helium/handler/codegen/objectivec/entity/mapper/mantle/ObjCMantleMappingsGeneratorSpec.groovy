package com.stanfy.helium.handler.codegen.objectivec.entity.mapper.mantle

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCDefaultProjectBuilder
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCPropertyNameTransformer
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCTypeTransformer
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.model.Type
import spock.lang.Specification
/**
 * Created by paultaykalo on 1/29/16.
 */
class ObjCMantleMappingsGeneratorSpec extends Specification {

  ObjCMantleMappingsGenerator sut
  ObjCEntitiesOptions options
  ProjectDsl projectDsl;
  ObjCDefaultProjectBuilder projectBuilder;


  def setup() {
    sut = new ObjCMantleMappingsGenerator()
    options = new ObjCEntitiesOptions()
    projectBuilder = new ObjCDefaultProjectBuilder(new ObjCTypeTransformer(), new ObjCPropertyNameTransformer())
    projectDsl = new ProjectDsl()
    projectDsl.typeResolver.registerNewType( new Type(name:"string"));

  }

  def "should update entities super class to MTLModel"() {
    given:
    projectDsl.type "A" message {
      name 'string'
    };
    def project = projectBuilder.build(projectDsl, options)

    when:

    sut.generate(project, projectDsl, options)

    then:
    project.classesTree.classes.first().definition.superClassName == "MTLModel"
  }


}
