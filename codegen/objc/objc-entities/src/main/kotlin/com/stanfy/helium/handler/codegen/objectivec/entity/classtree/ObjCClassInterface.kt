package com.stanfy.helium.handler.codegen.objectivec.entity.classtree

import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCPropertyDefinition
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCSourcePart

/**
 * Created by ptaykalo on 8/17/14.
 * Holds information about class Definition for specific Objective-C class with specific ClassName
 */
public class ObjCClassInterface(val className: String) : ObjCSourcePart {

  public val bodySourceParts = arrayListOf<ObjCSourcePart>()

  public var superClassName: String = "NSObject"

  public var propertyDefinitions = arrayListOf<ObjCPropertyDefinition>()
    private set

  public var methods = arrayListOf<ObjCMethod>()
    private set

  /**
  Adds specified source part to the central part (inside @implementation)
   */
  public fun addBodySourcePart(sourcePart: ObjCSourcePart) {
    bodySourceParts.add(sourcePart)
  }

  override fun asString(): String {
    // TODO use some templates
    val bld = StringBuilder()
    for (sourcePart in bodySourceParts) {
      bld.append(sourcePart.asString()).append("\n")
    }
    bld.append("@interface ").append(className).append(" : ").append(superClassName).append("\n")
    for (propertyDefinition in propertyDefinitions) {
      bld.append(propertyDefinition.asString()).append("\n")
    }
    for (method in methods) {
      bld.append(ObjCMethodDeclarationSourcePart(method).asString()).append("\n")
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
   * Adds speciied method definition to this class
   */
  public fun addMethod(method: ObjCMethod) {
    methods.add(method)
  }
}
