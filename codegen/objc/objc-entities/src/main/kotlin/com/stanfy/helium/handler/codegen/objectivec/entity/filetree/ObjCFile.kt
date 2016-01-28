package com.stanfy.helium.handler.codegen.objectivec.entity.filetree

/**
 * Created by ptaykalo on 8/17/14.
 * Structure that represent Objective-C source filetree
 * Concrete implementations can be header, and implementation filetree
 */
public abstract class ObjCFile(val name: String, val contents: String) {

  public constructor(name: String) : this(name, "")

  /**
   * File extension :)
   */
  public abstract fun getExtension(): String

}


public class ObjCHeaderFile(name: String, contents: String) : ObjCFile(name, contents) {
  override fun getExtension(): String {
    return "h";
  }
}

public class ObjCImplementationFile(name: String, contents: String) : ObjCFile(name, contents) {
  override fun getExtension(): String {
    return "m"
  }
}

