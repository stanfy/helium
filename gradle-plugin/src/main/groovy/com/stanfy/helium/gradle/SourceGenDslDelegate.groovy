package com.stanfy.helium.gradle

import com.stanfy.helium.handler.codegen.java.constants.ConstantsGeneratorOptions
import com.stanfy.helium.utils.DslUtils
import groovy.transform.PackageScope

import com.stanfy.helium.handler.codegen.java.entity.EntitiesGeneratorOptions

/**
 * Delegate for DSL
 * <code>
 *   sourceGen {
 *     entities {
 *     }
 *     constants {
 *     }
 *   }
 * </code>
 */
class SourceGenDslDelegate {

  public static final String DEFAULT_PACKAGE = "api"

  private EntitiesDslDelegate entities
  private ConstantsDslDelegate constants

  private final Object owner

  public SourceGenDslDelegate(final Object owner) {
    this.owner = owner
  }

  @PackageScope
  EntitiesDslDelegate getEntities() {
    return entities
  }

  @PackageScope
  ConstantsDslDelegate getConstants() {
    return constants
  }

  @PackageScope
  void setEntities(EntitiesDslDelegate value) {
    this.entities = value
  }

  @PackageScope
  void setConstants(ConstantsDslDelegate value) {
    this.constants = value
  }

  void entities(Closure<?> config) {
    entities = new EntitiesDslDelegate()
    DslUtils.runWithProxy(entities, config)
  }

  void constants(Closure<?> config) {
    constants = new ConstantsDslDelegate()
    DslUtils.runWithProxy(constants, config)
  }


  abstract class BaseDslDelegate<T> {

    T genOptions

    File output

    void output(File output) {
      this.output = output;
    }

    void options(Closure<?> config) {
      DslUtils.runWithProxy(genOptions, config)
    }

  }

  class EntitiesDslDelegate extends BaseDslDelegate<EntitiesGeneratorOptions> {

    @Override
    void options(Closure<?> config) {
      genOptions = EntitiesGeneratorOptions.defaultOptions(DEFAULT_PACKAGE)
      super.options(config)
    }

  }

  class ConstantsDslDelegate extends BaseDslDelegate<ConstantsGeneratorOptions> {

    @Override
    void options(Closure<?> config) {
      genOptions = ConstantsGeneratorOptions.defaultOptions(DEFAULT_PACKAGE)
      super.options(config)
    }

  }

}
