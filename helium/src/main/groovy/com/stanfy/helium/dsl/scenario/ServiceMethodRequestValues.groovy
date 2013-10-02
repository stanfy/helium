package com.stanfy.helium.dsl.scenario

import com.stanfy.helium.entities.TypedEntity
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

/**
 * All the values required for service method execution.
 */
@CompileStatic
@PackageScope([PackageScopeTarget.METHODS])
class ServiceMethodRequestValues {

  /** Request body. */
  final TypedEntity body

  /** Request parameters. */
  final TypedEntity parameters

  /** Request path parameters. */
  final Map<String, String> pathParameters

  /** HTTP headers. */
  final Map<String, String> httpHeaders

  ServiceMethodRequestValues(final TypedEntity body, final TypedEntity parameters,
                             final Map<String, String> pathParameters, final Map<String, String> httpHeaders) {
    this.body = body
    this.parameters = parameters
    this.pathParameters = pathParameters
    this.httpHeaders = httpHeaders
  }

}
