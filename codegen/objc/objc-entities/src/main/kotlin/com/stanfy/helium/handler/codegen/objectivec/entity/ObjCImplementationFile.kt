package com.stanfy.helium.handler.codegen.objectivec.entity;

import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCFile

/**
 * Created by ptaykalo on 8/17/14.
 * Rperensents Implementation file (.m) with the Objective-C source
 */
public class ObjCImplementationFile(name: String) : ObjCFile(name) {
  override fun getExtension(): String {
    return "m"
  }

}
