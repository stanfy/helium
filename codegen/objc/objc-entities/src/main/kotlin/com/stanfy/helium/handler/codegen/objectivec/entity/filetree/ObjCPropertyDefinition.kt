package com.stanfy.helium.handler.codegen.objectivec.entity.filetree;

import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCType
import com.stanfy.helium.model.Field;

public enum class AccessModifier {
  COPY,
  RETAIN,
  ASSIGN,
  STRONG,
  WEAK
}

public enum class AtomicModifier {
  ATOMIC,
  NONATOMIC,
}

/**
 * Created by ptaykalo on 8/19/14.
 * Wrapper for ObjC property
 */
public class ObjCPropertyDefinition : ObjCSourcePart {

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
  public var name: String;

  /**
   * Property result type (this is the type, which will be simply translated to the output)
   * so, in case of ObjC - it should be NSString, and NSArray... etc
   */
  public var type: ObjCType;

  /**
   * Access modifier for property AccessModifier.STRONG - for default value
   */
  private var accessModifier = AccessModifier.STRONG;

  /**
   * By default we'll create non-atomic modifier
   */
  public var atomicModifier = AtomicModifier.NONATOMIC;

  /**
   * Additional comment
   */
  public var comment: String? = null;

  //TODO :  Remove it from here
  /**
   * the Helium filed, from which this property was generated
   */
  public var correspondingField: Field? = null;

  /**
   * Returns true, if this property is sequence,
   * And property type is ono corresponds to the actual items type
   */
  public var isSequence: Boolean = false;

  /**
   * Holds information for item type, that will be presented in this property.
   * Property type can be "NSArray / NSSet" etc
   * This property will hold type of actual items, those are in this property
   */
  public var sequenceType: ObjCType? = null;


  override fun asString(): String {
    var propertyDeclaration = "@property(" + atomicModifier.toString().toLowerCase() + ", " + accessModifier.toString().toLowerCase() + ") " + type + " " + name + ";";
    if (comment != null) {
      propertyDeclaration = "// " + comment + "\n" + propertyDeclaration;
    }
    return propertyDeclaration;
  }

}
