package com.stanfy.helium.dsl

import com.stanfy.helium.entities.json.ClosureJsonConverter
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.constraints.ConstrainedType
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import static com.stanfy.helium.utils.DslUtils.runWithProxy

/**
 * DSL for type declarations.
 */
@CompileStatic
class TypeDsl {

  /** Pending type. */
  private final Type type

  /** Project DSL instance. */
  private final ProjectDsl dsl

  private TypeDsl(final Type type, final ProjectDsl dsl) {
    this.type = type
    this.dsl = dsl
  }

  @PackageScope
  static TypeDsl create(final Type type, final ProjectDsl dsl) {
    return new TypeDsl(type, dsl)
  }

  Message message(final Closure<?> spec) {
    return message(Collections.emptyMap(), spec)
  }

  Message message(Map args, final Closure<?> spec) {
    Message msg = dsl.createAndAddMessage(type.name, spec, true)
    if (args.containsKey("skipUnknownFields")) {
      msg.skipUnknownFields = args.skipUnknownFields
    }
    return msg
  }

  Message message(Map args) {
    return message(args, { })
  }

  Sequence sequence(final String item) {
    return dsl.createAndAddSequence(type.name, item)
  }

  void spec(final Closure<?> spec) {
    ConfigurableType proxy = new ConfigurableType(type, dsl)
    runWithProxy(proxy, spec)

    // register custom converters
    Set<String> formats = new HashSet<>()
    formats.addAll(proxy.@readers.keySet())
    formats.addAll(proxy.@writers.keySet())
    formats.each { String format ->
      def reader = proxy.@readers[format], writer = proxy.@writers[format]
      if (!reader) { reader = ClosureJsonConverter.AS_STRING_READER }
      if (!writer) { writer = ClosureJsonConverter.AS_STRING_WRITER }
      dsl.typeResolver.findConverters(format).addConverter(
          type.name, new ClosureJsonConverter(type, reader, writer)
      )
    }

    // turn into constrained type
    if (proxy.getBaseTypeName() && proxy.getConstraints()) {
      ConstrainedType type = new ConstrainedType(dsl.typeResolver.byName(proxy.getBaseTypeName()))
      type.addConstraints(proxy.getConstraints())
      type.name = this.type.name
      type.description = this.type.description
      dsl.updatePrimitiveType(type)
    }
  }

}
