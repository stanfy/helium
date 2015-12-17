package com.stanfy.helium.handler.codegen.objectivec.entity.file

import java.util.ArrayList
import java.util.HashSet

/**
 * Created by ptaykalo on 8/17/14.
 * Holds information about class Definition for specific Objective-C class with specific ClassName
 */
public class ObjCClassInterface(val className: String) : ObjCSourcePart {

  public var propertyDefinitions = ArrayList<ObjCPropertyDefinition>()
    private set

  public var externalClassDeclaration = HashSet<String>()
    private set

  override fun asString(): String {
    // TODO use some templates
    val bld = StringBuilder()
    for (externalClass in externalClassDeclaration) {
      bld.append("@class ").append(externalClass).append(";\n")
    }
    bld.append("@interface ").append(className).append(" : NSObject").append("\n")
    for (propertyDefinition in propertyDefinitions) {
      bld.append(propertyDefinition.asString()).append("\n")
    }
    bld.append("@end")
    return bld.toString()
  }


  /**
   * Adds specified property definition to this class
   */
  public fun addPropertyDefinition(property: ObjCPropertyDefinition) {
    propertyDefinitions.add(property)
  }

  /**
   * Adds external class declaration string. This one should be transformed to "@class |externalClass|" in the eneratir
   */
  public fun addExternalClassDeclaration(externalClass: String) {
    externalClassDeclaration.add(externalClass)
  }
}
