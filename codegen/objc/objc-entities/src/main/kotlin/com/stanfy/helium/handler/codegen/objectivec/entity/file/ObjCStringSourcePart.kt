package com.stanfy.helium.handler.codegen.objectivec.entity.file;

/**
 * Created by ptaykalo on 9/10/14.
 * Simpliest objc Source part, which  simply returns contents
 */
public class ObjCStringSourcePart(val contents: String = "") : ObjCSourcePart {

  override fun asString(): String {
    return contents;
  }

}
