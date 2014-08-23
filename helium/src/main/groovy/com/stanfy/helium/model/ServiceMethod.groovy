package com.stanfy.helium.model

import com.stanfy.helium.model.tests.MethodTestInfo
import com.stanfy.helium.utils.Names
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

  /** HTTP headers. */
  final List<HttpHeader> httpHeaders = new ArrayList<>()

  @Override
  String getCanonicalName() {
    return Names.canonicalName(type.toString().toLowerCase(Locale.US) + " " + path)
  }

  void setPath(final String path) {
    this.@path = Names.rootPath(path)
  }

  String getPathWithParameters(Map<String, String> parameters) {
    String res = path
    parameters.each { String name, String value ->
      res = res.replaceAll("@${Pattern.quote(name)}", value)
    }
    return new URI("http", "host.com", res, null).toURL().getPath()
  }

  String getUriQueryWithExamples(final String encoding) {
    return getUriQueryWithResolver(encoding, { Field f ->
      if (!f.examples) { return null }
      return String.valueOf(f.examples[0])
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

  boolean hasRequiredParametersInPath() {
    return path?.contains("@")
  }

  boolean hasRequiredParameterFields() {
    return parameters?.hasRequiredFields()
  }

  boolean hasRequiredHeaders() {
    return httpHeaders.any { HttpHeader h -> !h.constant }
  }

  boolean hasRequiredParameters() {
    return hasRequiredParametersInPath() || hasRequiredParameterFields() || hasRequiredHeaders()
  }

  List<String> getPathParameters() {
    if (!hasRequiredParametersInPath()) {
      return []
    }
    return (path =~ /@(\w+)/).collect { List<String> it -> it[1] }
  }

  String toString() {
    return name ? "\"$name\"(type: $type path: $path)" : "\"type: $type path: $path\""
  }

  @Override
  boolean equals(o) {
    if (this.is(o)) {
      return true
    }
    if (getClass() != o.class) {
      return false
    }

    ServiceMethod that = (ServiceMethod) o

    if (path != that.path) {
      return false
    }
    if (type != that.type) {
      return false
    }

    return true
  }

  @Override
  int hashCode() {
    int result = path.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }
}
