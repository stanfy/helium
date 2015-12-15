package com.stanfy.helium.internal.dsl

import com.stanfy.helium.model.DataType
import com.stanfy.helium.model.FileType
import com.stanfy.helium.model.MultipartType
import com.stanfy.helium.model.TypeResolver
import groovy.transform.CompileStatic

/**
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
@CompileStatic
class MultipartBuilder extends TypeAwareBuilder {

  MultipartType multipartType

  MultipartBuilder(final MultipartType multipartType, final TypeResolver typeResolver) {
    super(typeResolver)
    this.@multipartType = multipartType
  }

  @Override
  def invokeMethod(final String name, final Object args) {
    if (args.getClass().isArray()) {
      // When using file() or data() wrap in corresponding type.
      def arr = args as Object[]
      if (arr.length == 0) {
        switch (name) {
          case 'file':
            return new FileType()
          case 'data':
            return new DataType()
        }
      } else if (arr.length == 1) {
        multipartType.parts.put(name, resolveType(arr[0]))
      }
    }
  }

}
