package com.stanfy.helium.handler.codegen.objectivec.entity;

import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCFile

/**
 * Created by ptaykalo on 8/17/14.
 * Class that represents objective-C header file
 * Will always have .h extension
 */
public class ObjCHeaderFile(name: String) : ObjCFile(name) {
  override fun getExtension(): String {
    return "h";
  }
}
