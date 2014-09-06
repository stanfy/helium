package com.stanfy.helium.handler.codegen.objectivec;

import com.stanfy.helium.handler.codegen.GeneratorOptions;

/**
 * Options for a handler that generated Obj-C entities.
 */
public class ObjcEntitiesOptions extends GeneratorOptions {

  /** Class names prefix. */
  private String prefix = "HE";

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  /** Returns prefix for all files those would be generated */
  public String getPrefix() {
    return prefix;
  }

}
