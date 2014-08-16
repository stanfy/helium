package com.stanfy.helium.handler.codegen.objectivec;

/**
 * Created by ptaykalo on 8/17/14.
 * Structure that represent Objective-C source file
 * Concrete implementations can be header, and implementation file
 */
abstract public class ObjCFile {

  private String name;

  public ObjCFile(final String name) {
    this.name = name;
  }

  /*
  Returns file name
   */
  public String getName() { return name; }

  /*
  File extension :)
   */
  abstract String getExtension();

}
