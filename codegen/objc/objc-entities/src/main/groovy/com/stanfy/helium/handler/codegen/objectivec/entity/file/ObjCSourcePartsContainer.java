package com.stanfy.helium.handler.codegen.objectivec.entity.file;

import java.util.ArrayList;

/**
 * Created by paultaykalo on 12/16/15.
 * Class that can have another source parts
 */
public class ObjCSourcePartsContainer implements ObjCSourcePart {

  private ArrayList<ObjCSourcePart> sourceParts = new ArrayList<ObjCSourcePart>();

  public ArrayList<ObjCSourcePart> getSourceParts() {
    return sourceParts;
  }

  /**
   * Adds source part to current container
   * @param sourcePart source part that need to be rendered as a part of container
   */
  public void addSourcePart(ObjCSourcePart sourcePart) {
    sourceParts.add(sourcePart);
  }

  @Override
  public String asString() {
    StringBuilder bld = new StringBuilder();
    for (ObjCSourcePart part : getSourceParts()) {
      bld.append(part.asString());
      bld.append("\n");
    }
    return bld.toString();
  }
}
