package com.stanfy.helium.gradle.internal

import com.stanfy.helium.internal.utils.DslUtils
import com.stanfy.helium.internal.utils.SelectionRules

class SelectionRulesBuilder {

  final SelectionRules rules

  SelectionRulesBuilder(SelectionRules rules) {
    this.rules = rules
  }

  @Override
  def invokeMethod(String name, Object args) {
    if (name in ['excludes', 'includes']) {
      rules.invokeMethod(name, args)
      return
    }

    Closure<?> config = (args as Object[])[0] as Closure<?>
    SelectionRules nested = new SelectionRules(name)
    DslUtils.runWithProxy(nested, config)
    rules.nest(nested)
  }

}
