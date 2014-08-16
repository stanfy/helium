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

    def "should generate return ObjCProject"() {
        when:
        parser = new ObjCProjectParser()
        objCProject = parser.parse(project);

        then:
        objCProject != null
    }


}
