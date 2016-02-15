package com.stanfy.helium.handler.codegen.objectivec.entity.classtree

import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCImportPart
import com.stanfy.helium.handler.codegen.objectivec.entity.model.ObjCProperty
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCSourcePart
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCStringSourcePart


/**
 * Created by ptaykalo on 8/17/14.
 * Holds information about class Definition for specific Objective-C class with specific ClassName
 */
public class ObjCClassInterface(val className: String)  {

  public enum class SourcePartLocation {
    IMPORT,
    BEFORE_PROPERTIES_DEFINITIONS,
    AFTER_PROPERTIES_DEFINITIONS,
  }

  public val sourceParts = hashMapOf<SourcePartLocation, MutableList<ObjCSourcePart>>()

  public var superClassName: String = "NSObject"
  public val implementedProtocols = hashSetOf<String>()

  public val propertyDefinitions = arrayListOf<ObjCProperty>()

  public val methods = arrayListOf<ObjCMethod>()

  public fun addSourcePartToLocation(sourcePart: ObjCSourcePart, location: SourcePartLocation) {
    val sourcePartsAtLocation = sourceParts.getOrPut(key = location, defaultValue = { arrayListOf<ObjCSourcePart>() })
    sourcePartsAtLocation.add(sourcePart)
  }

  public fun addSourcePartToLocation(sourcePart: String, location: SourcePartLocation) {
    addSourcePartToLocation(ObjCStringSourcePart(sourcePart), location)
  }

  public fun sourcePartsAtLocation(location:SourcePartLocation): MutableList<ObjCSourcePart> {
    var parts = sourceParts[location]
    if (parts == null){
      parts = arrayListOf<ObjCSourcePart>()
      sourceParts[location] = parts
    }
    return parts
  }

  public fun importClassWithName(className:String) {
    addSourcePartToLocation(ObjCImportPart(className), SourcePartLocation.IMPORT)
  }

  public fun importFrameworkWithName(className:String) {
    addSourcePartToLocation(ObjCImportPart(className, true), SourcePartLocation.IMPORT)
  }

  /**
   * Adds specified property definition to this class
   */
  public fun addPropertyDefinition(property: ObjCProperty) {
    propertyDefinitions.add(property)
  }

  public fun addPropertyDefinitionsList(properties: List<ObjCProperty>) {
    propertyDefinitions.addAll(properties)
  }

  /**
   * Adds specified method definition to this class
   */
  public fun addMethod(method: ObjCMethod) {
    methods.add(method)
  }

}
