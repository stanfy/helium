package com.stanfy.helium.handler.codegen.objectivec.entity.typemapping

import com.stanfy.helium.handler.codegen.objectivec.entity.model.ObjCType
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import spock.lang.Specification

class ObjCTypeMappingRegistrySpec extends Specification {

  def ObjCTypeMappingRegistry sut

  def setup() {
    sut = new ObjCTypeMappingRegistry()
  }

  def "should use NSString for string type"() {
    def string = new Type(name: "string")
    when:
    def objCType = sut.objcType(string)

    then:
    objCType.name == "NSString"
    objCType.isReference
  }

  def "should use correct simple transform for simple types"() {
    given:
    Type type = new Type(name: heliumType);

    def objCType = sut.objcType(type)

    expect:
    objCType.name == objcType
    objCType.isReference

    where:
    heliumType | objcType
    "int32"    | "NSNumber"
    "int64"    | "NSNumber"
    "long"     | "NSNumber"
    "bool"     | "NSNumber"
    "boolean"  | "NSNumber"
    "float"    | "NSNumber"
    "float32"  | "NSNumber"
    "float64"  | "NSNumber"
    "double"   | "NSNumber"
  }

  def "should use NSArray type for sequence types"() {
    given:
    Type type = new Sequence(name: "Sequence")

    when:
    def objCType = sut.objcType(type)

    then:
    objCType.name == "NSArray"
    objCType.isReference
  }

  def "should use generic typing NSArray type for sequence types with specified item types"() {
    given:

    Type type = new Sequence(name: "Sequence")
    type.itemsType = new Type(name: "string")

    when:
    def objCType = sut.objcType(type)

    then:
    objCType.name == "NSArray"
    objCType.isReference
    objCType.genericOf != null
    objCType.genericOf.name == "NSString"
    objCType.genericOf.isReference
  }

  def "should use fall back to helium type name for unknown type"() {
    def message = new Message(name: "AS")
    when:
    def objCType = sut.objcType(message)

    then:
    objCType.name == "AS";
  }

  def "should use registered type mapping if exists"() {
    given:
    def message = new Message(name: "ABC")
    sut.registerMapping(message, new ObjCType("BRC"));

    when:
    def objCType = sut.objcType(message)

    then:
    objCType.name == "BRC";
    objCType.isReference
  }

}
