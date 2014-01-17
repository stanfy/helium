package com.stanfy.helium.dsl

import com.stanfy.helium.utils.ConfigurableProxy
import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.TypeResolver
import groovy.transform.CompileStatic

import static com.stanfy.helium.utils.DslUtils.runWithProxy

/**
 * Builder for fields.
 */
class FieldsBuilder {

  /** Type for skipped fields. */
  private static final Type IGNORABLE_TYPE = new Type(name: "_helium_ignorable_")

  /** Result message. */
  private final Message message

  /** Type resolver. */
  private final ProjectDsl project

  /** Type resolver. */
  private final TypeResolver typeResolver

  @CompileStatic
  FieldsBuilder(final Message message, final ProjectDsl project, final TypeResolver typeResolver) {
    this.message = message
    this.project = project
    this.typeResolver = typeResolver
  }

  @Override
  def invokeMethod(final String name, final Object args) {
    Object arg = args
    if (args.getClass().isArray()) {
      Object[] arr = args as Object[]
      if (arr.length > 1) {
        throw new IllegalArgumentException("Bad argument for building field $name: $args")
      }
      arg = arr[0]
    }

    if (arg instanceof Closure) {
      // just configure
      Field f = new Field()
      runWithProxy(new ConfigurableProxy<Field>(f, project), (Closure<?>)arg)
      f.name = name
      message.addField(f)
      return f
    }

    if (arg instanceof Map) {
      if (arg.skip) {
        Field f = new Field(name: name, type: IGNORABLE_TYPE, skip: true)
        message.addField(f)
        return f
      }
      Type type = resolveType(arg['type'])
      arg.type = type
      Field f = new Field(arg)
      f.name = name
      message.addField(f)
      return f
    }

    // treat parameter as a type
    Field field = new Field(name : name, type : resolveType(arg))
    message.addField(field)
    return new OptionalFieldTrigger(field : field)
  }

  @CompileStatic
  private Type resolveType(final Object arg) {
    if (arg == null) {
      throw new IllegalArgumentException("Type is not specified");
    }

    if (arg instanceof Type) {
      return (Type)arg
    }

    final Type type
    if (arg instanceof Class) {
      type = typeResolver.byGroovyClass((Class<?>)arg)
    } else {
      type = typeResolver.byName("$arg")
    }
    return type
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
