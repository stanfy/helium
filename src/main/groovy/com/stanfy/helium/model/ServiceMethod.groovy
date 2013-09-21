package com.stanfy.helium.model

/**
 * Method that can invoked on a service.
 */
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
