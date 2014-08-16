package com.stanfy.helium.handler.codegen.objectivec

import spock.lang.Specification

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCProjectSpec extends Specification{

    ObjCProject project;

    def setup() {
        project = new ObjCProject();
    }
    def "should add files"() {
        when:
        project.addFile(new ObjCHeaderFile());

        then:
        project.getFiles().size() == 1

    }

}
