package com.stanfy.helium.handler.codegen.objectivec.file;

import com.stanfy.helium.model.Field;

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

  /**
   * the Helium filed, from which this property was generated
   */
  private Field correspondingField;

  /**
   * Returns true, if this property is sequence,
   * And property type is ono corresponds to the actual items type
   */
  private boolean isSequence;



  /**
   * Holds information for item type, that will be presented in this property.
   * Property type can be "NSArray / NSSet" etc
   * This proeprty will hold type of actual items, those are inthis property
   */
  private String sequenceType;


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

  public Field getCorrespondingField() {
    return correspondingField;
  }

  public void setCorrespondingField(final Field correspondingField) {
    this.correspondingField = correspondingField;
  }


  public boolean isSequence() { return isSequence; }

  public void setSequence(final boolean isSequence) { this.isSequence = isSequence; }

  public String getSequenceType() { return sequenceType; }

  public void setSequenceType(final String sequenceType) { this.sequenceType = sequenceType; }

  @Override
  public String asString() {
    String propertyDeclaration = "@property(" + atomicModifier.toString().toLowerCase() + ", " + accessModifier.toString().toLowerCase() + ") " + type + " " + name + ";";
    if (comment != null) {
      propertyDeclaration = "// " + comment + "\n" + propertyDeclaration;
    }
    return propertyDeclaration;
  }
}
