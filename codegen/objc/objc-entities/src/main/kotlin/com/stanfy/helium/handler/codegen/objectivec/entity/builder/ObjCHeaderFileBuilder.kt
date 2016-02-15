package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCComplexClass
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCClassInterface
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCMethodDeclarationSourcePart

/**
 * Created by paultaykalo on 12/17/15.
 * Builder that is responsible for generating actual contents of header class
 * For specified ObjC Class structure
 */
class ObjCHeaderFileBuilder : ObjCBuilder<ObjCComplexClass, String> {

  override fun build(from: ObjCComplexClass, options: ObjCEntitiesOptions?): String {
//    val builder = StringBuilder()
//
//    builder.append(from.classesForwardDeclarations.joinToString("") { className -> "@class $className;\n" })
//    builder.append(from.protocolsForwardDeclarations.joinToString("") { protocolName -> "@protocol $protocolName;\n" })
//    val clz = from.definition
//    builder.append(clz.sourcePartsAtLocation(ObjCClassInterface.SourcePartLocation.IMPORT)
//        .joinToString("\n") { import -> "${import.asString()}" })
//    builder.append("\n")
//
//    builder.append("@interface ").append(clz.className).append(" : ").append(clz.superClassName)
//    if (clz.implementedProtocols.size > 0) {
//      builder.append("<")
//      builder.append(clz.implementedProtocols.joinToString())
//      builder.append(">")
//    }
//    builder.append("\n")
//
//    builder.append(clz.sourcePartsAtLocation(ObjCClassInterface.SourcePartLocation.BEFORE_PROPERTIES_DEFINITIONS)
//        .joinToString("\n") { sourcePart -> "${sourcePart.asString()}" })
//
//    for (propertyDefinition in clz.propertyDefinitions) {
//      builder.append(propertyDefinition.asString()).append("\n")
//    }
//
//    builder.append(clz.sourcePartsAtLocation(ObjCClassInterface.SourcePartLocation.AFTER_PROPERTIES_DEFINITIONS)
//        .joinToString("\n") { sourcePart -> "${sourcePart.asString()}" })
//
//
//    for (method in clz.methods) {
//      builder.append(ObjCMethodDeclarationSourcePart(method).asString()).append("\n")
//    }
//
//    builder.append("@end")
//    return builder.toString()
    return ""
  }
}