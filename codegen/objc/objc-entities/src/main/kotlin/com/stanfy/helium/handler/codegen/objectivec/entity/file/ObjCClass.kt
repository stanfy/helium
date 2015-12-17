package com.stanfy.helium.handler.codegen.objectivec.entity.file;

/**
 * Created by ptaykalo on 8/17/14.
 * Simple object structure that holds information about objectiveC class
 */
public class ObjCClass(val name: String) {

  /**
   * Link to class implementation part.
   * This part lives in the ObjCImplementationFile
   */
  public var implementation: ObjCImplementationFileSourcePart? = null

  /**
   * Link to class definition part.
   * This part lives in the ObjCCDefinitionFile
   */
  public var definition: ObjCClassInterface? = null

}

