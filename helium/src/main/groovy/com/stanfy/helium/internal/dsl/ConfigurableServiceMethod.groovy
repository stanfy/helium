package com.stanfy.helium.internal.dsl

import com.stanfy.helium.model.*
import com.stanfy.helium.utils.ConfigurableProxy

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
        if ("body" == it) {
          return [
              "multipart" : { Object partArg ->
                delegate.multipart(partArg)
              },
              "form" : { Object formArg ->
                delegate.form(formArg)
              },
              "data" : {
                delegate.data()
              }
          ]
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

  void form(final Object args) {
    Type typeToWrap = null
    ServiceMethod core = getCore()

    if (args instanceof String) {
      def messageType = args as String
      typeToWrap = getProject().types.byName(messageType)
    } else if (args instanceof Closure) {
      defineMessageType("body", args as Closure)
      typeToWrap = core.body
    }

    // Wrap message to get form body.
    if (typeToWrap == null) {
      throw new IllegalArgumentException("Bad arguments for form type: $args.")
    }

    if (typeToWrap instanceof Message) {
      core.body = new FormType(typeToWrap as Message)
    } else {
      throw new IllegalArgumentException("Bad arguments for form type: $args. Only message can be used as type for form")
    }

  }

  void multipart() {
    multipart(null, null)
  }

  void multipart(final Object args) {
    ServiceMethod core = getCore()
    if (!args) {
      core.body = new MultipartType()
      return
    }
    String contentType
    Closure<?> closure

    if (args.getClass().isArray()) {
      def arr = args as Object[]
      if (arr.length > 1) {
        if (isString((args)) && arr[1] instanceof Closure) {
          contentType = arr[0] as String
          closure = arr[1] as Closure<?>
        } else {
          throw new IllegalArgumentException("Bad arguments for multipart type: $args.")
        }

      }
    } else if (args instanceof Closure<?>) {
      closure = args as Closure<?>
      contentType = null
    } else if (isString(args)) {
      closure = null
      contentType = args as String
    }

    multipart(contentType, closure)
  }

  void multipart(final String contentType, final Closure<?> closure) {
    MultipartType multipartType = (contentType != null) ? new MultipartType(contentType) : new MultipartType()

    if (closure != null) {
      MultipartBuilder builder = new MultipartBuilder(multipartType, getProject().types)
      runWithProxy(builder, closure)
    }

    getCore().body = multipartType
  }

  private static boolean isString(args) {
    args instanceof String || args instanceof GString
  }

  void data() {
    getCore().body = new DataType()
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
