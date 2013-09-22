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
  Message response

  /** Request body. */
  Message body

}
