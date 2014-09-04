package com.stanfy.helium.handler.codegen.objectivec;

/**
 * Created by ptaykalo on 8/17/14.
 * Class that represents objective-C header file
 * Will always have .h extension
 */
public class ObjCHeaderFile extends ObjCFile {

  public ObjCHeaderFile(final String name) {
    super(name);
  }

  @Override
  public String getExtension() {
    return "h";
  }
}
