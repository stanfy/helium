package com.stanfy.helium.handler.codegen.objectivec.entity.mapper.sfobjectmapping

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProjectComplex
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProjectStructureGenerator
import com.stanfy.helium.model.Project

/**
 * Created by ptaykalo on 9/2/14.
 * Class that is responsible for generate files those are responsible for
 * correct mapping performing from JSON Objects to Messages
 * Generated classes will could be used with
 * https://github.com/stanfy/SFObjectMapping
 */
public class ObjCSFObjectMappingsGenerator : ObjCProjectStructureGenerator {
  public val MAPPINGS_FILENAME = "HeliumMappings"

  override fun generate(project: ObjCProjectComplex, projectDSL: Project, options: ObjCEntitiesOptions) {
//    val mappingsClassName = options.prefix + MAPPINGS_FILENAME
//    val mappingsClass = ObjCComplexClass(mappingsClassName)
//    project.classesTree.addClass(mappingsClass)
//
//    mappingsClass.implementation.importClassWithName("SFMapping")
//    mappingsClass.implementation.importClassWithName("NSObject+SFMapping")
//
//    val initializeMethod = ObjCMethod("initialize", ObjCMethod.ObjCMethodType.CLASS, "void")
//    val initializeMethodSourcePart = ObjCMethodImplementationSourcePart(initializeMethod)
//    mappingsClass.implementation.addBodySourcePart(initializeMethodSourcePart)
//
//    // get property definitions
//    val contentsBuilder = StringBuilder()
//
//    // Generate all them all
//    for (m in projectDSL.messages) {
//      val objCClass = project.classesTree.getClassForType(m.name) ?: continue
//      mappingsClass.implementation.importClassWithName(objCClass.name)
//
//      // Get the implementation
//      contentsBuilder.append("    [").append(objCClass.name).append(" setMappingInfo:").append("\n")
//
//      for (prop in objCClass.definition.propertyDefinitions) {
//        contentsBuilder.append("      [SFMapping ")
//        val field = prop.correspondingField
//        if (field != null) {
//          if (field.isSequence) {
//            val itemClass = prop.sequenceType!!.name
//            contentsBuilder.append("collection:@\"").append(prop.name).append("\" itemClass:@\"").append(itemClass).append("\" toKeyPath:@\"").append(field.name).append("\"],\n")
//          } else {
//            contentsBuilder.append("property:@\"").append(prop.name).append("\" toKeyPath:@\"").append(field.name).append("\"],\n")
//          }
//
//        } else {
//          contentsBuilder.append("property:@\"").append(prop.name).append("\" toKeyPath:@\"").append(prop.name).append("\"],\n")
//        }
//
//      }
//      contentsBuilder.append("    nil];").append("\n\n")
//
//    }
//
//    initializeMethodSourcePart.addSourcePart(ObjCStringSourcePart(contentsBuilder.toString()))
//
  }

}
