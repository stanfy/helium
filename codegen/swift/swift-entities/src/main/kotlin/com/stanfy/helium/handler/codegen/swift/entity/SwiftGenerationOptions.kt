package com.stanfy.helium.handler.codegen.swift.entity

import com.stanfy.helium.handler.codegen.GeneratorOptions

enum class SwiftEntitiesAccessLevel {
  INTERNAL,
  PUBLIC,
}


class SwiftGenerationOptions : GeneratorOptions () {

  /**
   * Map that contains mappings for custom Helium Types. i.e. timestamp -> NSDate.
   * It is used for generating custom(complex) types.
   */
  var customTypesMappings = mapOf<String, String>()

  /**
   * Map that contains default values for specific types
   * For optional values of this types, in case if there's no value default value will be supplied
   */
  var typeDefaultValues = mapOf<String, String>()

  /**
   * Specifies what visibility generated entities should have.
   * Default value is INTERNAL
   */
  var entitiesAccessLevel = SwiftEntitiesAccessLevel.INTERNAL

}