package com.stanfy.helium.model

import groovy.transform.CompileStatic

/**
 * Represents <a href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.1">http form-url-encoded</a> type.
 * It wraps other {@link Message} type, which may not have nested messages.
 *
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
