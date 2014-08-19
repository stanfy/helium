package com.stanfy.helium.handler.codegen.objectivec.file;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ptaykalo on 8/17/14.
 * Holds information about class Definition for spcific obejctive class with specific ClassName
 */
public class ObjCClassDefinition implements ObjCSourcePart {
  /*
Class Name
 */
  private String className;
  private ArrayList<ObjCPropertyDefinition>propertyDefinitions = new ArrayList<ObjCPropertyDefinition>();

  public ObjCClassDefinition(final String className) {
    this.className = className;
  }

  public String getClassName() {
    return className;
  }

  @Override
  public String asString() {
    // TODO use some templates
    return
        String.join("\n",
            "@interface " + className + " : NSObject",
            "@end"
        );
  }

  /*
  Adds spcecified property definition to this class
   */
  public void addPropertyDefinition(ObjCPropertyDefinition property) {
    propertyDefinitions.add(property);
  }

  public List<ObjCPropertyDefinition> getPropertyDefinitions() {
    return propertyDefinitions;
  }
}
