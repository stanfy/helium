package com.stanfy.helium.dsl

import com.stanfy.helium.model.Type
import groovy.transform.CompileStatic

/**
 * Configurable wrapper.
 * @param <T> wrapped instance type.
 */
class ConfigurableProxy<T extends GroovyObject> {

  /** DSL instance. */
  final ProjectDsl project

  /** Core object. */
  final T core

  @CompileStatic
  public ConfigurableProxy(final T core, final ProjectDsl project) {
    this.core = core
    this.project = project
  }

  @Override
  def getProperty(final String name) {
    return core.getProperty(name)
  }

  @Override
  Object invokeMethod(final String name, final Object args) {
    try {
      return metaClass.invokeMethod(this, name, args)
    } catch (MissingMethodException e) {
      if (name == e.method && core.hasProperty(name)) {

        final Object argument
        if (args.getClass().isArray()) {
          if (args.length != 1) { throw new IllegalArgumentException("Bad arguments $args for property $name") }
          argument = args[0]
        } else {
          argument = args
        }

        try {
          core."$name" = argument
        } catch (ClassCastException castError) {
          if (name.equalsIgnoreCase('type')) {
            final Type type = argument instanceof Class ? project.typeResolver.byGroovyClass(argument) : project.typeResolver.byName("$argument")
            try {
              core."$name" = type
            } catch (Exception typeError) {
              throw e
            }
          } else {
            throw e
          }
        }

        return core."$name"
      }
      throw e
    }
  }

}
