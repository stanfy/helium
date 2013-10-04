package com.stanfy.helium.model

import com.stanfy.helium.model.tests.MethodTestInfo
import com.stanfy.helium.model.tests.ServiceTestInfo
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

  /** Tests info. */
  final ServiceTestInfo testInfo = new ServiceTestInfo()

  String getCanonicalName() {
    return name?.replaceAll(/\W+/, '')
  }

  String getMethodUri(final MethodTestInfo testInfo, final ServiceMethod method) {
    Map<String, String> parameters = testInfo?.useExamples ? testInfo?.pathExample : null
    return getMethodUri(method, parameters)
  }

  String getMethodUri(final ServiceMethod method, final Map<String, String> parameters) {
    if (!method.path) { throw new IllegalStateException("Service method path is not specified") }
    String path = method.path
    if (parameters) {
      path = method.getPathWithParameters(parameters)
    }
    path = path.startsWith('/') ? path[1..-1] : path

    if (!location) { throw new IllegalStateException("Service location is not specified") }
    String loc = location.endsWith('/') ? location[0..-2] : location
    return "$loc/$path"
  }

}
