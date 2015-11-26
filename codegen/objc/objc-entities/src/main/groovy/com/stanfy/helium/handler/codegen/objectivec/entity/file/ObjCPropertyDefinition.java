package com.stanfy.helium.handler.codegen.objectivec.entity.file;

/**
 * Created by ptaykalo on 8/19/14.
 * Wrapper for ObjC property
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
  Property result type (this is the type, which will be simply translated to the output)
  so, in case of ObjC - it should be NSString, and NSArray... etc
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

  /*
    Additional comment
     */
  private String comment;

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

  public String getComment() {
    return comment;
  }

  public void setComment(final String comment) {
    this.comment = comment;
  }


  public AccessModifier getAccessModifier() {
    return accessModifier;
  }

  public AtomicModifier getAtomicModifier() {
    return atomicModifier;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }


  @Override
  public String asString() {
    String propertyDeclaration = "@property(" + atomicModifier.toString().toLowerCase() + ", " + accessModifier.toString().toLowerCase() + ") " + type + " " + name + ";";
    if (comment != null) {
      propertyDeclaration = "// " + comment + "\n" + propertyDeclaration;
    }
    return propertyDeclaration;
  }
}
