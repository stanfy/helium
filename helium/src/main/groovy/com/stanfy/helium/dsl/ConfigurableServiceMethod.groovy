package com.stanfy.helium.dsl

import com.stanfy.helium.model.HttpHeader
import com.stanfy.helium.model.Message
import com.stanfy.helium.utils.ConfigurableProxy
import com.stanfy.helium.model.ServiceMethod

import static com.stanfy.helium.utils.DslUtils.runWithProxy

/**
 * Extended proxy for ServiceMethod.
 */
class ConfigurableServiceMethod extends ConfigurableProxy<ServiceMethod> {

  static {
    ["parameters", "response", "body"].each {
      ConfigurableServiceMethod.metaClass."$it" << { Object arg ->
        if (arg instanceof Closure<?>) {
          delegate.defineMessageType(it, (Closure<?>)arg)
        } else if (arg instanceof String) {
          delegate.defineMessageType(it, (String)arg)
        }
      }
    }
  }

  ConfigurableServiceMethod(final ServiceMethod core, final ProjectDsl project) {
    super(core, project)
  }

  void defineMessageType(final String property, Closure<?> body) {
    ServiceMethod core = getCore()
    Message message = getProject().createAndAddMessage("${core.canonicalName}_${property}_${core.type}", body, false)
    message.anonymous = true
    core."$property" = message
  }

  void defineMessageType(final String property, String messageType) {
    ServiceMethod core = getCore()
    core."$property" = getProject().types.byName(messageType)
  }

  void tests(final Closure<?> spec) {
    runWithProxy(new ConfigurableMethodTestsInfo(getCore().testInfo, getProject()), spec)
  }

  @SuppressWarnings("GrMethodMayBeStatic")
  HttpHeader header(final Map<String, ?> map) {
    return new HttpHeader(map)
  }

  HttpHeader header(final Map<String, String> map, final String name) {
    HttpHeader header = header(map)
    header.name = name
    return header
  }

  HttpHeader header(final Map<String, String> map, final String name, final String value) {
    HttpHeader header = header(map, name)
    header.value = value
    return header
  }

  HttpHeader header(final String name) {
    return header(Collections.emptyMap(), name)
  }

  HttpHeader header(final String name, final String value) {
    return header(Collections.emptyMap(), name, value)
  }

  void httpHeaders(final Object... args) {
    if (!args.length) {
      throw new IllegalArgumentException("No headers specified")
    }
    ArrayList<HttpHeader> headers = new ArrayList<>(args.length)
    for (Object arg in args) {
      if (arg instanceof String || arg instanceof GString) {
        headers.add header(arg as String)
      } else if (arg instanceof HttpHeader) {
        headers.add arg as HttpHeader
      } else {
        throw new IllegalArgumentException("Cannot cast $arg of type ${arg.getClass()} to HttpHeader")
      }
    }
    ServiceMethod core = getCore()
    core.httpHeaders.addAll(headers)
  }

}
