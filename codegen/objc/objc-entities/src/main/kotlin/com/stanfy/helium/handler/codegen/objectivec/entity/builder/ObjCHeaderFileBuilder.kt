package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCClass

/**
 * Created by paultaykalo on 12/17/15.
 * Builder that is responsinble for generating actual contents of header class
 * For specified ObjC Class structure
 */
class ObjCHeaderFileBuilder : ObjCBuilder<ObjCClass, String> {

  override fun build(from: ObjCClass): String {
    return this.build(from, null)
  }

  override fun build(from: ObjCClass, options: ObjCEntitiesOptions?): String {
    val builder = StringBuilder()
    from.classesForwardDeclarations.forEach { s ->
      builder.append("@class ").append(s).append(";\n")
    }
    from.protocolsForwardDeclarations.forEach { s ->
      builder.append("@protocol ").append(s).append(";\n")
    }
    builder.append(from.definition.asString())
    return builder.toString()
  }

}