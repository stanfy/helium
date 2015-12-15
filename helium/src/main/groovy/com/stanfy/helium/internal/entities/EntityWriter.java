package com.stanfy.helium.internal.entities;

import java.io.IOException;

/**
 * Can write entity somewhere.
 */
public interface EntityWriter {

  void write(final TypedEntity<?> entity) throws IOException;

}
