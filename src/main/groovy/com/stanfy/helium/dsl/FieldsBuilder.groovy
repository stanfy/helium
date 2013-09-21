package com.stanfy.helium.dsl

import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type

/**
 * Builder for fields.
 */
class FieldsBuilder {

  /** Result message. */
  private final Message message

  /** Type resolver. */
  private final TypeResolver typeResolver

  FieldsBuilder(final Message message, final TypeResolver typeResolver) {
    this.message = message
    this.typeResolver = typeResolver
  }

  @Override
  def invokeMethod(final String name, final Object args) {
    Object arg = args
    if (args.getClass().isArray()) {
      if (args.length > 1) {
        throw new IllegalArgumentException("Bad argument for building field $name: $args")
      }
      arg = args[0]
    }

    if (arg instanceof Closure) {
      // just configure
      Field f = new Field()
      f.configure arg
      f.name = name
      message.fields.add f
      return f
    }

    if (arg instanceof Map) {
      Type type = resolveType(arg['type'])
      arg.type = type
      Field f = new Field(arg)
      f.name = name
      message.fields.add f
      return f
    }

    // treat parameter as a type
    Field field = new Field(name : name, type : resolveType(arg))
    message.fields.add field
    return new OptionalFieldTrigger(field : field)
  }

  private Type resolveType(final Object arg) {
    if (arg instanceof Type) { return arg }
    final Type type
    if (arg instanceof Class) {
      type = typeResolver.byGroovyClass(arg)
    } else {
      type = typeResolver.byName("$arg")
    }
    return type
  }

}

class OptionalFieldTrigger {
  Field field

  boolean getRequired() {
    field.required = true
  }
  boolean getOptional() {
    field.required = false
  }

  void setRequired(boolean value) {
    field.required = value
  }
}