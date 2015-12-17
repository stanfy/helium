package com.stanfy.helium.handler.codegen.objectivec.entity.filetree;

/**
 * Created by ptaykalo on 9/10/14.
 * Source part that represents import line
 */
public class ObjCImportPart(val filename:String) : ObjCSourcePart {
  override fun asString(): String {
    return "#import \"$filename.h\"";
  }

}
