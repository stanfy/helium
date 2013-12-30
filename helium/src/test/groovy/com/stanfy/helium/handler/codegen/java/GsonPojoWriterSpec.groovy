package com.stanfy.helium.handler.codegen.java

import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type
import spock.lang.Specification

/**
 * Tests for GsonPojoWriter.
 */
class GsonPojoWriterSpec extends Specification {

  /** Instance under tests. */
  GsonPojoWriter writer
  /** Output. */
  StringWriter output

  def setup() {
    output = new StringWriter()
    writer = new GsonPojoWriter(new PojoWriter(output))
  }

  def "should write class file"() {
    given:
    Message msg = new Message(name: "MyMsg")
    msg.addField(new Field(name: "device_id", type: new Type(name: "string")))
    msg.addField(new Field(name: "another_id", type: new Type(name: "int32")))

    when:
    new MessageToJavaClass(writer, PojoGeneratorOptions.defaultOptions("test")).write(msg)

    then:
    output.toString() == """
package test;

import com.google.gson.annotations.SerializedName;

public class MyMsg {

  @SerializedName("device_id")
  public String device_id;

  @SerializedName("another_id")
  public int another_id;


}
""".trim() + '\n'
  }

}
