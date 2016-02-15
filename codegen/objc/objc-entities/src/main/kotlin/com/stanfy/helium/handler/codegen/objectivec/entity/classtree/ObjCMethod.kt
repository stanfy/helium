package com.stanfy.helium.handler.codegen.objectivec.entity.classtree

import java.util.*

/**
 * Created by paultaykalo on 12/18/15.
 */
class ObjCMethod(val name: String, val methodType: ObjCMethod.ObjCMethodType, var returnType: String) {

  public enum class ObjCMethodType {
    CLASS,
    INSTANCE
  }

  constructor(name: String) : this(name, ObjCMethodType.INSTANCE, "void")

  data class ParameterPair(val type: String, val name: String)

  /**
   * List of parameters
   * Enry key is a type
   * Entry value is parameter name
   */
  val parameters = ArrayList<ParameterPair>();


  /**
   * Adds parameter to the list of parameters
   * @param type parameter type
   * @param name parameter name
   */
  public fun addParameter(type: String, name: String) {
    parameters.add(ParameterPair(type, name))
  }

}