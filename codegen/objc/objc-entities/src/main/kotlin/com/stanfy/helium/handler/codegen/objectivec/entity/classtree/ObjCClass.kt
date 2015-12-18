package com.stanfy.helium.handler.codegen.objectivec.entity.classtree;

import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCImportPart

/**
 * Created by ptaykalo on 8/17/14.
 * Simple object structure that holds information about objectiveC class
 */
public class ObjCClass(val name: String, val definition: ObjCClassInterface,
                       val implementation: ObjCClassImplementation) {

  public var forwardDeclarations = hashSetOf<String>()
    private set

  /**
   * Adds external class declaration string. This one should be transformed to "@class |externalClass|" in the eneratir
   */
  public fun addForwardDeclaration(externalClass: String) {
    forwardDeclarations.add(externalClass)
  }

}

