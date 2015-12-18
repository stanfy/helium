package com.stanfy.helium.handler.codegen.objectivec.entity;

import com.stanfy.helium.handler.codegen.GeneratorOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCType

/**
 * Options for a handler that generated Obj-C entities.
 */
public class ObjCEntitiesOptions : GeneratorOptions() {

  /** Class names prefix. */
  public var prefix = "HE";

  /**
   * Map that contains mappings for custom Helium Types. i.e. timestamp -> NSDate.
   * It is used for generating custom(complex) types.
   */
  public var customTypesMappings = mapOf<String, String>()

}
