package com.stanfy.helium.model

import groovy.transform.CompileStatic

/**
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
@CompileStatic
class FormType extends Type {

  Message base;

  public FormType(final Message base) {
    this.base = base
    base.activeFields.each { Field field ->
      if (field.type instanceof Message) {
        throw new IllegalArgumentException("Form messages may not contain other messages: $base.${field.name}")
      }
    }
  }
}
