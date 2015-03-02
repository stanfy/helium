package com.stanfy.helium.handler.codegen.objectivec.file;

/**
 * Created by ptaykalo on 8/17/14.
 * Simple object structure that holds information about objectiveC class
 */
public class ObjCClass {

  /*
  Class name is unique in the project, since objC still doesn't have normal packages or namespaces
   */
  private String name;
  /*
  Link to class implementation part.
  This part lives in the ObjCImplementationFile
   */
  private ObjCClassImplementation implementation;
    /*
  Link to class definition part.
  This part lives in the ObjCCDefinitionFile
   */
  private ObjCClassDefinition definition;

  public ObjCClass(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public ObjCClassImplementation getImplementation() {
    return implementation;
  }
  public void setImplementation(final ObjCClassImplementation implementation) {
    this.implementation = implementation;
  }

  public ObjCClassDefinition getDefinition() {
    return definition;
  }
  public void setDefinition(final ObjCClassDefinition definition) {
    this.definition = definition;
  }
}

