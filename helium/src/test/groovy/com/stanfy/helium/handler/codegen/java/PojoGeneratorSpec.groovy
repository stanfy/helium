package com.stanfy.helium.handler.codegen.java

import com.stanfy.helium.dsl.ProjectDsl
import spock.lang.Specification

/**
 * Tests for PojoGenerator.
 */
class PojoGeneratorSpec extends Specification {

  PojoGenerator generator
  ProjectDsl project
  File output
  PojoGeneratorOptions options

  def setup() {
    project = new ProjectDsl()
    project.type "A" message { }
    project.type "B" message { }
    project.type "C" message { }

    output = File.createTempDir()

    options = PojoGeneratorOptions.defaultOptions("com.stanfy.helium")
    generator = new PojoGenerator(output, options)
  }

  def "should generate files"() {
    when:
    generator.handle(project)

    then:
    new File("$output/com/stanfy/helium/A.java").exists()
    new File("$output/com/stanfy/helium/B.java").exists()
    new File("$output/com/stanfy/helium/C.java").exists()
  }

  def "should be able to chain writers"() {
    given:
    options.writerWrapper = Writers.chain(Writers.gson(), Writers.androidParcelable())

    when:
    generator.handle(project)
    def text = new File("$output/com/stanfy/helium/A.java").text

    then:
    text == """
package com.stanfy.helium;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class A
    implements Parcelable {

  public static final Creator<A> CREATOR = new Creator<A>() {
    public A createFromParcel(Parcel source) {
      return new A(source);
    }
    public A[] newArray(int size) {
      return new A[size];
    }
  };


  public A() {
  }

  A(Parcel source) {
  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int options) {
  }

}
""".trim() + '\n'
  }

}
