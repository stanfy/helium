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
    return getUriQueryWithResolver(encoding, { Field f ->
      if (!f.examples) { return null }
      return f.examples[0]
    })
  }
  String getUriQueryWithParameters(final String encoding, final Map<String, String> parameters) {
    return getUriQueryWithResolver(encoding, { Field f ->
      return parameters[f.name]
    })
  }

  private String getUriQueryWithResolver(final String encoding, final Closure<?> resolver) {
    if (!parameters) { return "" }
    StringBuilder res = new StringBuilder()
    boolean noData = false
    parameters.fields.each { Field field ->
      if (noData) { return }
      if (field.type instanceof Message || field.type instanceof Sequence) {
        throw new IllegalStateException("Type $field.type is not allowed in parameters")
      }
      String value = resolver(field)
      if (!value) {
        noData |= field.required
        return
      }
      String name = field.name
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
