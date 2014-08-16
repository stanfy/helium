package com.stanfy.helium.handler.codegen.objectivec;

/**
 * Created by ptaykalo on 8/17/14.
 */
public class ObjCImplementationFile extends ObjCFile {

  public ObjCImplementationFile(String name) {
    super(name);
  }

  @Override
  String getExtension() {
    return "m";
  }
}
