package com.stanfy.helium.handler.codegen.objectivec.entity.filetree;

import java.util.ArrayList;

/**
 * Created by paultaykalo on 12/16/15.
 * Class that can have another source parts
 */
public open class ObjCSourcePartsContainer : ObjCSourcePart {

  public var sourceParts = ArrayList<ObjCSourcePart>()
    private set

  /**
   * Adds source part to current container
   * @param sourcePart source part that need to be rendered as a part of container
   */
  public fun addSourcePart(sourcePart: ObjCSourcePart) {
    sourceParts.add(sourcePart);
  }

  override fun asString(): String {
    val bld = StringBuilder();
    for (part in sourceParts) {
      bld.append(part.asString());
      bld.append("\n");
    }
    return bld.toString();
  }

}
