package com.stanfy.helium.gradle

import com.stanfy.helium.internal.utils.DslUtils

/**
 * DSL for per-specification rules:
 * <code>
 *   specification(file) {
 *     ...
 *   }
 * </code>
 */
class SpecificationDslDelegate {

  /** Specification. */
  private final File specification

  /** User config to edit. */
  private final UserConfig config

  SpecificationDslDelegate(final File spec, final UserConfig config) {
    this.specification = spec
    this.config = config
  }

  void sourceGen(Closure<Void> action) {
    SourceGenDslDelegate delegate = new SourceGenDslDelegate(action.owner)
    DslUtils.runWithProxy(delegate, action)
    config.set specification, delegate
  }

}
