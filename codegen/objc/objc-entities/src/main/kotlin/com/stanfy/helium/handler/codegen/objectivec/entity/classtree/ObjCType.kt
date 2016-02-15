package com.stanfy.helium.handler.codegen.objectivec.entity.classtree

/**
 * Created by paultaykalo on 12/18/15.
 */
data class ObjCType(val name: String, val isReference: Boolean, val isCustom:Boolean) {

  public constructor(name: String) : this(name, isReference = true, isCustom = false)
  public constructor(name: String, isReference: Boolean) : this(name, isReference = isReference, isCustom = false)

  /**
   * Type can be generic of some other type
   */
  var genericOf: ObjCType? = null

  /**
   * I hope no one will name their classes with NS prefix :)
   */
  fun isFoundationType(): Boolean {
    return name.startsWith("NS") || name == "id"
  }

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
