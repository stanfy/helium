package com.stanfy.helium.internal.dsl

import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.constraints.ConstrainedType
import com.stanfy.helium.model.constraints.Constraint
import com.stanfy.helium.utils.ConfigurableProxy
import com.stanfy.helium.utils.DslUtils
import groovy.transform.PackageScope

/**
 * DSL for fields configuration.
 */
class ConfigurableField extends ConfigurableProxy<Field> {

  private ArrayList<Constraint<?>> constraints

  ConfigurableField(final Field core, final ProjectDsl project) {
    super(core, project)
  }

  void constraints(final Closure<?> spec) {
    this.constraints = new ArrayList<>()
    DslUtils.runWithProxy(new ConstraintsDsl(constraints), spec)
  }

  @PackageScope void resolveConstraints(final Message message) {
    if (!constraints) {
      return
    }

    def core = getCore()
    Type baseType = core.type
    if (!baseType) {
      throw new IllegalStateException("Field type is not resolved: ${core}")
    }

    ConstrainedType newType = new ConstrainedType(baseType)
    newType.addConstraints(constraints)
    newType.anonymous = true
    newType.name = "constraints_on_${baseType.name}_for_${message.name}#${core.name}"

    core.type = newType
  }

}
