package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCClass

/**
 * Created by paultaykalo on 12/17/15.
 * Builder that is responsinble for generating actual contents of header class
 * For specified ObjC Class structure
 */
class ObjCHeaderFileBuilder : ObjCBuilder<ObjCClass, String> {

  override fun build(from: ObjCClass, options: ObjCEntitiesOptions?): String {
    val builder = StringBuilder()

    builder.append(from.classesForwardDeclarations.joinToString("") { className -> "@class $className;\n" })
    builder.append(from.protocolsForwardDeclarations.joinToString("") { protocolName -> "@protocol $protocolName;\n" })
    builder.append(from.definition.asString())
    return builder.toString()
  }

}