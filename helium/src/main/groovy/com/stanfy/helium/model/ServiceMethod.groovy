package com.stanfy.helium.model

import groovy.transform.CompileStatic

/**
 * Method that can invoked on a service.
 */
@CompileStatic
class ServiceMethod extends Descriptionable {

  /** Method path. */
  String path

  /** Method type: get, post, etc. */
  MethodType type

  /** Parameters. */
  Message parameters

  /** Response. */
  Type response

  /** Request body. */
  Type body

  /** Used encoding. */
  String encoding

  String getCanonicalName() {
    String res = path.replaceAll(/[\/.]+/, '_').replaceAll(/\W+/, '')
    return res[0] == '_' ? res[1..-1] : res
  }

}
