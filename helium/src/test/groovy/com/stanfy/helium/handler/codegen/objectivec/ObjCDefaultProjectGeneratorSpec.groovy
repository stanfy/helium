package com.stanfy.helium.handler.codegen.objectivec

import com.stanfy.helium.handler.codegen.java.constants.ConstantsGeneratorOptions
import com.stanfy.helium.handler.codegen.java.constants.JavaConstantsGenerator
import spock.lang.Specification

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCDefaultProjectGeneratorSpec extends ObjCProjectGeneratorSpec<ObjCProjectGenerator> {

    def setup() {
        generator = new ObjCProjectGenerator(output, project);
    }
}
