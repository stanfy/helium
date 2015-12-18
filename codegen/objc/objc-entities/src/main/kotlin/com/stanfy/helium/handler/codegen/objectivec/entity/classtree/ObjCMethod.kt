package com.stanfy.helium.handler.codegen.objectivec.entity.classtree

import java.util.*

/**
 * Created by paultaykalo on 12/18/15.
 */
class ObjCMethod {
  public enum class ObjCMethodType {
    CLASS,
    INSTANCE
  }

  data class ParameterPair(val type: String, val name: String)


  var methodType = ObjCMethodType.INSTANCE;

  /**
   * Method name
   */
  public var name: String = ""
    private set

  /**
   * Method return type. simple string is used
   */
  var returnType: String = "void";

  /**
   * List of parameters
   * Enry key is a type
   * Entry value is parameter name
   */
  val parameters = ArrayList<ParameterPair>();


  constructor(name: String) :
  this(name, ObjCMethodType.INSTANCE, "void") {
  }

  constructor(name: String, methodType: ObjCMethod.ObjCMethodType, returnType: String) {
    this.methodType = methodType;
    this.name = name;
    this.returnType = returnType;
  }

  /**
   * Adds parameter to the list of parameters
   * @param type parameter type
   * @param name parameter name
   */
  public fun addParameter(type: String, name: String) {
    parameters.add(ParameterPair(type, name))
  }

}