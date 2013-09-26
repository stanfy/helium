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
    if (!location) { throw new IllegalStateException("Service location is not specified") }
    String loc = location.endsWith('/') ? location[0..-2] : location
    if (!method.path) { throw new IllegalStateException("Service method path is not specified") }
    String path = method.path.startsWith('/') ? method.path[1..-1] : method.path
    return "$loc/$path"
  }

}
