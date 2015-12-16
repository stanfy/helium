package com.stanfy.helium.handler.codegen.objectivec.entity

import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCClassInterface
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCPropertyDefinition
import spock.lang.Specification

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCClassDefinitionSpec extends Specification {

  ObjCProject project;
  ObjCHeaderFile headerFile
  private String fileName

  def setup() {
    project = new ObjCProject();
    fileName = "test"
    headerFile = new ObjCHeaderFile(fileName);

  }

  def "should add sourceParts"() {
    when:
    ObjCClassInterface classDefinition = new ObjCClassInterface(fileName);
    ObjCPropertyDefinition propertyDefinition = new ObjCPropertyDefinition("name", "type");
    classDefinition.addPropertyDefinition(propertyDefinition)

    then:
    classDefinition.getPropertyDefinitions().size() == 1
  }

  def "should generate contents of properties sourceParts when repersented as string"() {
    when:
    ObjCClassInterface classDefinition = new ObjCClassInterface(fileName);
    ObjCPropertyDefinition propertyDefinition = new ObjCPropertyDefinition("name", "type");
    classDefinition.addPropertyDefinition(propertyDefinition)

    then:
    classDefinition.asString().contains(propertyDefinition.asString());
  }



}
