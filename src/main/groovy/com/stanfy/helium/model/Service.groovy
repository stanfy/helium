package com.stanfy.helium.model

import groovy.transform.CompileStatic

/**
 * Service entity.
 */
@CompileStatic
class Service extends Descriptionable implements StructureUnit {

  /** Version name. */
  String version

  /** Service location (base URL/path). */
  String location

  /** Encoding used. */
  String encoding

  /** Service methods. */
  final List<ServiceMethod> methods = new ArrayList<>()

  String getCanonicalName() {
    return name?.replaceAll(/\W+/, '')
  }

  String getMethodUri(final ServiceMethod method) {
    return "$location/$method.path"
  }

}
