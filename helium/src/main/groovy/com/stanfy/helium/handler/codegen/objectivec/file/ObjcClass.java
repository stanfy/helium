package com.stanfy.helium.handler.codegen.objectivec.file;

/**
 * Created by ptaykalo on 8/17/14.
 * Simple object structure that holds information about objectiveC class
 */
public class ObjCClass {

  private String name;
  private ObjCClassImplementation implementation;
  private ObjCClassDefinition definition;

  public ObjCClass(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public ObjCClassImplementation getImplementation() { return implementation; }
  public void setImplementation(ObjCClassImplementation implementation) { this.implementation = implementation; }

  public ObjCClassDefinition getDefinition() { return definition; }
  public void setDefinition(ObjCClassDefinition definition) { this.definition = definition; }
}

