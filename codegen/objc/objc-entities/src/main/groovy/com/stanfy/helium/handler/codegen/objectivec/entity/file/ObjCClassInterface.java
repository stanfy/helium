package com.stanfy.helium.handler.codegen.objectivec.entity.file;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ptaykalo on 8/17/14.
 * Holds information about class Definition for specific Objective-C class with specific ClassName
 */
public class ObjCClassInterface implements ObjCSourcePart {
  /**
   * Class Name
   */
  private String className;
  private ArrayList<ObjCPropertyDefinition> propertyDefinitions = new ArrayList<ObjCPropertyDefinition>();
  private Set<String> externalClassDeclaration = new HashSet<String>();

  public ObjCClassInterface(final String className) {
    this.className = className;
  }

  public String getClassName() {
    return className;
  }

  @Override
  public String asString() {
    // TODO use some templates
    StringBuilder bld = new StringBuilder();
    for (String externalClass : externalClassDeclaration) {
      bld.append("@class ").append(externalClass).append(";\n");
    }
    bld.append("@interface ").append(className).append(" : NSObject").append("\n");
    for (ObjCPropertyDefinition propertyDefinition : propertyDefinitions) {
      bld.append(propertyDefinition.asString()).append("\n");
    }
    bld.append("@end");
    return bld.toString();
  }

  /**
   * Adds specified property definition to this class
   */
  public void addPropertyDefinition(final ObjCPropertyDefinition property) {
    propertyDefinitions.add(property);
  }


  public List<ObjCPropertyDefinition> getPropertyDefinitions() {
    return propertyDefinitions;
  }

  /**
   * Returns a list of the files those are used in this class definition, but hasn't imported
   */
  public Set<String> getExternalClassDeclaration() {
    return externalClassDeclaration;
  }

  /**
   * Adds external class declaration string. This one should be transformed to "@class |externalClass|" in the eneratir
   */
  public void addExternalClassDeclaration(final String externalClass) { externalClassDeclaration.add(externalClass); }
}
