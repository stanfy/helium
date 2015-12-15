package com.stanfy.helium.handler.codegen.objectivec.entity.file;

/**
 * Created by ptaykalo on 9/10/14.
 * Source part that represents import line
 */
public class ObjCImportPart implements ObjCSourcePart {
  private String filename;

  public ObjCImportPart(final String filename) {
    this.filename = filename;
  }

  @Override
  public String asString() {
    return "#import \"" + filename + ".h\"";
  }

  public String getFilename() {
    return filename;
  }
}
