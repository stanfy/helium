package com.stanfy.helium.handler.codegen.objectivec.entity.file;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by paultaykalo on 12/16/15.
 */
public class ObjCMethodImplementationSourcePart extends ObjCSourcePartsContainer {
  public enum ObjCMethodType {
    CLASS,
    INSTANCE;
  }


  ObjCMethodType methodType = ObjCMethodType.INSTANCE;

  /**
   * Method name
   */
  String name;

  /**
   * Method return type. simple string is used
   */
  String returnType = "void";

  /**
   * List of parameters
   * Enry key is a type
   * Entry value is parameter name
   */
  ArrayList<Map.Entry<String, String>> parameters = new ArrayList<Map.Entry<String, String>>();


  public ObjCMethodImplementationSourcePart(String name) {
    this(name, ObjCMethodType.INSTANCE, "void");
    this.name = name;
  }

  public ObjCMethodImplementationSourcePart(String name, ObjCMethodType methodType, String returnType) {
    this.methodType = methodType;
    this.name = name;
    this.returnType = returnType;
  }

  /**
   * Adds parameter to the list of parameters
   * @param type parameter type
   * @param name parameter name
   */
  public void addParameter(String type, String name) {
    parameters.add(new AbstractMap.SimpleEntry<String, String>(type, name));
  }

  @Override
  public String asString() {
    StringBuilder bld = new StringBuilder();
    bld.append(methodType == ObjCMethodType.CLASS ? "+" : "-");
    bld.append("(").append(returnType).append(")");
    bld.append(name);
    for (Map.Entry<String, String> parameter : parameters) {
      bld.append(":(");
      bld.append(parameter.getKey());
      bld.append(")");
      bld.append(parameter.getValue());
      bld.append(" ");
    }
    bld.append(" {\n");
    bld.append(super.asString());
    bld.append("\n}");
    return bld.toString();
  }
}
