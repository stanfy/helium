package com.stanfy.helium.entities.json;

import com.stanfy.helium.model.Type;

import java.io.IOException;

/**
 * Gets next value from JsonReader.
 */
interface JsonTypedValuePuller {

  Object validateNextValue(ValuePuller puller, Type type, boolean required) throws IOException;

}
