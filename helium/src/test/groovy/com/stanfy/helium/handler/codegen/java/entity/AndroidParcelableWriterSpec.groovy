package com.stanfy.helium.handler.codegen.java.entity

import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type
import spock.lang.Specification
/**
 * Created by roman on 12/30/13.
 */
class AndroidParcelableWriterSpec extends Specification {
  /** Instance under tests. */
  AndroidParcelableWriter writer
  /** Output. */
  StringWriter output
  /** Options. */
  EntitiesGeneratorOptions options

  def setup() {
    output = new StringWriter()
    options = EntitiesGeneratorOptions.defaultOptions("test")
    writer = new AndroidParcelableWriter(new PojoWriter(output), options)
  }

  def "should write parcelable code"() {
    given:
    Message msg = new Message(name: "MyMsg")
    msg.addField(new Field(name: "device_id", type: new Type(name: "string")))
    msg.addField(new Field(name: "another_id", type: new Type(name: "int32")))

    when:
    new MessageToJavaClass(writer, options).write(msg)

    then:
    output.toString() == """
package test;

import android.os.Parcel;
import android.os.Parcelable;

public class MyMsg
    implements Parcelable {

  public static final Creator<MyMsg> CREATOR = new Creator<MyMsg>() {
        public MyMsg createFromParcel(Parcel source) {
          return new MyMsg(source);
        }
        public MyMsg[] newArray(int size) {
          return new MyMsg[size];
        }
      };

  public String device_id;

  public int another_id;


  public MyMsg() {
  }

  MyMsg(Parcel source) {
    this.device_id = source.readString();
    this.another_id = source.readInt();
  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int options) {
    dest.writeString(this.device_id);
    dest.writeInt(this.another_id);
  }

}
""".trim() + '\n'
  }


  def "should be able to read dates"() {
    given:
    Message msg = new Message(name: "MyMsg")
    msg.addField(new Field(name: "dateField", type: new Type(name: "date")))
    options.customPrimitivesMapping = [date: Date.class.name]

    when:
    writer.getOutput().emitPackage("test")
    writer.getOutput().beginType("MyMsg", "class");
    writer.writeConstructors(msg)
    writer.getOutput().endType();

    then:
    output.toString() == """
package test;

class MyMsg {
  public MyMsg() {
  }

  MyMsg(android.os.Parcel source) {
    long dateFieldValue = source.readLong();
    this.dateField = dateFieldValue != -1 ? new Date(dateFieldValue) : null;
  }

}
""".trim() + '\n'
  }

  def "should be able to write dates"() {
    given:
    Message msg = new Message(name: "MyMsg")
    msg.addField(new Field(name: "dateField", type: new Type(name: "date")))
    options.customPrimitivesMapping = [date: Date.class.name]

    when:
    writer.getOutput().emitPackage("test")
    writer.getOutput().beginType("MyMsg", "class");
    writer.writeClassEnd(msg);

    then:
    output.toString() == """
package test;

class MyMsg {

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(android.os.Parcel dest, int options) {
    dest.writeLong(this.dateField != null ? this.dateField.getTime() : -1L);
  }

}
""".trim() + '\n'
  }


  def "should read sequences as arrays"() {
    given:
    Message msg = new Message(name: "MyMsg")
    msg.addField(new Field(name: "arrayField", type: new Type(name: "int32"), sequence: true))
    options.useArraysForSequences()

    when:
    writer.getOutput().emitPackage("test")
    writer.getOutput().beginType("MyMsg", "class");
    writer.writeConstructors(msg)
    writer.getOutput().endType();

    then:
    output.toString() == """
package test;

class MyMsg {
  public MyMsg() {
  }

  MyMsg(android.os.Parcel source) {
    this.arrayField = source.createIntArray();
  }

}
""".trim() + '\n'
  }

  def "should write sequences as arrays"() {
    given:
    Message msg = new Message(name: "MyMsg")
    msg.addField(new Field(name: "arrayField", type: new Type(name: "int32"), sequence: true))
    options.useArraysForSequences()

    when:
    writer.getOutput().emitPackage("test")
    writer.getOutput().beginType("MyMsg", "class");
    writer.writeClassEnd(msg)

    then:
    output.toString() == """
package test;

class MyMsg {

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(android.os.Parcel dest, int options) {
    dest.writeIntArray(this.arrayField);
  }

}
""".trim() + '\n'
  }

  private static String buildClassCode(Map paramsMap) {
    if (!paramsMap.className) {
      throw new IllegalArgumentException("className is required")
    }

    return """
package ${paramsMap.containsKey('package') ? paramsMap.package : 'test'};

class ${paramsMap.className} {
  public ${paramsMap.className}() {
  }

  ${paramsMap.className}(android.os.Parcel source) {
    ${paramsMap.readBody.trim()}
  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(android.os.Parcel dest, int options) {
    ${paramsMap.writeBody.trim()}
  }

}
""".trim() + '\n'
  }

