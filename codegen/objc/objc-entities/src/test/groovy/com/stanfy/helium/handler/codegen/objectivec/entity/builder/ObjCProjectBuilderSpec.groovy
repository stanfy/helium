package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.typemapping.ObjCTypeMappingRegistry
import com.stanfy.helium.internal.dsl.ProjectDsl
import spock.lang.Specification

/**
 * Created by paultaykalo on 2/12/16.
 */
class ObjCProjectBuilderSpec extends Specification {

  ObjCProjectBuilder sut
  ObjCProjectBuildContext context

  def setup() {
    sut = new ObjCProjectBuilder()
    context = new ObjCProjectBuildContext(new ObjCTypeMappingRegistry(), new ObjCPropertyNameTransformer(), null)
  }

  def "should generate class for message"() {
    given:
    def DSL = new ProjectDsl()
    DSL.type "A" message {}

    when:
    def project = sut.build(DSL, context)

    then:
    project.classes.size() == 1
    project.classes.first().name == "A"

  }

  def "should not generate class for anonymous message"() {
    given:
    def DSL = new ProjectDsl()
    DSL.type "A" message {}
    DSL.type "B" message {}
    DSL.types.byName("A").anonymous = true

    when:
    def project = sut.build(DSL, context)

    then:
    project.classes.size() == 1
    project.classes.first().name == "B"
  }

  def "should use prefix for classes from options"() {
    given:
    def DSL = new ProjectDsl()
    DSL.type "A" message {}

    when:
    def options = new ObjCEntitiesOptions()
    options.prefix = "COOL"
    context = new ObjCProjectBuildContext(new ObjCTypeMappingRegistry(), new ObjCPropertyNameTransformer(), options)
    def project = sut.build(DSL, context)

    then:
    project.classes.size() == 1
    project.classes.first().name == "COOLA"
  }

  def "should register type mappings"() {
    given:
    def DSL = new ProjectDsl()
    DSL.type "A" message {}

    def options = new ObjCEntitiesOptions()
    options.prefix = "COOL"
    context = new ObjCProjectBuildContext(new ObjCTypeMappingRegistry(), new ObjCPropertyNameTransformer(), options)

    when:
    def project = sut.build(DSL, context)

    def type = context.typeMappingRegistry.objcType(DSL.getTypes().byName("A"))

    then:
    type.name == "COOLA"
    type.isReference
  }

}
