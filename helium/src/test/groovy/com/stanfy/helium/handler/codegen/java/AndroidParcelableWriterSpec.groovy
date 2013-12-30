package com.stanfy.helium.handler.codegen.java

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
  PojoGeneratorOptions options

  def setup() {
    output = new StringWriter()
    options = PojoGeneratorOptions.defaultOptions("test")
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

}
