package com.stanfy.helium.gradle

import groovy.transform.PackageScope

import com.stanfy.helium.handler.codegen.java.entity.EntitiesGeneratorOptions

/**
 * Delegate for DSL
 * <code>
 *   pojo {
 *     packageName = "foo"
 *   }
 * </code>
 */
class PojoDslDelegate {

  @PackageScope File output

  @PackageScope EntitiesGeneratorOptions genOptions = EntitiesGeneratorOptions.defaultOptions("api")

  void output(File output) {
    this.output = output;
  }

  void options(Closure<?> config) {
    Closure<?> action = config.clone() as Closure<?>
    action.delegate = genOptions
    action.resolveStrategy = Closure.DELEGATE_FIRST
    action();
  }

}
