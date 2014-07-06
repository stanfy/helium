package com.stanfy.helium.handler.codegen.java.entity

import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type
import spock.lang.Specification

import javax.lang.model.element.Modifier

/**
 * Tests for PojoWriter.
 */
class PojoWriterSpec extends Specification {

  protected static final String TEST_PACKAGE = "com.stanfy.helium.test"

  /** Instance under tests. */
  PojoWriter writer
  /** Output. */
  StringWriter output
  /** Options. */
  EntitiesGeneratorOptions options

  def setup() {
    output = new StringWriter()
    options = new EntitiesGeneratorOptions()
    options.fieldModifiers = [Modifier.PRIVATE] as Set
    options.addGetters = true
    options.addSetters = true
    options.addToString = false
    options.packageName = TEST_PACKAGE;
    writer = new PojoWriter(output)
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
    new MessageToJavaClass(writer, options).write(msg)

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
    return this.fieldStr;
  }

  public void setFieldStr(String value) {
    this.fieldStr = value;
  }

  public int getFieldInt32() {
    return this.fieldInt32;
  }

  public void setFieldInt32(int value) {
    this.fieldInt32 = value;
  }

  public boolean getFieldBool() {
    return this.fieldBool;
  }

  public void setFieldBool(boolean value) {
    this.fieldBool = value;
  }

  public List<Float> getFieldFloatList() {
    return this.fieldFloatList;
  }

  public void setFieldFloatList(List<Float> value) {
    this.fieldFloatList = value;
  }

  public Child getFieldChild() {
    return this.fieldChild;
  }

