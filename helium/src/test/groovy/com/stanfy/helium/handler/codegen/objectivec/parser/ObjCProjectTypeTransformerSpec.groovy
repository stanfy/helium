package com.stanfy.helium.handler.codegen.objectivec.parser

import com.stanfy.helium.handler.codegen.objectivec.file.ObjCPropertyDefinition
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type
import spock.lang.Specification
import com.stanfy.helium.model.Sequence;

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCProjectTypeTransformerSpec extends Specification{

    ObjCTypeTransformer typeTransformer;

    def setup() {
        typeTransformer = new ObjCTypeTransformer();
    }

    def "should use NSString for string type"() {
        def string = new Type(name: "string")
        when:
        def objCType = typeTransformer.objCType(string)
        def accessorModifierForType = typeTransformer.accessorModifierForType(string)

        then:
        objCType == "NSString *";
        accessorModifierForType == ObjCPropertyDefinition.AccessModifier.COPY
    }

    def "should use NSArray for sequence sub-type"() {
        def sequence = new Sequence(name: "string")
        when:
        def objCType = typeTransformer.objCType(sequence)
        def accessorModifierForType = typeTransformer.accessorModifierForType(sequence)

        then:
        objCType == "NSArray *";
        accessorModifierForType == ObjCPropertyDefinition.AccessModifier.STRONG
    }

    def "should use fall back to helium type name for unknown type"() {
        def message = new Message(name: "AS")
        when:
        def objCType = typeTransformer.objCType(message)
        def accessorModifierForType = typeTransformer.accessorModifierForType(message)

        then:
        objCType == "AS";
        accessorModifierForType == ObjCPropertyDefinition.AccessModifier.STRONG
    }


    def "should use registered type transoformation if exists"() {
        def message = new Message(name: "AS")
        when:
        typeTransformer.registerRefTypeTransformation("AS", "SomePrefix_AS");
        def objCType = typeTransformer.objCType(message)
        def accessorModifierForType = typeTransformer.accessorModifierForType(message)

        then:
        objCType == "SomePrefix_AS *";
        accessorModifierForType == ObjCPropertyDefinition.AccessModifier.STRONG
    }

    def "should user correct access modifier if such was registered"() {
        def message = new Message(name: "AS")
        when:
        typeTransformer.registerRefTypeTransformation("AS", "SomePrefix_AS", ObjCPropertyDefinition.AccessModifier.WEAK);
        def objCType = typeTransformer.objCType(message)
        def accessorModifierForType = typeTransformer.accessorModifierForType(message)

        then:
        objCType == "SomePrefix_AS *";
        accessorModifierForType == ObjCPropertyDefinition.AccessModifier.WEAK
    }

}
