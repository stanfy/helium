package com.stanfy.helium.handler.codegen.objectivec.entity.classtree


/**
 * Created by paultaykalo on 12/17/15.
 */

class ObjCProjectClassesTree {
  /**
   * Classes that this project contains
   */
  val classes = arrayListOf<ObjCClass>()
  /**
   * Classes that this project contains
   */
  val pregeneratedClasses = arrayListOf<ObjCPregeneratedClass>()

  /**
   * Holds mapping form DSL Type names to classes, that this project contains
   */
  val classesByTypes = hashMapOf<String, ObjCClass>()

  fun addClass(objCClass: ObjCClass) {
    classes.add(objCClass);
  }

  /**
   * Adds class, and bounds it to the specified DSL Type
   */
  fun addClass(objCClass: ObjCClass, dslType: String) {
    classes.add(objCClass);
    classesByTypes.put(dslType, objCClass);
  }

  /**
   * Adds class, which cannot be actually queried, and contains only
   * string, pregenerated information information
   */
  fun addSourceCodeClass(objCClass: ObjCPregeneratedClass) {
    pregeneratedClasses.add(objCClass);
  }

  fun getClassForType(dslTypeName: String): ObjCClass? {
    return classesByTypes.get(dslTypeName);
  }

}