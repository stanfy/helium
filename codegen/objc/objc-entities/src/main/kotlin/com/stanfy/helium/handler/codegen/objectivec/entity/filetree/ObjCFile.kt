package com.stanfy.helium.handler.codegen.objectivec.entity.filetree

/**
 * Created by ptaykalo on 8/17/14.
 * Structure that represent Objective-C source filetree
 * Concrete implementations can be header, and implementation filetree
 */
abstract class ObjCFile(val name: String, val contents: String) {

  constructor(name: String) : this(name, "")

  /**
   * File extension :)
   */
  abstract fun getExtension(): String

}


class ObjCHeaderFile(name: String, contents: String) : ObjCFile(name, contents) {

  constructor(name: String) : this(name, "")

  override fun getExtension(): String {
    return "h";
  }
}

class ObjCImplementationFile(name: String, contents: String) : ObjCFile(name, contents) {
  constructor(name: String) : this(name, "")

  override fun getExtension(): String {
    return "m"
  }
}

