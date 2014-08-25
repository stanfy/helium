package com.stanfy.helium.handler.codegen.objectivec.file;

/**
 * Created by ptaykalo on 8/19/14.
 *
 */
public class ObjCPropertyDefinition implements ObjCSourcePart {

  public enum AccessModifier {
    COPY,
    RETAIN,
    ASSIGN,
    STRONG,
    WEAK
  }

  public enum AtomicModifier {
    ATOMIC,
    NONATOMIC,
  }


  /*
  Property name
   */
  private String name;

  /*
  Property result type
   */
  private String type;

  /*
  Access modifier for property AccessModifier.STRONG - for default value
   */
  private AccessModifier accessModifier;

  /*
  By default we'll create non-atomic modifier
   */
  private AtomicModifier atomicModifier;

  public ObjCPropertyDefinition(final String name, final String type) {
    this(name, type, AccessModifier.STRONG, AtomicModifier.NONATOMIC);
  }

  public ObjCPropertyDefinition(final String name, final String type, final AccessModifier accessModifier) {
    this(name, type, accessModifier, AtomicModifier.NONATOMIC);
  }

  public ObjCPropertyDefinition(final String name, final String type, final AccessModifier accessModifier, final AtomicModifier atomicModifier) {
    this.name = name;
    this.type = type;
    this.accessModifier = accessModifier;
    this.atomicModifier = atomicModifier;
  }

  @Override
  public String asString() {
    return null;
  }
}
