package com.stanfy.helium.handler.codegen.objectivec.entity.generator

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCHeaderFile
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCImplementationFile
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
    project.addFile(new ObjCHeaderFile("A"));
    project.addFile(new ObjCHeaderFile("B"));
    project.addFile(new ObjCImplementationFile("A"));
    project.addFile(new ObjCImplementationFile("B"));

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
