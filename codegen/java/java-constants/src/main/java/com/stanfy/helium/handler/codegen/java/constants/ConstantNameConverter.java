package com.stanfy.helium.handler.codegen.java.constants;

import com.stanfy.helium.model.Field;

import java.io.Serializable;

/**
 * Converts field to a constant.
 */
public interface ConstantNameConverter extends Serializable {

  String constantFrom(Field field);

}
