package com.stanfy.helium.handler.codegen.objectivec.entity.filetree;

/**
 * Created by ptaykalo on 8/17/14.
 * Rperensents Implementation filetree (.m) with the Objective-C source
 */

public class ObjCImplementationFile(name: String, contents: String) : ObjCFile(name, contents) {
  public constructor(name: String) : this(name, "") {
  }

  override fun getExtension(): String {
    return "m"
  }

}
