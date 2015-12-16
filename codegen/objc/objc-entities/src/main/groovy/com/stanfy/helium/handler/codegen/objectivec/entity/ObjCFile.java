package com.stanfy.helium.handler.codegen.objectivec.entity;
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCSourcePart;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ptaykalo on 8/17/14.
 * Structure that represent Objective-C source file
 * Concrete implementations can be header, and implementation file
 */
public abstract class ObjCFile implements ObjCSourcePart {

  private String name;
  private ArrayList<ObjCSourcePart> sourceParts = new ArrayList<ObjCSourcePart>();

  public ObjCFile(final String name) {
    this.name = name;
  }

  /**
   * Returns file name
   */
  public String getName() {
    return name;
  }

  /**
   * File extension :)
   */
  public abstract String getExtension();


  public List<ObjCSourcePart> getSourceParts() {
    return sourceParts;
  }

  public void addSourcePart(final ObjCSourcePart sourcePart) {
    sourceParts.add(sourcePart);
  }

  @Override
  public String asString() {
    StringBuilder bld = new StringBuilder();
    for (ObjCSourcePart part : sourceParts) {
      bld.append(part.asString());
    }
    return bld.toString();
  }
}
