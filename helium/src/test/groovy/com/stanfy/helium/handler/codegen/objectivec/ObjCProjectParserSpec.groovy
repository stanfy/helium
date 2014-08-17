package com.stanfy.helium.handler.codegen.objectivec

import com.stanfy.helium.dsl.ProjectDsl
import spock.lang.Specification

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCProjectParserSpec extends Specification{

    ObjCProjectParser parser;
    ProjectDsl project;
    ObjCProject objCProject;

    def setup() {
        project = new ProjectDsl()
        project.type "A" message { }
        project.type "B" message { }
        project.type "C" message { }
    }

    def "should generate ObjCProject"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project);

        then:
        objCProject != null
    }

    def "should add ObjCFiles for each message"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project);

        // At least 6 files
        then:
        objCProject.getFiles() != null
        objCProject.getFiles().size() >= 6
    }

    def "should generate ObjCProject with .h and .m file for each message"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project);

        then:
        objCProject.getFiles() != null
        objCProject.getFiles().any({ file -> file.extension == "h" })
        objCProject.getFiles().any({ file -> file.extension == "m" })
    }

    def "should generate ObjCProject with files those have message name in their names"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project);

        then:
        objCProject.getFiles() != null
        objCProject.getFiles().any({ file -> file.name.toLowerCase().contains("A".toLowerCase()) })
        objCProject.getFiles().any({ file -> file.name.toLowerCase().contains("B".toLowerCase()) })
        objCProject.getFiles().any({ file -> file.name.toLowerCase().contains("C".toLowerCase()) })
    }

    def "should generate ObjCProject with ,m files which have correct class implementations"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project);

        then:
        objCProject.getFiles() != null
        objCProject.getFiles().any({ file -> file.name.toLowerCase().contains("A".toLowerCase()) })
        objCProject.getFiles().any({ file -> file.name.toLowerCase().contains("B".toLowerCase()) })
        objCProject.getFiles().any({ file -> file.name.toLowerCase().contains("C".toLowerCase()) })
    }

    def "should generate Classes each of those have definition and implementation"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project);

        then:
        objCProject.getClasses() != null
        objCProject.getClasses().every({ objCClass -> objCClass.getDefinition() != null && objCClass.getImplementation() != null });
    }

    def "should generate Classes each of those have definition and implementation with correct names"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project);

        then:
        objCProject.getClasses() != null
        objCProject.getClasses().every({ objCClass -> objCClass.getDefinition().getClassName() == objCClass.getName() && objCClass.getImplementation().getClassName() == objCClass.getName()});
    }



}
