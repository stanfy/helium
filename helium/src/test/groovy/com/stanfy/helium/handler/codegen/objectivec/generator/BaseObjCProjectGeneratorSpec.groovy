package com.stanfy.helium.handler.codegen.objectivec.generator

import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.handler.codegen.objectivec.ObjCHeaderFile
import com.stanfy.helium.handler.codegen.objectivec.ObjCImplementationFile
import com.stanfy.helium.handler.codegen.objectivec.ObjCProject
import com.stanfy.helium.handler.codegen.objectivec.ObjCProjectGenerator
import spock.lang.Specification

/**
 * Created by ptaykalo on 8/17/14.
 */
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
