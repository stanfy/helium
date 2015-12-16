package com.stanfy.helium.handler.codegen.objectivec.entity.generator

import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCClassInterface
import spock.lang.Specification

import java.util.regex.Pattern

/**
 * Created by ptaykalo on 8/25/14.
 */
class ObjCClassDefinitionGenerator extends Specification {

  ObjCClassInterface classDefinition;

  def setup() {
    classDefinition = new ObjCClassInterface("S");
  }

  def "should generate external classes parts"() {
    given:
    def externalClassName = "ExternalOne"
    classDefinition.addExternalClassDeclaration(externalClassName)
    def regex = Pattern.compile(".*\\s*@class\\s*" + externalClassName + "\\s*;.*", Pattern.DOTALL);
    expect:
    regex.matcher(classDefinition.asString()).matches()

  }
}

