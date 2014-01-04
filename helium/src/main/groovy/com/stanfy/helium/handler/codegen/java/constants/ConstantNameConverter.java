package com.stanfy.helium.handler.codegen.java.constants;

import com.stanfy.helium.model.Field;

/**
 * Converts field to a constant.
 */
public interface ConstantNameConverter {

  String constantFrom(Field field);

}
