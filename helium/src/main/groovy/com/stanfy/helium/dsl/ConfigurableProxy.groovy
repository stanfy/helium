package com.stanfy.helium.dsl

import com.stanfy.helium.model.Type
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

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

  @CompileStatic
  @PackageScope
  static Object resolveSingleArgument(final String name, final Object args) {
    if (args.getClass().isArray()) {
      Object[] argArray = (Object[]) args
      if (argArray.length != 1) { throw new IllegalArgumentException("Expected one argument for $name") }
      return argArray[0]
    }
    return args
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

        final Object argument = resolveSingleArgument(name, args)

        try {
          core.setProperty(name, argument)
        } catch (ClassCastException castError) {
          if (name.equalsIgnoreCase('type')) {
            final Type type = argument instanceof Class ? project.typeResolver.byGroovyClass((Class<?>)argument) : project.typeResolver.byName("$argument")
            try {
              core.setProperty(name, type)
            } catch (Exception typeError) {
              throw e
            }
          } else {
            throw e
          }
        }

        return core.getProperty(name)
      }
      throw e
    }
  }

}
