package com.stanfy.helium.handler.codegen.objectivec.entity.filetree;

/**
 * Created by ptaykalo on 9/10/14.
 * Simplest objc Source part, which  simply returns contents
 */
public class ObjCStringSourcePart(val contents: String = "") : ObjCSourcePart {

  override fun asString(): String {
    return contents;
  }

}
