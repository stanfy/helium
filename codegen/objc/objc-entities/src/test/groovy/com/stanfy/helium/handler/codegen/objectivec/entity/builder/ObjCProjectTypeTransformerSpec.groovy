package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.Helium
import com.stanfy.helium.handler.codegen.objectivec.entity.model.ObjCType
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
    objCType.name == "NSString"
    objCType.isReference
    accessorModifierForType == AccessModifier.COPY
  }

  def "should use correct simple transform for types"() {
    given:
    Type type = new Type(name: heliumType);

    def objCType = typeTransformer.objCType(type)
    def accessorModifierForType = typeTransformer.accessorModifierForType(type)

    expect:
    objCType.name == objcType
    objCType.isReference
    accessorModifierForType == accessModifier;

    where:
    heliumType | objcType    | accessModifier
    "int32"    | "NSNumber"  | AccessModifier.STRONG
    "int64"    | "NSNumber"  | AccessModifier.STRONG
    "long"     | "NSNumber"  | AccessModifier.STRONG
    "bool"     | "NSNumber"  | AccessModifier.STRONG
    "boolean"  | "NSNumber"  | AccessModifier.STRONG
    "float"    | "NSNumber"  | AccessModifier.STRONG
    "float32"  | "NSNumber"  | AccessModifier.STRONG
    "float64"  | "NSNumber"  | AccessModifier.STRONG
    "double"   | "NSNumber"  | AccessModifier.STRONG
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
    objCType.name == "NSNumber"
    objCType.isReference
    accessorModifierForType == AccessModifier.STRONG;
  }

  def "should use NSArray for sequence sub-type"() {
    def sequence = new Sequence(name: "string")
    when:
    def objCType = typeTransformer.objCType(sequence)
    def accessorModifierForType = typeTransformer.accessorModifierForType(sequence)

    then:
    objCType.name == "NSArray";
    objCType.isReference
    accessorModifierForType == AccessModifier.STRONG
  }

  def "should use NSArray with generic for sequence sub-type"() {
    def sequence = new Sequence(name: "somename", itemsType: new Type(name: "string"))
    when:
    def objCType = typeTransformer.objCType(sequence)
    def accessorModifierForType = typeTransformer.accessorModifierForType(sequence)

    then:
    objCType.name == "NSArray"
    objCType.isReference
    objCType.genericOf
    objCType.genericOf.name == "NSString"
    objCType.genericOf.isReference
    accessorModifierForType == AccessModifier.STRONG
  }


  def "should use fall back to helium type name for unknown type"() {
    def message = new Message(name: "AS")
    when:
    def objCType = typeTransformer.objCType(message)
    def accessorModifierForType = typeTransformer.accessorModifierForType(message)

    then:
    objCType.name == "AS";
    accessorModifierForType == AccessModifier.STRONG
  }


  def "should use registered type transoformation if exists"() {
    def message = new Message(name: "AS")
    when:
    typeTransformer.registerTransformation("AS", new ObjCType("SomePrefix_AS"));
    def objCType = typeTransformer.objCType(message)
    def accessorModifierForType = typeTransformer.accessorModifierForType(message)

    then:
    objCType.name == "SomePrefix_AS";
    objCType.isReference
    accessorModifierForType == AccessModifier.STRONG
  }

  def "should user correct access modifier if such was registered"() {
    def message = new Message(name: "AS")
    when:
    typeTransformer.registerTransformation("AS", new ObjCType("SomePrefix_BS"), AccessModifier.WEAK);
    def objCType = typeTransformer.objCType(message)
    def accessorModifierForType = typeTransformer.accessorModifierForType(message)

    then:
    objCType.name == "SomePrefix_BS";
    objCType.isReference
    accessorModifierForType == AccessModifier.WEAK
  }

}
