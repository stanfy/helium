package com.stanfy.helium.model

import com.stanfy.helium.model.tests.MethodTestInfo
import groovy.transform.CompileStatic

import java.util.regex.Pattern

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

  /** Test information. */
  final MethodTestInfo testInfo = new MethodTestInfo()

  String getCanonicalName() {
    String res = path.replaceAll(/[\/.]+/, '_').replaceAll(/\W+/, '')
    return res[0] == '_' ? res[1..-1] : res
  }

  String getPathWithParameters(Map<String, String> parameters) {
    String res = path
    parameters.each { String name, String value ->
      res = res.replaceAll("@${Pattern.quote(name)}", value)
    }
    return res
  }

  boolean hasParametrizedPath() {
    return path.contains('@')
  }

}
