package com.stanfy.helium.handler.codegen.objectivec

import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClassDefinition
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCPropertyDefinition
import spock.lang.Specification

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCClassDefinitionSpec extends Specification {

    ObjCProject project;
    ObjCHeaderFile headerFile
    private String fileName

    def setup() {
        project = new ObjCProject();
        fileName = "test"
        headerFile = new ObjCHeaderFile(fileName);

    }

    def "should add sourceParts"() {
        when:
        ObjCClassDefinition classDefinition = new ObjCClassDefinition(fileName);
        ObjCPropertyDefinition propertyDefinition = new ObjCPropertyDefinition("name", "type");
        classDefinition.addPropertyDefinition(propertyDefinition)

        then:
        classDefinition.getPropertyDefinitions().size() == 1
    }




}
