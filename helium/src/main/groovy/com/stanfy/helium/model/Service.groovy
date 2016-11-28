package com.stanfy.helium.model

import com.stanfy.helium.internal.MethodsExecutor
import com.stanfy.helium.model.tests.BehaviourSuite
import com.stanfy.helium.model.tests.CheckListener
import com.stanfy.helium.model.tests.MethodTestInfo
import com.stanfy.helium.model.tests.ServiceTestInfo
import groovy.transform.CompileStatic

/**
 * Service entity.
 */
@CompileStatic
class Service extends Descriptionable implements StructureUnit, Checkable {

  /** Version name. */
  String version

  /** Service location (base URL/path). */
  String location

  /** Encoding used. */
  String encoding

  /** Possible authentication schemes. */
  List<Authentication> authentications = new ArrayList<>()

  /** Service methods. */
  final List<ServiceMethod> methods = new ArrayList<>()

  /** Tests info. */
  final ServiceTestInfo testInfo = new ServiceTestInfo()

  String getCanonicalName() {
    if (!name) {
      return null;
    }
    return super.getCanonicalName();
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
    path = path.startsWith('/') ? path.substring(1) : path

    if (!location) { throw new IllegalStateException("Service location is not specified") }
    String loc = location.endsWith('/') ? location.substring(0, location.length() - 1) : location
    return "$loc/$path"
  }

  @Override
  BehaviourSuite check(final MethodsExecutor executor, final CheckListener listener) {
    return BehaviourSuite.EMPTY
  }

  List<Authentication.Type> authenticationTypes() {
    return authentications.collect { it.type }
  }

}
