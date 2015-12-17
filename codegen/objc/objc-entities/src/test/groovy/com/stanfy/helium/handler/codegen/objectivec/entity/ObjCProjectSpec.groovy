package com.stanfy.helium.handler.codegen.objectivec.entity

import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCProjectClassStructure
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCClass
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCClassInterface
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCHeaderFile
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCClassImplementation
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCProjectFileStructure
import spock.lang.Specification

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCProjectSpec extends Specification {

  ObjCProject project;
  ObjCProjectFileStructure projectFileStructure;
  ObjCProjectClassStructure projectClassStructure;

  def setup() {
    project = new ObjCProject();
    projectFileStructure = project.fileStructure
    projectClassStructure = project.classStructure
  }

  def "should add files"() {
    when:
    projectFileStructure.addFile(new ObjCHeaderFile("header", ""));

    then:
    projectFileStructure.files.size() == 1

  }

  def "should add classes"() {
    when:
    projectClassStructure.addClass(new ObjCClass("Class", new ObjCClassInterface(""), new ObjCClassImplementation("")));

    then:
    projectClassStructure.getClasses().size() == 1
    projectClassStructure.getClasses().get(0).getName() == "Class"
  }

  def "should add classes for specific DSL Types"() {
    when:
    def addedClass = new ObjCClass("Class", new ObjCClassInterface(""), new ObjCClassImplementation(""))

    def dslTypeName = "SomeDSLType"
    projectClassStructure.addClass(addedClass, dslTypeName);

    then:
    projectClassStructure.getClasses().size() == 1
    projectClassStructure.getClasses().get(0).getName() == "Class"
    projectClassStructure.getClassForType(dslTypeName) != null
    projectClassStructure.getClassForType(dslTypeName) == addedClass
  }



}
