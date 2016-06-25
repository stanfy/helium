package com.stanfy.helium.handler.codegen.objectivec.entity.model

/**
 * Created by paultaykalo on 12/18/15.
 */
data class ObjCType(val name: String, val isReference: Boolean, val isCustom:Boolean) {

  constructor(name: String) : this(name, isReference = true, isCustom = false)
  constructor(name: String, isReference: Boolean) : this(name, isReference = isReference, isCustom = false)

  /**
   * Type can be generic of some other type
   */
  var genericOf: ObjCType? = null

}
