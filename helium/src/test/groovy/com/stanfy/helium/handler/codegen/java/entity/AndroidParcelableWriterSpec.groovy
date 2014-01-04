package com.stanfy.helium.handler.codegen.java.entity

import com.stanfy.helium.handler.codegen.java.entity.AndroidParcelableWriter
import com.stanfy.helium.handler.codegen.java.entity.EntitiesGeneratorOptions
import com.stanfy.helium.handler.codegen.java.entity.MessageToJavaClass
import com.stanfy.helium.handler.codegen.java.entity.PojoWriter
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


  def "should be able to treat other messages"() {
    given:
    Message child = new Message(name: "ChildMsg")
    Message msg = new Message(name: "MyMsg")
    msg.addField(new Field(name: "msg", type: child))

    when:
    writer.getOutput().emitPackage("test")
    writer.getOutput().beginType("MyMsg", "class");
    writer.writeConstructors(msg)
    writer.writeClassEnd(msg)

    then:
    output.toString() == """
package test;

class MyMsg {
  public MyMsg() {
  }

  MyMsg(android.os.Parcel source) {
    this.msg = (ChildMsg) source.readValue(getClass().getClassLoader());
  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(android.os.Parcel dest, int options) {
    dest.writeValue(this.msg);
  }

}
""".trim() + '\n'
  }

}
