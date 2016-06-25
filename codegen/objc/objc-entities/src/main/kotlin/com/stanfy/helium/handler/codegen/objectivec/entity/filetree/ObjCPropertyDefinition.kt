package com.stanfy.helium.handler.codegen.objectivec.entity.filetree;

import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCType
import com.stanfy.helium.model.Field;

enum class AccessModifier {
  COPY,
  RETAIN,
  ASSIGN,
  STRONG,
  WEAK
}

enum class AtomicModifier {
  ATOMIC,
  NONATOMIC,
}

/**
 * Created by ptaykalo on 8/19/14.
 * Wrapper for ObjC property
 */
class ObjCPropertyDefinition : ObjCSourcePart {

  constructor(name: String, type: ObjCType) :
  this(name, type, AccessModifier.STRONG, AtomicModifier.NONATOMIC) {

  }

  constructor(name: String, type: ObjCType, accessModifier: AccessModifier) :
  this(name, type, accessModifier, AtomicModifier.NONATOMIC) {
  }

  constructor(name: String, type: ObjCType, accessModifier: AccessModifier, atomicModifier: AtomicModifier) {
    this.name = name;
    this.type = type;
    this.accessModifier = accessModifier;
    this.atomicModifier = atomicModifier;
  }

  /**
   * Property name
   */
  val name: String

  /**
   * Property result type (this is the type, which will be simply translated to the output)
   * so, in case of ObjC - it should be NSString, and NSArray... etc
   */
  val type: ObjCType

  /**
   * Access modifier for property AccessModifier.STRONG - for default value
   */
  private val accessModifier:AccessModifier

  /**
   * By default we'll create non-atomic modifier
   */
  val atomicModifier:AtomicModifier

  /**
   * Additional comment
   */
  var comment: String? = null

  //TODO :  Remove it from here?
  /**
   * the Helium filed, from which this property was generated
   */
  var correspondingField: Field? = null

  /**
   * Returns true, if this property is sequence,
   * And property type is ono corresponds to the actual items type
   */
  var isSequence: Boolean = false;

  /**
   * Holds information for item type, that will be presented in this property.
   * Property type can be "NSArray / NSSet" etc
   * This property will hold type of actual items, those are in this property
   */
  var sequenceType: ObjCType? = null;


  override fun asString(): String {
    var propertyDeclaration = "@property(" + atomicModifier.toString().toLowerCase() + ", " + accessModifier.toString().toLowerCase() + ") " + type + " " + name + ";";
    if (comment != null) {
      propertyDeclaration = "// " + comment + "\n" + propertyDeclaration;
    }
    return propertyDeclaration;
  }

}
