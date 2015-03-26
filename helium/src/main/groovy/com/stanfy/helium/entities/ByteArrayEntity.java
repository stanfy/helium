package com.stanfy.helium.entities;

/**
 * Convenience class for transferring byte arrays.
 * The purpose is to avoid unnecessary casts <code>byte[] to Byte[]</code>
 * and boxing/unboxing.
 *
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
public class ByteArrayEntity {

  private byte[] bytes;

  public ByteArrayEntity(final byte[] bytes) {
    this.bytes = bytes;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public void setBytes(final byte[] bytes) {
    this.bytes = bytes;
  }
}
