package com.stanfy.helium.handler.codegen.java.entity

import com.stanfy.helium.handler.codegen.java.BaseMessageToClassGeneratorSpec

/**
 * Tests for EntitiesGenerator.
 */
class EntitiesGeneratorSpec extends BaseMessageToClassGeneratorSpec<EntitiesGenerator> {

  EntitiesGeneratorOptions options

  def setup() {
    options = EntitiesGeneratorOptions.defaultOptions("com.stanfy.helium")
    generator = new EntitiesGenerator(output, options)
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
  public String toString() {
    return "A: has no fields";
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
