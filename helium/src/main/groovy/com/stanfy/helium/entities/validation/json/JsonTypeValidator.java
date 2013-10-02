package com.stanfy.helium.entities.validation.json;

import com.stanfy.helium.model.Type;

import java.io.IOException;

/**
 * Validates next value from JsonReader.
 */
public interface JsonTypeValidator {

  String validateNextValue(JsonValuePuller json, Type type) throws IOException;

}
