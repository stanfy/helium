package com.stanfy.helium.handler.codegen.objectivec.parser.options;

public class DefaultObjCProjectParserOptions implements ObjCProjectParserOptions {

  private String prefix = "HE";

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  @Override
  public String getPrefix() {
    return prefix;
  }
}
