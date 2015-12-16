package com.stanfy.helium.handler.codegen.objectivec.entity.file;

/**
 * Created by ptaykalo on 8/17/14.
 * Simple object structure that holds information about objectiveC class
 */
public class ObjCClass {

  /**
   * Class name is unique in the project, since objC still doesn't have normal packages or namespaces
   */
  private String name;
  /**
   * Link to class implementation part.
   * This part lives in the ObjCImplementationFile
   */
  private ObjCImplementationFileSourcePart implementation;

  /**
   * Link to class definition part.
   * This part lives in the ObjCCDefinitionFile
   */
  private ObjCClassInterface definition;

  public ObjCClass(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public ObjCImplementationFileSourcePart getImplementation() { return implementation; }

  public void setImplementation(final ObjCImplementationFileSourcePart implementation) {
    this.implementation = implementation;
  }

  public ObjCClassInterface getDefinition() { return definition; }

  public void setDefinition(final ObjCClassInterface definition) {
    this.definition = definition;
  }
}

