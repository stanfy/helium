package com.stanfy.helium.handler.codegen.objectivec.entity.model

/**
 * Created by paultaykalo on 2/12/16.
 * ObjcClass model representation
 */
class ObjCClass(val name: String, val superClass:ObjCClass? = null ) {

  /**
   * Array of object properties
   */
  val properties = arrayListOf<ObjCProperty>()

}

