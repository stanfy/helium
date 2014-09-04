package com.stanfy.helium.handler.codegen.objectivec;

/**
 * Created by ptaykalo on 8/17/14.
 * Rperensents Implementation file (.m) with the Objective-C source
 */
public class ObjCImplementationFile extends ObjCFile {

  public ObjCImplementationFile(final String name) {
    super(name);
  }

  @Override
  public String getExtension() {
    return "m";
  }
}
