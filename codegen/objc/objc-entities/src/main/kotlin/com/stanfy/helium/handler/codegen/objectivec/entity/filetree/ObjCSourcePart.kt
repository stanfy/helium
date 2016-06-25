package com.stanfy.helium.handler.codegen.objectivec.entity.filetree;

/**
 * Created by ptaykalo on 8/17/14.
 * Contains information about some Objective-C source Part, that can be transformed To taw Data
 */
interface ObjCSourcePart {

  /**
   * Returns source part as string
   */
  fun asString(): String;
}


/**
 * Simplest objc Source part, which  simply returns contents
 */
class ObjCStringSourcePart(val contents: String = "") : ObjCSourcePart {

  override fun asString(): String {
    return contents;
  }

}
