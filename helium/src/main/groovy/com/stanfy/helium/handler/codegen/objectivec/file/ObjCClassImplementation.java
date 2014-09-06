package com.stanfy.helium.handler.codegen.objectivec.file;

/**
 * Created by ptaykalo on 8/17/14.
 * Simple block that know how to serialize ObjC Class Implementation
 */
public class ObjCClassImplementation implements ObjCSourcePart {

  /*
  Class Name
   */
  private String className;

  public ObjCClassImplementation(final String className) {
    this.className = className;
  }


  public String getClassName() {
    return className;
  }

  @Override
  public String asString() {
    // TODO use some templates
    StringBuilder bld = new StringBuilder();
    bld.append("#import \"").append(className).append(".h\"\n");
    bld.append("@implementation ").append(className).append("\n");
    bld.append("@end");
    return bld.toString();

  }
}
