package com.stanfy.helium.handler.codegen.objectivec.entity.classtree

import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCClass

/**
 * Created by paultaykalo on 12/17/15.
 */

public class ObjCProjectClassesStructure {
  /**
   * Classes that this project contains
   */
  public var classes = arrayListOf<ObjCClass>()
    private set

  /**
   * Holds mapping form DSL Type names to classes, that this project contains
   */
  public var classesByTypes = hashMapOf<String, ObjCClass>()
    private set

  public fun addClass(objCClass: ObjCClass) {
    classes.add(objCClass);
  }

  /**
   * Adds class, and bounds it to the specified DSL Type
   */
  public fun addClass(objCClass: ObjCClass, dslType: String) {
    classes.add(objCClass);
    classesByTypes.put(dslType, objCClass);
  }

  public fun getClassForType(dslTypeName: String): ObjCClass? {
    return classesByTypes.get(dslTypeName);
  }

}