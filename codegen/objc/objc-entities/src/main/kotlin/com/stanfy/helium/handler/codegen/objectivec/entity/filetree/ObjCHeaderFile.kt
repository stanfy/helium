package com.stanfy.helium.handler.codegen.objectivec.entity.filetree;

import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCFile

/**
 * Created by ptaykalo on 8/17/14.
 * Class that represents objective-C header filetree
 * Will always have .h extension
 */
public class ObjCHeaderFile(name: String, contents: String) : ObjCFile(name, contents) {

  public constructor(name: String) : this(name, "") {
  }

  override fun getExtension(): String {
    return "h";
  }
}