package com.stanfy.helium.handler.codegen.objectivec.entity.file;

/**
 * Created by ptaykalo on 9/10/14.
 * Simplies objc Source part, which  simply returns contents
 */
public class ObjCStringSourcePart implements ObjCSourcePart {

  private String contents = "";

  public ObjCStringSourcePart(final String contents) {
    this.contents = contents;
  }

  @Override
  public String asString() {
    return contents;
  }
}