  private void outReadAndWrite(final Message msg) {
    writer.getOutput().emitPackage("test")
    writer.getOutput().beginType(msg.name, "class");
    writer.writeConstructors(msg)
    writer.writeClassEnd(msg)
  }

  def "should be able to treat other messages"() {
    given:
    Message child = new Message(name: "ChildMsg")
    Message msg = new Message(name: "MyMsg")
    msg.addField(new Field(name: "msg", type: child))

    when:
    outReadAndWrite(msg)

    then:
    output.toString() == buildClassCode(
        className: 'MyMsg',
        readBody: """
    this.msg = (ChildMsg) source.readParcelable(getClass().getClassLoader());
""",
        writeBody: """
    dest.writeParcelable(this.msg, options);
""")

  }

  def "should be able to treat other message sequences"() {
    given:
    Message child = new Message(name: "ChildMsg")
    Message msg = new Message(name: "MyMsg")
    msg.addField(new Field(name: "msg", type: child, sequence: true))

    when:
    outReadAndWrite(msg)

    then:
    output.toString() == buildClassCode(
        className: 'MyMsg',
        readBody: """
    Parcelable[] msgParcelables = source.readParcelableArray(getClass().getClassLoader());
    if (msgParcelables != null) {
      this.msg = new ChildMsg[msgParcelables.length];
      for (int i = 0; i < msgParcelables.length; i++) {
        this.msg[i] = (ChildMsg) msgParcelables[i];
      }
    }
""",
        writeBody: """
    dest.writeParcelableArray(this.msg, options);
""")

  }


  def "should handle booleans"() {
    given:
    Message msg = new Message(name: "MyMsg")
    Type boolType = new Type(name: "bool")
    msg.addField(new Field(name: "boolF", type: boolType))

    when:
    outReadAndWrite(msg)

    then:
    output.toString() == buildClassCode(
        className: "MyMsg",
        readBody: """
    this.boolF = source.readInt() == 1;
""",
        writeBody: """
    dest.writeInt(this.boolF ? 1 : 0);
"""
    )
  }

  def "should handle boolean sequences"() {
    given:
    Message msg = new Message(name: "MyMsg")
    Type boolType = new Type(name: "bool")
    msg.addField(new Field(name: "boolF", type: boolType, sequence: true))

    when:
    outReadAndWrite(msg)

    then:
    output.toString() == buildClassCode(
        className: "MyMsg",
        readBody: """
    int boolFCount = source.readInt();
    if (boolFCount > 0) {
      this.boolF = new boolean[boolFCount];
      for (int i = 0; i < boolFCount; i++) {
        this.boolF[i] = source.readInt() == 1;
      }
    }
""",
        writeBody: """
    int boolFCount = this.boolF != null ? this.boolF.length : 0;
    dest.writeInt(boolFCount);
    for (int i = 0; i < boolFCount; i++) {
      dest.writeInt(this.boolF[i] ? 1 : 0);
    }
"""
    )
  }

  def "ignores skipped fields"() {
    given:
    Message msg = new Message(name: "MyMsg")
    Type type = new Type(name: "any Type")
    msg.addField(new Field(name: "field", type: type, skip: true))

    when:
    outReadAndWrite(msg)

    then:
    output.toString() == '''
package test;

class MyMsg {
  public MyMsg() {
  }

  MyMsg(android.os.Parcel source) {
  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(android.os.Parcel dest, int options) {
  }

}
'''.trim() + '\n'
  }

  def "safe field names for reserved words"() {
    given:
    Message msg = new Message(name: "Test")
    msg.addField(new Field(name: "new", type: new Type(name: "bool")))

    when:
    outReadAndWrite(msg)

    then:
    output.toString() == buildClassCode(
        className: "Test",
        readBody: """
    this.newField = source.readInt() == 1;
""",
        writeBody: """
    dest.writeInt(this.newField ? 1 : 0);
"""
    )

  }


  def "enumerations support"() {
    given:
    Message msg = new Message(name: "Test")
    msg.addField(new Field(name: "enum", type: new Type(name: "my-enum")))
    options.customPrimitivesMapping = ['my-enum': MyTestEnum.class.name]

    when:
    outReadAndWrite(msg)

    then:
    output.toString() == buildClassCode(
        className: "Test",
        readBody: """
    this.enumField = ${MyTestEnum.class.name.replace('$', '.')}.values()[source.readInt()];
""",
        writeBody: """
    dest.writeInt(this.enumField.ordinal());
"""
    )
  }

  /** Enum for tests. */
  enum MyTestEnum {
    ONE, TWO
  }

}
