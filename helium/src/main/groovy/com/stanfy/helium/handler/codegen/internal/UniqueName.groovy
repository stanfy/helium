package com.stanfy.helium.handler.codegen.internal

import java.security.MessageDigest;

/**
 * Generates a new unique name based on the specified file.
 */
public class UniqueName {

  static String from(final File file) {
    String path = file.canonicalPath
    MessageDigest digest = MessageDigest.getInstance("MD5")
    digest.update(path.getBytes("UTF-8"))
    return "helium_".concat(new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0'))
  }

}
