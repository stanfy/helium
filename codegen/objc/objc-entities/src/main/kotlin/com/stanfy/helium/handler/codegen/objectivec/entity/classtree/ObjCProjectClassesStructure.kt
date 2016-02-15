package com.stanfy.helium.handler.codegen.objectivec.entity.classtree


/**
 * Created by paultaykalo on 12/17/15.
 */

public class ObjCProjectClassesStructure {
  /**
   * Classes that this project contains
   */
  public val classes = arrayListOf<ObjCComplexClass>()
  /**
   * Classes that this project contains
   */
  public val pregeneratedClasses = arrayListOf<ObjCPregeneratedClass>()

  /**
   * Holds mapping form DSL Type names to classes, that this project contains
   */
  public val classesByTypes = hashMapOf<String, ObjCComplexClass>()

  public fun addClass(objCClass: ObjCComplexClass) {
    classes.add(objCClass);
  }

  /**
   * Adds class, and bounds it to the specified DSL Type
   */
  public fun addClass(objCClass: ObjCComplexClass, dslType: String) {
    classes.add(objCClass);
    classesByTypes.put(dslType, objCClass);
  }

  /**
   * Adds class, which cannot be actually queried, and contains only
   * string, pregenerated information information
   */
  public fun addSourceCodeClass(objCClass: ObjCPregeneratedClass) {
    pregeneratedClasses.add(objCClass);
  }

  public fun getClassForType(dslTypeName: String): ObjCComplexClass? {
    return classesByTypes.get(dslTypeName);
  }

}