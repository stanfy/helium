package com.stanfy.helium.handler.codegen.objectivec.parser

import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.handler.codegen.objectivec.ObjCHeaderFile
import com.stanfy.helium.handler.codegen.objectivec.ObjCImplementationFile
import com.stanfy.helium.handler.codegen.objectivec.ObjCProject
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClassDefinition
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClassImplementation
import com.stanfy.helium.handler.codegen.objectivec.parser.ObjCProjectParser
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

    def "should generate ,m files wich should contain implementation part"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project);
        def implementationFiles = objCProject.getFiles().findResults({ file -> return file instanceof ObjCImplementationFile ? file : null })
        def definitionFiles = objCProject.getFiles().findResults({ file -> return file instanceof ObjCHeaderFile ? file : null })
        then:
        implementationFiles.every ({ file -> file.getSourceParts().any{ sourcePart -> sourcePart instanceof ObjCClassImplementation}})
        definitionFiles.every ({ file -> file.getSourceParts().any{ sourcePart -> sourcePart instanceof ObjCClassDefinition}})
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

    def "should generate register types for all messages in the project"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project);

        then:
        parser.getTypeTransformer().objCType(project.getTypes().byName("A")) != null
        parser.getTypeTransformer().objCType(project.getTypes().byName("B")) != null
        parser.getTypeTransformer().objCType(project.getTypes().byName("C")) != null
    }


}
