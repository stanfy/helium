package com.stanfy.helium.handler.codegen.java.entity

import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.constraints.EnumConstraint
import spock.lang.Specification
/**
 * Tests for JacksonPojoWriter.
 */
class JacksonPojoWriterSpec extends Specification {

  /** Instance under tests. */
  JacksonPojoWriter writer
  /** Output. */
  StringWriter output

  def setup() {
    output = new StringWriter()
    writer = new JacksonPojoWriter(new PojoWriter(output))
  }

  def "should write class file"() {
    given:
    Message msg = new Message(name: "MyMsg")
    msg.addField(new Field(name: "device_id", type: new Type(name: "string")))
    msg.addField(new Field(name: "another_id", type: new Type(name: "int32")))

    when:
    new MessageToJavaClass(writer, EntitiesGeneratorOptions.defaultOptions("test")).write(msg)

    then:
    output.toString() == """
package test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MyMsg {

  @JsonProperty("device_id")
  public String device_id;

  @JsonProperty("another_id")
  public int another_id;


  @Override
  public String toString() {
    return "MyMsg: {\\n"
         + "  device_id=\\"" + device_id + "\\",\\n"
         + "  another_id=\\"" + another_id + "\\"\\n"
         + "}";
  }
}
""".trim() + '\n'
  }

  def "enumeration annotations written"() {
    given:
    EnumConstraint<String> enumConstraint = new EnumConstraint<>(["value1", "value2"])
    Type enumType = new Type(name: "MyEnum")

    when:
    new ConstraintsToEnum(writer, EntitiesGeneratorOptions.defaultOptions("test")).write(enumType, enumConstraint)

    then:
    output.toString() == """
package test;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MyEnum {
  @JsonProperty("value1")
  VALUE1,
  @JsonProperty("value2")
  VALUE2;
}
""".trim() + '\n'
  }

}
