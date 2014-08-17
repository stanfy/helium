package com.stanfy.helium.handler.codegen.objectivec.file;

/**
 * Created by ptaykalo on 8/17/14.
 */
public class ObjCClassDefinition {
  /*
Class Name
 */
  private String className;

  public ObjCClassDefinition(final String className) {
    this.className = className;
  }

  public String getClassName() {
    return className;
  }
}
