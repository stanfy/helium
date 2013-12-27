package com.stanfy.helium.handler.codegen.java

import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type
import spock.lang.Specification

/**
 * Tests for MessageToGsonClass.
 */
class MessageToGsonClassSpec extends Specification {

  /** Instance under tests. */
  MessageToGsonClass converter
  /** Output. */
  StringWriter output

  def setup() {
    output = new StringWriter()
    converter = new MessageToGsonClass(output, PojoGeneratorOptions.defaultOptions("test"))
  }

  def "should write class file"() {
    given:
    Message msg = new Message(name: "MyMsg")
    msg.addField(new Field(name: "device_id", type: new Type(name: "string")))
    msg.addField(new Field(name: "another_id", type: new Type(name: "int32")))

    when:
    converter.write(msg)

    then:
    output.toString() == """
package test;

import com.google.gson.annotations.SerializedName;

public class MyMsg {

  @SerializedName("device_id")
  public final String device_id;

  @SerializedName("another_id")
  public final int another_id;


}
""".trim() + '\n'
  }

}
