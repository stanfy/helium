package com.stanfy.helium.handler.codegen.objectivec.entity.file;

import java.util.ArrayList;

/**
 * Created by ptaykalo on 8/17/14.
 * Simple block that know how to serialize ObjC Class Implementation
 */
public class ObjCImplementationFileSourcePart implements ObjCSourcePart {

  /**
   * Class Name
   */
  private String filename;
  private ArrayList<ObjCImportPart> importSourceParts =  new ArrayList<ObjCImportPart>();
  private ArrayList<ObjCSourcePart> bodySourceParts =  new ArrayList<ObjCSourcePart>();

  public ObjCImplementationFileSourcePart(final String filename) {
    this.filename = filename;
  }

  public String getFilename() {
    return filename;
  }

  /**
  Adds specified source part to the top part (before @implementation)
   */
  public void addImportSourcePart(final ObjCImportPart sourcePart) {
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
    bld.append("#import \"").append(filename).append(".h\"\n");
    for (ObjCSourcePart sourcePart : importSourceParts) {
      bld.append(sourcePart.asString()).append("\n");
    }
    bld.append("@implementation ").append(filename).append("\n");
    for (ObjCSourcePart sourcePart : bodySourceParts) {
      bld.append(sourcePart.asString()).append("\n");
    }
    bld.append("@end");
    return bld.toString();

  }
}
