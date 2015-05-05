package com.stanfy.helium.internal.dsl

import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.TypeResolver
import groovy.transform.CompileStatic

import static com.stanfy.helium.utils.DslUtils.runWithProxy

/**
 * Builder for fields.
 */
class FieldsBuilder extends TypeAwareBuilder {

  /** Type for skipped fields. */
  private static final Type IGNORABLE_TYPE = new Type(name: "_helium_ignorable_")

  /** Result message. */
  private final Message message

  /** Type resolver. */
  private final ProjectDsl project


  FieldsBuilder(final Message message, final ProjectDsl project, final TypeResolver typeResolver) {
    super(typeResolver)
    this.message = message
    this.project = project
  }

  @Override
  def invokeMethod(final String name, final Object args) {
    Map map = null
    Closure<?> spec = null
    Object arg = args

    if (args.getClass().isArray()) {
      Object[] arr = args as Object[]
      if (arr.length > 1) {
        if (arr[0] instanceof Map && arr[1] instanceof Closure) {
          map = arr[0] as Map
          spec = arr[1] as Closure<?>
          arg = null
        } else {
          throw new IllegalArgumentException("Bad argument for building field $name: $args")
        }
      } else {
        if (arr[0] instanceof Map) {
          map = arr[0] as Map
          arg = null
        } else if (arr[0] instanceof Closure<?>) {
          spec = arr[0] as Closure<?>
          arg = null
        } else {
          arg = arr[0]
        }
      }
    }

    Field f = null;

    // configure with map
    if (map != null) {
      if (map.skip) {
        f = new Field(name: name, type: IGNORABLE_TYPE, skip: true)
        message.addField(f)
        return f
      }

      Type type = resolveType(map['type'])
      map.type = type
      f = new Field(map)
      f.name = name
      message.addField(f)
    }

    // configure with closure
    if (spec != null) {
      if (f == null) {
        f = new Field()
        message.addField(f)
      }
      def fieldDsl = new ConfigurableField(f, project)
      runWithProxy(fieldDsl, spec)
      fieldDsl.resolveConstraints(message)
      f.name = name
    }

    if (f != null) {
      return f
    }

    if (arg == null) {
      throw new IllegalArgumentException("Bad syntax for field description: $args")
    }

    // treat parameter as a type
    f = new Field(name : name, type : resolveType(arg))
    message.addField(f)
    return new OptionalFieldTrigger(field : f)
  }


  @CompileStatic
  class OptionalFieldTrigger {
    Field field

    boolean getRequired() {
      field.required = true
    }
    boolean getOptional() {
      field.required = false
    }

    boolean getSequence() {
      field.sequence = true
      field.required = false
    }

    void setRequired(boolean value) {
      field.required = value
    }

    void setSequence(boolean value) {
      field.sequence = value
    }
  }

}
