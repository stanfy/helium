package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCSourcePart

import java.util.ArrayList
import java.util.List

/**
 * Created by ptaykalo on 8/17/14.
 * Structure that represent Objective-C source file
 * Concrete implementations can be header, and implementation file
 */
public abstract class ObjCFile(val name: String) : ObjCSourcePart {

  public val sourceParts = ArrayList<ObjCSourcePart>()

  /**
   * File extension :)
   */
  public abstract fun getExtension(): String


  public fun addSourcePart(sourcePart: ObjCSourcePart) {
    sourceParts.add(sourcePart)
  }

  override fun asString(): String {
    val bld = StringBuilder()
    for (part in sourceParts) {
      bld.append(part.asString())
    }
    return bld.toString()
  }
}
