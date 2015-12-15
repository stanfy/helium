package com.stanfy.helium.handler.codegen.objectivec.entity.file;

import java.util.ArrayList;

/**
 * Created by ptaykalo on 8/17/14.
 * Simple block that know how to serialize ObjC Class Implementation
 */
public class ObjCClassImplementation implements ObjCSourcePart {

  /*
  Class Name
   */
  private String className;
  private ArrayList<ObjCSourcePart> importSourceParts =  new ArrayList<ObjCSourcePart>();
  private ArrayList<ObjCSourcePart> bodySourceParts =  new ArrayList<ObjCSourcePart>();

  public ObjCClassImplementation(final String className) {
    this.className = className;
  }

  public String getClassName() {
    return className;
  }

  /**
  Adds specified source part to the top part (before @implementation)
   */
  public void addImportSourcePart(final ObjCSourcePart sourcePart) {
    importSourceParts.add(sourcePart);
  }
  /**
   Adds specified source part to the central part (inside @implementation)
   */
  public void addBodySourcePart(final ObjCSourcePart sourcePart) {
    bodySourceParts.add(sourcePart);
  }

  @Override
  public String asString() {
    // TODO use some templates
    StringBuilder bld = new StringBuilder();
    bld.append("#import \"").append(className).append(".h\"\n");
    for (ObjCSourcePart sourcePart : importSourceParts) {
      bld.append(sourcePart.asString()).append("\n");
    }
    bld.append("@implementation ").append(className).append("\n");
    for (ObjCSourcePart sourcePart : bodySourceParts) {
      bld.append(sourcePart.asString()).append("\n");
    }
    bld.append("@end");
    return bld.toString();

  }
}
