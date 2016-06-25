package com.stanfy.helium.handler.codegen.objectivec.entity.classtree

import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCImportPart
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCSourcePart
import java.util.ArrayList

/**
 * Created by ptaykalo on 8/17/14.
 * Simple block that know how to serialize ObjC Class Implementation
 */
class ObjCClassImplementation(val filename: String) : ObjCSourcePart {

  // TODO : Dependencies
  private val importSourceParts = hashSetOf<ObjCImportPart>()
  private val bodySourceParts = arrayListOf<ObjCSourcePart>()

  /**
  Adds specified source part to the top part (before @implementation)
   */
  fun addImportSourcePart(sourcePart: ObjCImportPart) {
    importSourceParts.add(sourcePart)
  }

  fun importClassWithName(className:String) {
    this.addImportSourcePart(ObjCImportPart(className))
  }
  /**
  Adds specified source part to the central part (inside @implementation)
   */
  fun addBodySourcePart(sourcePart: ObjCSourcePart) {
    bodySourceParts.add(sourcePart)
  }

  /**
   * Adds method implementation part to the
   */
  fun addMethod(method:ObjCMethod) {
    addBodySourcePart(ObjCMethodImplementationSourcePart(method))
  }


  override fun asString(): String {
    val bld = StringBuilder()
    bld.append("#import \"").append(filename).append(".h\"\n")
    for (sourcePart in importSourceParts) {
      bld.append(sourcePart.asString()).append("\n")
    }
    bld.append("@implementation ").append(filename).append("\n")
    for (sourcePart in bodySourceParts) {
      bld.append(sourcePart.asString()).append("\n")
    }
    bld.append("@end")
    return bld.toString()

  }
}
