package com.stanfy.helium.handler.codegen.objectivec.entity.classtree

import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCImportPart
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCPropertyDefinition
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCSourcePart
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCStringSourcePart


/**
 * Created by ptaykalo on 8/17/14.
 * Holds information about class Definition for specific Objective-C class with specific ClassName
 */
class ObjCClassInterface(val className: String) : ObjCSourcePart {

  enum class SourcePartLocation {
    IMPORT,
    BEFORE_PROPERTIES_DEFINITIONS,
    AFTER_PROPERTIES_DEFINITIONS,
  }

  val sourceParts = hashMapOf<SourcePartLocation, MutableList<ObjCSourcePart>>()

  var superClassName: String = "NSObject"
  val implementedProtocols = hashSetOf<String>()

  val propertyDefinitions = arrayListOf<ObjCPropertyDefinition>()

  val methods = arrayListOf<ObjCMethod>()

  fun addSourcePartToLocation(sourcePart: ObjCSourcePart, location: SourcePartLocation) {
    val sourcePartsAtLocation = sourceParts.getOrPut(key = location, defaultValue = { arrayListOf<ObjCSourcePart>() })
    sourcePartsAtLocation.add(sourcePart)
  }

  fun addSourcePartToLocation(sourcePart: String, location: SourcePartLocation) {
    addSourcePartToLocation(ObjCStringSourcePart(sourcePart), location)
  }

  fun sourcePartsAtLocation(location:SourcePartLocation): MutableList<ObjCSourcePart> {
    var parts = sourceParts[location]
    if (parts == null){
      parts = arrayListOf<ObjCSourcePart>()
      sourceParts[location] = parts
    }
    return parts
  }

  fun importClassWithName(className:String) {
    addSourcePartToLocation(ObjCImportPart(className), SourcePartLocation.IMPORT)
  }

  fun importFrameworkWithName(className:String) {
    addSourcePartToLocation(ObjCImportPart(className, true), SourcePartLocation.IMPORT)
  }

  /**
   * Adds specified property definition to this class
   */
  fun addPropertyDefinition(property: ObjCPropertyDefinition) {
    propertyDefinitions.add(property)
  }

  fun addPropertyDefinitionsList(properties: List<ObjCPropertyDefinition>) {
    propertyDefinitions.addAll(properties)
  }

  /**
   * Adds speciied method definition to this class
   */
  fun addMethod(method: ObjCMethod) {
    methods.add(method)
  }

  override fun asString(): String {
    // TODO use some templates
    val bld = StringBuilder()
    bld.append(sourcePartsAtLocation(SourcePartLocation.IMPORT)
        .joinToString("\n") { import -> "${import.asString()}" })
    bld.append("\n")

    bld.append("@interface ").append(className).append(" : ").append(superClassName)
    if (implementedProtocols.size > 0) {
      bld.append("<")
      bld.append(implementedProtocols.joinToString())
      bld.append(">")
    }
    bld.append("\n")

    bld.append(sourcePartsAtLocation(SourcePartLocation.BEFORE_PROPERTIES_DEFINITIONS)
        .joinToString("\n") { sourcePart -> "${sourcePart.asString()}" })

    for (propertyDefinition in propertyDefinitions) {
      bld.append(propertyDefinition.asString()).append("\n")
    }

    bld.append(sourcePartsAtLocation(SourcePartLocation.AFTER_PROPERTIES_DEFINITIONS)
        .joinToString("\n") { sourcePart -> "${sourcePart.asString()}" })


    for (method in methods) {
      bld.append(ObjCMethodDeclarationSourcePart(method).asString()).append("\n")
    }

    bld.append("@end")
    return bld.toString()
  }

}
