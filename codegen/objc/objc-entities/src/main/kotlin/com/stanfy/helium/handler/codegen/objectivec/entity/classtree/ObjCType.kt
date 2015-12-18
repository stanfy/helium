package com.stanfy.helium.handler.codegen.objectivec.entity.classtree

/**
 * Created by paultaykalo on 12/18/15.
 */
data class ObjCType (val name:String, val isReference:Boolean){

  public constructor(name:String):this(name, isReference = true)

  /**
   * Type can be generic of some other type
   */
  var genericOf:ObjCType? = null

  override fun toString(): String {
    val bld = StringBuilder()
    bld.append(name)
    if (genericOf != null) {
      bld.append("<").append(genericOf).append(">")
    }
    if (isReference) {
      bld.append(" *")
    }
    return bld.toString()
  }
}
