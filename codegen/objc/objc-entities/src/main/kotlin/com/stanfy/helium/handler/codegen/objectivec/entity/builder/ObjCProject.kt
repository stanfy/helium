package com.stanfy.helium.handler.codegen.objectivec.entity;

import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCFile
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCClass;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ptaykalo on 8/17/14.
 * Class that represents structure of objective C Project
 */
public class ObjCProject {

  /**
   * Files those are contained in this project. Each should have an unique name
   */
  public var files = ArrayList<ObjCFile>();
  /**
   * Classes that this project contains
   */
  public var classes = ArrayList<ObjCClass>()
  /**
   * Holds mapping form DSL Type names to classes, that this project contains
   */
  public var classesByTypes = HashMap<String, ObjCClass>()


  public fun addFile(file: ObjCFile) {
    files.add(file);
  }

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
