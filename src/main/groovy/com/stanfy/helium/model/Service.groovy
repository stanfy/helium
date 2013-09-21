package com.stanfy.helium.model

/**
 * Service entity.
 */
class Service extends Descriptionable {

  /** Version name. */
  String version

  /** Service location (base URL/path). */
  String location

  /** Service methods. */
  final List<ServiceMethod> methods = new ArrayList<>()

}
