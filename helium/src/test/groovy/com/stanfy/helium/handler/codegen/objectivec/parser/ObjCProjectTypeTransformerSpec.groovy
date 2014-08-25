package com.stanfy.helium.handler.codegen.objectivec.parser

import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.handler.codegen.objectivec.ObjCHeaderFile
import com.stanfy.helium.handler.codegen.objectivec.ObjCImplementationFile
import com.stanfy.helium.handler.codegen.objectivec.ObjCProject
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClassDefinition
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClassImplementation
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCPropertyDefinition
import spock.lang.Specification

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCProjectTypeTransformerSpec extends Specification{

    ObjCTypeTransformer typeTransformer;

    def setup() {
        typeTransformer = new ObjCTypeTransformer();
    }

    def "should use NSString for string type"() {
        when:
        def objCType = typeTransformer.objCType("string")
        def accessorModifierForType = typeTransformer.accessorModifierForType("string")

        then:
        objCType == "NSString *";
        accessorModifierForType == ObjCPropertyDefinition.AccessModifier.COPY
    }

}
