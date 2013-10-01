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

  String getUriQueryWithExamples(final String encoding) {
    if (!parameters) { return "" }
    StringBuilder res = new StringBuilder()
    boolean noData = false
    parameters.fields.each { Field field ->
      if (noData) { return }
      if (field.type instanceof Message || field.type instanceof Sequence) {
        throw new IllegalStateException("Type $field.type is not allowed in parameters")
      }
      if (!field.required && !field.examples) { return }
      if (!field.examples) {
        noData = true
        return
      }
      String name = field.name
      String value = field.examples[0]
      res << "$name=${URLEncoder.encode(value, encoding)}&"
    }
    if (noData) { return "" }
    if (res.length()) {
      res.delete(res.length() - 1, res.length())
    }
    return "?$res"
  }

  boolean hasBody() {
    return type.hasBody && body != null
  }

}
