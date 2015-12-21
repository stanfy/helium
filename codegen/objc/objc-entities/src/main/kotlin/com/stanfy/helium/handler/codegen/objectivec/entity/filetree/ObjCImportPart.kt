package com.stanfy.helium.handler.codegen.objectivec.entity.filetree;

/**
 * Created by ptaykalo on 9/10/14.
 * Source part that represents import line
 */
public class ObjCImportPart(val filename:String, val isFrameworkImport:Boolean) : ObjCSourcePart {
  constructor(filename: String):this(filename,false)
  override fun asString(): String {
    if (isFrameworkImport) {
      return "#import <$filename.h>";
    } else {
      return "#import \"$filename.h\"";
    }
  }

}
