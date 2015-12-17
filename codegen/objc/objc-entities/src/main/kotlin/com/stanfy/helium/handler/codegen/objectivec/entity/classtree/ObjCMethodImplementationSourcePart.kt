package com.stanfy.helium.handler.codegen.objectivec.entity.classtree;

import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCSourcePartsContainer
import java.util.*

/**
 * Created by paultaykalo on 12/16/15.
 */
public class ObjCMethodImplementationSourcePart : ObjCSourcePartsContainer {
  public enum class ObjCMethodType {
    CLASS,
    INSTANCE
  }

  private data class ParameterPair(val type: String, val name: String)


  private var methodType = ObjCMethodType.INSTANCE;

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
  private val parameters = ArrayList<ParameterPair>();


  constructor(name: String) :
  this(name, ObjCMethodType.INSTANCE, "void") {
  }

  constructor(name: String, methodType: ObjCMethodImplementationSourcePart.ObjCMethodType, returnType: String) {
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

  override fun asString(): String {
    val bld = StringBuilder();
    bld.append(when (methodType) {
      ObjCMethodType.CLASS -> "-"
      ObjCMethodType.INSTANCE -> "+"
    });
    bld.append("(").append(returnType).append(")");
    bld.append(name);
    for (parameter in  parameters) {
      bld.append(":(");
      bld.append(parameter.type);
      bld.append(")");
      bld.append(parameter.name);
      bld.append(" ");
    }
    bld.append(" {\n");
    bld.append(super.asString());
    bld.append("\n}");
    return bld.toString();
  }
}
