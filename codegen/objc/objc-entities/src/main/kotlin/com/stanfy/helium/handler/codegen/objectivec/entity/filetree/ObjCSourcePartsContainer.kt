package com.stanfy.helium.handler.codegen.objectivec.entity.filetree;

/**
 * Created by paultaykalo on 12/16/15.
 * Class that can have another source parts
 */
open class ObjCSourcePartsContainer : ObjCSourcePart {

  val sourceParts = arrayListOf<ObjCSourcePart>()

  /**
   * Adds source part to current container
   * @param sourcePart source part that need to be rendered as a part of container
   */
  fun addSourcePart(sourcePart: ObjCSourcePart) {
    sourceParts.add(sourcePart);
  }

  /**
   * Adds string source part to current container
   * @param sourcePart source part that need to be rendered as a part of container
   */
  fun addSourcePart(sourcePart: String) {
    sourceParts.add(ObjCStringSourcePart(sourcePart));
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
