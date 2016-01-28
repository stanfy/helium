package com.stanfy.helium.handler.codegen.objectivec.entity

import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCProjectClassesTree
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCClass
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCClassInterface
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCHeaderFile
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCClassImplementation
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCProjectFilesStructure
import spock.lang.Specification

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCProjectSpec extends Specification {

  ObjCProjectFilesStructure filesStructure;
  ObjCProjectClassesTree classStructure;

  def setup() {
    filesStructure = new ObjCProjectFilesStructure()
    classStructure = new ObjCProjectClassesTree()
  }

  def "should add files"() {
    when:
    filesStructure.addFile(new ObjCHeaderFile("header", ""));

    then:
    filesStructure.files.size() == 1
  }

  def "should add classes"() {
    when:
    classStructure.addClass(new ObjCClass("Class", new ObjCClassInterface(""), new ObjCClassImplementation("")));

    then:
    classStructure.getClasses().size() == 1
    classStructure.getClasses().get(0).getName() == "Class"
  }

  def "should add classes for specific DSL Types"() {
    when:
    def addedClass = new ObjCClass("Class", new ObjCClassInterface(""), new ObjCClassImplementation(""))

    def dslTypeName = "SomeDSLType"
    classStructure.addClass(addedClass, dslTypeName);

    then:
    classStructure.getClasses().size() == 1
    classStructure.getClasses().get(0).getName() == "Class"
    classStructure.getClassForType(dslTypeName) != null
    classStructure.getClassForType(dslTypeName) == addedClass
  }



}
