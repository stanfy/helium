package com.stanfy.helium.handler.codegen.objectivec.generator

import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClassDefinition
import spock.lang.Specification

import java.util.regex.Pattern

/**
 * Created by ptaykalo on 8/25/14.
 */
class ObjCClassDefinitionGenerator extends Specification {

  ObjCClassDefinition classDefinition;

  def setup() {
    classDefinition = new ObjCClassDefinition("S");
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

