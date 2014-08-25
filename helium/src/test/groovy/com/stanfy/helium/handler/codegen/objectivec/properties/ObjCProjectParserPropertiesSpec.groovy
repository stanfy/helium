package com.stanfy.helium.handler.codegen.objectivec.properties

import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.handler.codegen.objectivec.ObjCProject
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClass
import com.stanfy.helium.handler.codegen.objectivec.parser.ObjCProjectParser
import com.stanfy.helium.handler.codegen.objectivec.parser.options.DefaultObjCProjectParserOptions
import com.stanfy.helium.model.Type
import spock.lang.Specification

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCProjectParserPropertiesSpec extends Specification{

    ObjCProjectParser parser;
    ProjectDsl project;
    ObjCProject objCProject
    DefaultObjCProjectParserOptions options

    def setup() {
        project = new ProjectDsl()
        parser = new ObjCProjectParser()
        options = new DefaultObjCProjectParserOptions();
    }

    //        project.type "A" message { }

    def "should generate ObjCProject with class"() {
        given:
        project.typeResolver.registerNewType( new Type(name:"string"));

        when:
        project.type "A" message {
            name 'string'
        };
        objCProject = parser.parse(project, options);

        then:
        objCProject != null
        objCProject.getClasses().size() == 1
    }

    def "should generate ObjCProject with class and property"() {
        given:
        project.typeResolver.registerNewType( new Type(name:"string"));

        when:
        project.type "A" message {
            name 'string'
        };
        objCProject = parser.parse(project, options);
        ObjCClass aClass = objCProject.getClasses().get(0);

        then:
        aClass.definition != null
        aClass.definition.propertyDefinitions.size() == 1
    }



}
