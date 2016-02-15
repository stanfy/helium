package com.stanfy.helium.handler.codegen.objectivec.entity;

import com.stanfy.helium.handler.codegen.GeneratorOptions


public enum class ObjCMappingOption {
  NONE,
  MANTLE,
  SFMAPPING
}

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

  /**
   * Map that contains custom value transformers for Mantle entities generators
   * This is useful, when JSON have some strange format, and we cannot be sure about it
   * structure, so custom transformers are needed for class
   * This is only for mantle mappings
   */
  public var mantleCustomValueTransformers = mapOf<String, String>()

  /**
   * Setting that allows to choose which mappings to generate
   * Default value is none, some general mappers can be used
   */
  public var mappingsType : ObjCMappingOption = ObjCMappingOption.NONE

}