  public void setFieldChild(Child value) {
    this.fieldChild = value;
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
    new MessageToJavaClass(writer, options).write(msg)

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


  def "should prettify names"() {
    given:
    options.prettifyNames = true;
    Message msg = new Message(name: "Test")
    msg.addField(new Field(name: "test_field", type: new Type(name: "string")))

    when:
    new MessageToJavaClass(writer, options).write(msg)

    then:
    output.toString() == """
package $TEST_PACKAGE;

public class Test {

  private String testField;


  public String getTestField() {
    return this.testField;
  }

  public void setTestField(String value) {
    this.testField = value;
  }

}
""".trim() + '\n'

  }

  def "ignores skipped fields"() {
    given:
    Message msg = new Message(name: "Test")
    msg.addField(new Field(name: "test_field", type: new Type(name: "any type"), skip: true))

    when:
    new MessageToJavaClass(writer, options).write(msg)

    then:
    output.toString() == """
package $TEST_PACKAGE;

public class Test {


}
""".trim() + '\n'
  }

  def "safe field names for reserved words"() {
    given:
    Message msg = new Message(name: "Test")
    msg.addField(new Field(name: "new", type: new Type(name: "string")))

    when:
    new MessageToJavaClass(writer, options).write(msg)

    then:
    output.toString() == """
package $TEST_PACKAGE;

public class Test {

  private String newField;


  public String getNew() {
    return this.newField;
  }

  public void setNew(String value) {
    this.newField = value;
  }

}
""".trim() + '\n'

  }

  def "primitive type arrays"() {
    given:
    Message msg = new Message(name: "Test")
    msg.addField(new Field(name: "a", type: new Type(name: "int32"), required: false, sequence: true))
    options.useArraysForSequences()

    when:
    new MessageToJavaClass(writer, options).write(msg)

    then:
    output.toString() == """
package $TEST_PACKAGE;

public class Test {

  private int[] a;


  public int[] getA() {
    return this.a;
  }

  public void setA(int[] value) {
    this.a = value;
  }

}
""".trim() + '\n'

  }

  def "toString message with single field"() {
    given:
    Message msg = new Message(name: "Test")
    msg.addField(new Field(name: "new", type: new Type(name: "string")))
    options.addToString = true

    when:
    new MessageToJavaClass(writer, options).write(msg)

    then:
    output.toString().contains(
'''
  @Override
  public String toString() {
    return "Test: {\\n"
         + "  newField=\\"" + newField + "\\"\\n"
         + "}";
  }
'''
    )
  }

  def "toString of empty message"() {
    given:
    Message msg = new Message(name: "Test")
    options.addToString = true

    when:
    new MessageToJavaClass(writer, options).write(msg)

    then:
    output.toString().contains(
'''
  @Override
  public String toString() {
    return "Test: has no fields";
  }
'''
    )
  }

  def "toString for complex message"() {
    given:
    Message msg = new Message(name: "MyMsg")
    msg.addField(new Field(name: "fieldStr", type: new Type(name: "string")))
    msg.addField(new Field(name: "fieldInt32", type: new Type(name: "int32")))
    msg.addField(new Field(name: "fieldBool", type: new Type(name: "bool")))
    Message childMessage = new Message(name: "Child")
    msg.addField(new Field(name: "fieldChild", type: childMessage))
    options.addToString = true

    when:
    new MessageToJavaClass(writer, options).write(msg)

    then:
    output.toString().contains(
'''
  @Override
  public String toString() {
    return "MyMsg: {\\n"
         + "  fieldStr=\\"" + fieldStr + "\\",\\n"
         + "  fieldInt32=\\"" + fieldInt32 + "\\",\\n"
         + "  fieldBool=\\"" + fieldBool + "\\",\\n"
         + "  fieldChild=\\"" + (fieldChild != null ? fieldChild.toString() : "null") + "\\"\\n"
         + "}";
  }
'''
    )
  }

  def "toString for sequence"() {
    given:
    Message msg = new Message(name: "MyMsg")
    msg.addField(new Field(name: "fieldFloatList", type: new Type(name: "float"), sequence: true))
    options.addToString = true
    options.sequenceCollectionName = "List"

    when:
    new MessageToJavaClass(writer, options).write(msg)

    then:
    output.toString().contains(
'''
  @Override
  public String toString() {
    return "MyMsg: {\\n"
         + "  fieldFloatList=\\"" + (fieldFloatList != null ? fieldFloatList.toString() : "null") + "\\"\\n"
         + "}";
  }
'''
    )
  }

  def "toString for sequence (sequences in form of arrays)"() {
    given:
    Message msg = new Message(name: "MyMsg")

    msg.addField(new Field(name: "affiliateId", type: new Type(name: "string")))
    Message childMessage = new Message(name: "Child")
    msg.addField(new Field(name: "fieldChild", type: childMessage))
    msg.addField(new Field(name: "fieldChildSeq", type: childMessage, sequence: true))
    msg.addField(new Field(name: "fieldChildSeqPrimitive", type: new Type(name: "float"), sequence: true))
    options.addToString = true
    options.sequenceCollectionName = null

    when:
    new MessageToJavaClass(writer, options).write(msg)

    then:
    output.toString().contains(
'''
  @Override
  public String toString() {
    return "MyMsg: {\\n"
         + "  affiliateId=\\"" + affiliateId + "\\",\\n"
         + "  fieldChild=\\"" + (fieldChild != null ? fieldChild.toString() : "null") + "\\",\\n"
         + "  fieldChildSeq=\\"" + (fieldChildSeq != null ? Arrays.deepToString(fieldChildSeq) : "null") + "\\",\\n"
         + "  fieldChildSeqPrimitive=\\"" + (fieldChildSeqPrimitive != null ? Arrays.toString(fieldChildSeqPrimitive) : "null") + "\\"\\n"
         + "}";
  }
''')
    output.toString().contains("import java.util.Arrays;")
  }

  def "escape Java keywords in toString"() {
    given:
    Message msg = new Message(name: "MyMsg")
    msg.addField(new Field(name: "package", type: new Type(name: "float")))
    msg.addField(new Field(name: "versionCode", type: new Type(name: "int32")))
    options.addToString = true

    when:
    new MessageToJavaClass(writer, options).write(msg)

    then:
    output.toString().contains(
'''
  @Override
  public String toString() {
    return "MyMsg: {\\n"
         + "  packageField=\\"" + packageField + "\\",\\n"
         + "  versionCode=\\"" + versionCode + "\\"\\n"
         + "}";
  }
''')
  }

  def "write JavaDoc from Description"() {
    given:
    Message msg = new Message(name: "Test", description: "Some testing class")
    msg.addField(new Field(name: "a", type: new Type(name: "int32"), required: false, sequence: true, description: "Just A field"))
    msg.addField(new Field(name: "percent", type: new Type(name: "int64"), required: false, description: "In %"))
    options.addGetters = false
    options.addSetters = false
    options.useArraysForSequences()

    when:
    new MessageToJavaClass(writer, options).write(msg)

    then:
    output.toString() == """
package $TEST_PACKAGE;

/**
 * Some testing class.
 */
public class Test {

  /**
   * Just A field.
   */
  private int[] a;

  /**
   * In %.
   */
  private long percent;


}
""".trim() + '\n'
  }

}
