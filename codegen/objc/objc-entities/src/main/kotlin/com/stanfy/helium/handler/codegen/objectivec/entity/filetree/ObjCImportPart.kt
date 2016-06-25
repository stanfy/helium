package com.stanfy.helium.handler.codegen.objectivec.entity.filetree;

/**
 * Created by ptaykalo on 9/10/14.
 * Source part that represents import line
 */
class ObjCImportPart(val filename:String, val isFrameworkImport:Boolean) : ObjCSourcePart {
  constructor(filename: String):this(filename,false)
  override fun asString(): String {
    if (isFrameworkImport) {
      return "#import <$filename.h>";
    } else {
      return "#import \"$filename.h\"";
    }
  }

  override fun equals(other: Any?): Boolean{
    if (this === other) return true
    if (other?.javaClass != javaClass) return false

    other as ObjCImportPart

    if (filename != other.filename) return false
    if (isFrameworkImport != other.isFrameworkImport) return false

    return true
  }

  override fun hashCode(): Int{
    var result = filename.hashCode()
    result += 31 * result + isFrameworkImport.hashCode()
    return result
  }


}
