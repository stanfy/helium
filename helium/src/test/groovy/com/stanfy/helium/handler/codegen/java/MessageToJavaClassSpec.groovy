package com.stanfy.helium.handler.codegen.java

import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type
import spock.lang.Specification

import javax.lang.model.element.Modifier

/**
 * Tests for MessageToJavaClass.
 */
class MessageToJavaClassSpec extends Specification {

  private static final String TEST_PACKAGE = "com.stanfy.helium.test"

  /** Instance under tests. */
  MessageToJavaClass converter
  /** Output. */
  StringWriter output
  /** Options. */
  PojoGeneratorOptions options

  def setup() {
    output = new StringWriter()
    options = new PojoGeneratorOptions()
    options.fieldModifiers = [Modifier.PRIVATE] as Set
    options.addGetters = true
    options.addSetters = true
    options.packageName = TEST_PACKAGE;
    converter = new MessageToJavaClass(output, options)
  }

  def "should write class file"() {
    given:
    Message msg = new Message(name: "MyMsg")
    msg.addField(new Field(name: "fieldStr", type: new Type(name: "string")))
    msg.addField(new Field(name: "fieldInt32", type: new Type(name: "int32")))
    msg.addField(new Field(name: "fieldBool", type: new Type(name: "bool")))
    msg.addField(new Field(name: "fieldFloatList", type: new Type(name: "float"), sequence: true))
    Message childMessage = new Message(name: "Child")
    msg.addField(new Field(name: "fieldChild", type: childMessage))

    when:
    converter.write(msg)

    then:
    output.toString() == """
package $TEST_PACKAGE;

import java.util.List;

public class MyMsg {

  private String fieldStr;
  private int fieldInt32;
  private boolean fieldBool;
  private List<Float> fieldFloatList;
  private Child fieldChild;

  public String getFieldStr() {
    return fieldStr;
  }

  public void setFieldStr(String value) {
    fieldStr = value;
  }

  public int getFieldInt32() {
    return fieldInt32;
  }

  public void setFieldInt32(int value) {
    fieldInt32 = value;
  }

  public boolean getFieldBool() {
    return fieldBool;
  }

  public void setFieldBool(boolean value) {
    fieldBool = value;
  }

  public List<Float> getFieldFloatList() {
    return fieldFloatList;
  }

  public void setFieldFloatList(List<Float> value) {
    fieldFloatList = value;
  }

  public Child getFieldChild() {
    return fieldChild;
  }

  public void setFieldChild(Child value) {
    fieldChild = value;
  }

}
""".trim() + '\n'
  }


  def "should handle custom primitive mapping"() {
    given:
    options.addGetters = false;
    options.addSetters = false;
    options.customPrimitivesMapping = [
        date: Date.class.canonicalName
    ]
    Message msg = new Message(name: "DateMsg")
    msg.addField(new Field(name: "date", type: new Type(name: "date")))
    msg.addField(new Field(name: "dateList", type: new Type(name: "date"), sequence: true))

    when:
    converter.write(msg)

    then:
    output.toString() == """
package $TEST_PACKAGE;

import java.util.Date;
import java.util.List;

public class DateMsg {

  private Date date;
  private List<Date> dateList;

}
""".trim() + '\n'

  }

}
