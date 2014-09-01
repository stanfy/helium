package com.stanfy.helium.handler.codegen.objectivec.parser

import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.handler.codegen.objectivec.ObjCHeaderFile
import com.stanfy.helium.handler.codegen.objectivec.ObjCImplementationFile
import com.stanfy.helium.handler.codegen.objectivec.ObjCProject
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClassDefinition
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClassImplementation
import com.stanfy.helium.handler.codegen.objectivec.parser.ObjCProjectParser
import com.stanfy.helium.handler.codegen.objectivec.parser.options.DefaultObjCProjectParserOptions
import com.stanfy.helium.handler.codegen.objectivec.parser.options.ObjCProjectParserOptions
import com.stanfy.helium.model.Type
import spock.lang.Specification

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCProjectParserWithOptionsSpec extends Specification{

    ObjCProjectParser parser;
    ProjectDsl project;
    ObjCProject objCProject;
    ObjCProjectParserOptions parseOptions;

    def setup() {
        project = new ProjectDsl()
        project.type "A" message { }
        project.type "B" message { }
        project.type "C" message {
            a "A"
            b "B" sequence
        }

        parseOptions = new DefaultObjCProjectParserOptions();
    }

    def "should generate ObjCProject with options"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project, parseOptions);

        then:
        objCProject != null
    }

    def "should add ObjCFiles for each message"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project, parseOptions);

        // At least 6 files
        then:
        objCProject.getFiles() != null
        objCProject.getFiles().size() >= 6
    }

    def "should generate ObjCProject with .h and .m file for each message"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project, parseOptions);

        then:
        objCProject.getFiles() != null
        objCProject.getFiles().any({ file -> file.extension == "h" })
        objCProject.getFiles().any({ file -> file.extension == "m" })
    }

    def "should generate ObjCProject with files those have message name in their names and prefix from options"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project, parseOptions);

        then:
        objCProject.getFiles() != null
        objCProject.getFiles().any({ file -> file.name.toLowerCase().contains("A".toLowerCase()) && file.name.startsWith(parseOptions.getPrefix())})
        objCProject.getFiles().any({ file -> file.name.toLowerCase().contains("B".toLowerCase()) && file.name.startsWith(parseOptions.getPrefix())})
        objCProject.getFiles().any({ file -> file.name.toLowerCase().contains("C".toLowerCase()) && file.name.startsWith(parseOptions.getPrefix())})
    }

    def "should generate ,m files wich should contain implementation part"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project, parseOptions);
        def implementationFiles = objCProject.getFiles().findResults({ file -> return file instanceof ObjCImplementationFile ? file : null })
        def definitionFiles = objCProject.getFiles().findResults({ file -> return file instanceof ObjCHeaderFile ? file : null })
        then:
        implementationFiles.every ({ file -> file.getSourceParts().any{ sourcePart -> sourcePart instanceof ObjCClassImplementation}})
        definitionFiles.every ({ file -> file.getSourceParts().any{ sourcePart -> sourcePart instanceof ObjCClassDefinition}})
    }

    def "should generate Classes each of those have definition and implementation"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project, parseOptions);

        then:
        objCProject.getClasses() != null
        objCProject.getClasses().every({ objCClass -> objCClass.getDefinition() != null && objCClass.getImplementation() != null });
    }

    def "should generate Classes each of those have definition and implementation with correct names"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project, parseOptions);

        then:
        objCProject.getClasses() != null
        objCProject.getClasses().every({ objCClass -> objCClass.getDefinition().getClassName() == objCClass.getName() && objCClass.getImplementation().getClassName() == objCClass.getName()});
    }

    def "should generate register types for all messages in the project"(String message) {
        given:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project, parseOptions);

        expect:
        parser.getTypeTransformer().objCType(project.getTypes().byName(message)) == parseOptions.prefix + message + " *";

        where:
        message << ["A", "B", "C"]
    }

    def "should fill external classes declarations"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project, parseOptions);

        def CClass = objCProject.getClasses().find {it.getDefinition().getClassName().contains("C") }

        then:
        !CClass.getDefinition().getExternalClassDeclaration().contains(parseOptions.prefix + "B") // Sequence
        CClass.getDefinition().getExternalClassDeclaration().contains(parseOptions.prefix + "A")
        !CClass.getDefinition().getExternalClassDeclaration().contains(parseOptions.prefix + "C")

    }

    def "should not generate properties with names of reserved keywords"() {
        when:
        project = new ProjectDsl()
        project.typeResolver.registerNewType( new Type(name:"int32"));

        project.type "A" message {
            copy "int32"
            copyField "int32"
            copyField1 "int32"
        }

        parser = new ObjCProjectParser()
        objCProject = parser.parse(project, parseOptions);

        def propertyNames = objCProject.getClasses().get(0).getDefinition().getPropertyDefinitions().collect {p -> p.getName()}

        then:
        propertyNames.size() == 3
        propertyNames.toSet().size() == 3

    }


}
