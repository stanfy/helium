package com.stanfy.helium.entities.validation.json;

import com.stanfy.helium.entities.ValuePuller;
import com.stanfy.helium.model.Type;

import java.io.IOException;

/**
 * Validates next value from JsonReader.
 */
public interface JsonTypeValidator {

  String validateNextValue(ValuePuller puller, Type type, boolean required) throws IOException;

}
