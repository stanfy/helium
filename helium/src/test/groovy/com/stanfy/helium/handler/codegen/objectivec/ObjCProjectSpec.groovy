package com.stanfy.helium.handler.codegen.objectivec

import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClass
import com.stanfy.helium.model.Type
import spock.lang.Specification

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCProjectSpec extends Specification {

  ObjCProject project;

  def setup() {
    project = new ObjCProject();
  }

  def "should add files"() {
    when:
    project.addFile(new ObjCHeaderFile());

    then:
    project.getFiles().size() == 1

  }

  def "should add classes"() {
    when:
    project.addClass(new ObjCClass("Class"));

    then:
    project.getClasses().size() == 1
    project.getClasses().get(0).getName() == "Class"
  }

  def "should add classes for specific DSL Types"() {
    when:
    def addedClass = new ObjCClass("Class")

    def dslTypeName = "SomeDSLType"
    project.addClass(addedClass, dslTypeName);

    then:
    project.getClasses().size() == 1
    project.getClasses().get(0).getName() == "Class"
    project.getClassForType(dslTypeName) != null
    project.getClassForType(dslTypeName) == addedClass
  }



}
