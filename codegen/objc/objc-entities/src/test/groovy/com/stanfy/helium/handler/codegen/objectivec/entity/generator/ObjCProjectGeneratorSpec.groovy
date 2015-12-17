package com.stanfy.helium.handler.codegen.objectivec.entity.generator

import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCHeaderFile
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCImplementationFile
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProject
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProjectGenerator
import spock.lang.Specification

/**
 * Created by ptaykalo on 8/17/14.
 */
// TODO: implement or remove
abstract class ObjCProjectGeneratorSpec<T extends ObjCProjectGenerator> extends Specification {
  ObjCProject project
  File output
  T generator

  def setup() {
    project = new ObjCProject()
    project.fileStructure.addFile(new ObjCHeaderFile("A"));
    project.fileStructure.addFile(new ObjCHeaderFile("B"));
    project.fileStructure.addFile(new ObjCImplementationFile("A"));
    project.fileStructure.addFile(new ObjCImplementationFile("B"));

    output = File.createTempDir()
    println output
  }

  def "should generate files"() {
    when:
    generator.generate()

    then:
    new File("$output/A.h").exists()
    new File("$output/B.h").exists()
    new File("$output/A.m").exists()
    new File("$output/B.m").exists()
  }

}
