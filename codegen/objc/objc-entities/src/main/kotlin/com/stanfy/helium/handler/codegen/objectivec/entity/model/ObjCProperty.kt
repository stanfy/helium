package com.stanfy.helium.handler.codegen.objectivec.entity.model;

/**
 * Created by ptaykalo on 8/19/14.
 * Wrapper for ObjC property
 */
public class ObjCProperty {

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
  constructor(name: String, type: ObjCType)
  : this(name, type, AccessModifier.STRONG, AtomicModifier.NONATOMIC)

  constructor(name: String, type: ObjCType, accessModifier: AccessModifier)
  : this(name, type, accessModifier, AtomicModifier.NONATOMIC)

  constructor(name: String, type: ObjCType, accessModifier: AccessModifier, atomicModifier: AtomicModifier) {
    this.name = name;
    this.type = type;
    this.accessModifier = accessModifier;
    this.atomicModifier = atomicModifier;
  }

  /**
   * Property name
   */
  public val name: String

  /**
   * Property result type (this is the type, which will be simply translated to the output)
   * so, in case of ObjC - it should be NSString, and NSArray... etc
   */
  public val type: ObjCType

  /**
   * Access modifier for property AccessModifier.STRONG - for default value
   */
  private val accessModifier: AccessModifier

  /**
   * By default we'll create non-atomic modifier
   */
  public val atomicModifier: AtomicModifier

  /**
   * Additional comment
   */
  public var comment: String? = null

}
