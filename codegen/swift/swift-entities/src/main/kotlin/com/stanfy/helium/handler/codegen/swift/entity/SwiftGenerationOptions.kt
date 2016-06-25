package com.stanfy.helium.handler.codegen.swift.entity

import com.stanfy.helium.handler.codegen.GeneratorOptions

class SwiftGenerationOptions : GeneratorOptions () {

  /**
   * Map that contains mappings for custom Helium Types. i.e. timestamp -> NSDate.
   * It is used for generating custom(complex) types.
   */
  var customTypesMappings = mapOf<String, String>()

}