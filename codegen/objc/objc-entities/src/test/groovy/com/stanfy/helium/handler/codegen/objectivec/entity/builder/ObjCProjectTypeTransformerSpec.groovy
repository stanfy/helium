package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.Helium
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.AccessModifier
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import spock.lang.Specification

/**
 * Tests for ObjCProjectTypeTransformer.
 */
class ObjCProjectTypeTransformerSpec extends Specification {

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
    accessorModifierForType == AccessModifier.COPY
  }

  def "should use correct simple transform for types"() {
    given:
    Type type = new Type(name: heliumType);

    def objCType = typeTransformer.objCType(type)
    def accessorModifierForType = typeTransformer.accessorModifierForType(type)

    expect:
    objCType == objcType
    accessorModifierForType == accessModifier;

    where:
    heliumType | objcType    | accessModifier
    "int32"    | "NSInteger" | AccessModifier.ASSIGN
    "int64"    | "NSInteger" | AccessModifier.ASSIGN
    "long"     | "NSInteger" | AccessModifier.ASSIGN
    "bool"     | "BOOL"      | AccessModifier.ASSIGN
    "boolean"  | "BOOL"      | AccessModifier.ASSIGN
    "float"    | "double"    | AccessModifier.ASSIGN
    "float32"  | "double"    | AccessModifier.ASSIGN
    "float64"  | "double"    | AccessModifier.ASSIGN
    "double"   | "double"    | AccessModifier.ASSIGN
  }

  def "should use correct simple transform for groovy types(Long)"() {
    given:
    def project = new Helium().defaultTypes().from {
      type 'A' message {
        foo long
      }
    }.project
    Type longType = project.messages[0].fields[0].type

    def objCType = typeTransformer.objCType(longType)
    def accessorModifierForType = typeTransformer.accessorModifierForType(longType)

    expect:
    objCType == "NSInteger"
    accessorModifierForType == AccessModifier.ASSIGN;
  }

  def "should use NSArray for sequence sub-type"() {
    def sequence = new Sequence(name: "string")
    when:
    def objCType = typeTransformer.objCType(sequence)
    def accessorModifierForType = typeTransformer.accessorModifierForType(sequence)

    then:
    objCType == "NSArray *";
    accessorModifierForType == AccessModifier.STRONG
  }

  def "should use fall back to helium type name for unknown type"() {
    def message = new Message(name: "AS")
    when:
    def objCType = typeTransformer.objCType(message)
    def accessorModifierForType = typeTransformer.accessorModifierForType(message)

    then:
    objCType == "AS";
    accessorModifierForType == AccessModifier.STRONG
  }


  def "should use registered type transoformation if exists"() {
    def message = new Message(name: "AS")
    when:
    typeTransformer.registerRefTypeTransformation("AS", "SomePrefix_AS");
    def objCType = typeTransformer.objCType(message)
    def accessorModifierForType = typeTransformer.accessorModifierForType(message)

    then:
    objCType == "SomePrefix_AS *";
    accessorModifierForType == AccessModifier.STRONG
  }

  def "should user correct access modifier if such was registered"() {
    def message = new Message(name: "AS")
    when:
    typeTransformer.registerRefTypeTransformation("AS", "SomePrefix_AS", AccessModifier.WEAK);
    def objCType = typeTransformer.objCType(message)
    def accessorModifierForType = typeTransformer.accessorModifierForType(message)

    then:
    objCType == "SomePrefix_AS *";
    accessorModifierForType == AccessModifier.WEAK
  }

}
