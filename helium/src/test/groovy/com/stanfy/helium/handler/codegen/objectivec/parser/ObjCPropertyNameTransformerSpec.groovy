package com.stanfy.helium.handler.codegen.objectivec.parser

import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCPropertyDefinition.AccessModifier
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import com.stanfy.helium.utils.ConfigurableProxy
import spock.lang.Specification

import static com.stanfy.helium.utils.DslUtils.runWithProxy

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCPropertyNameTransformerSpec extends Specification{

    ObjCPropertyNameTransformer propertyNameTransformer;

    def setup() {
        propertyNameTransformer = new ObjCPropertyNameTransformer();
    }

    def "should not allow to generate property names with reserved keywords"() {
        expect:
        propertyNameTransformer.propertyNameFrom("id") != "id"
        propertyNameTransformer.propertyNameFrom("copy") != "copy"
        propertyNameTransformer.propertyNameFrom("assign") != "assign"
        propertyNameTransformer.propertyNameFrom("selector") != "selector"
        propertyNameTransformer.propertyNameFrom("static") != "static"
    }


    def "should not allow to generate property names with reserved keywords Complex check"() {
        given:
        def convertedProperties = propertyNameTransformer.KEYWORDS.collectAll { s -> propertyNameTransformer.propertyNameFrom(s)}
        expect:
        convertedProperties.every { s -> !propertyNameTransformer.KEYWORDS.contains(s)}
    }

    def "should not allow to generate properties, which names were passed through set"() {
        expect:
        propertyNameTransformer.propertyNameFrom("someProperty", ["someProperty"] as Set) != "someProperty"
    }

    def "should not allow to generate properties, which names were passed through set (recursiveCall)"() {
        def firstGenerated = propertyNameTransformer.propertyNameFrom("someProperty", ["someProperty"] as Set)
        def secondGenerated = propertyNameTransformer.propertyNameFrom("someProperty", ["someProperty", firstGenerated] as Set)
        def thirdGenerated = propertyNameTransformer.propertyNameFrom("someProperty", ["someProperty", firstGenerated, secondGenerated] as Set)
        expect:
        firstGenerated != "someProperty"
        secondGenerated != "someProperty"
        secondGenerated != firstGenerated
        thirdGenerated != "someProperty"
        thirdGenerated != firstGenerated
        thirdGenerated != secondGenerated
    }

}
