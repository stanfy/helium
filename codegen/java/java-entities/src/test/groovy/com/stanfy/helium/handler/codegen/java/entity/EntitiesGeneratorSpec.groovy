package com.stanfy.helium.handler.codegen.java.entity

import com.stanfy.helium.handler.codegen.java.BaseMessageToClassGeneratorSpec

import static com.stanfy.helium.handler.codegen.java.ClassAncestors.extending
import static com.stanfy.helium.handler.codegen.java.ClassAncestors.implementing

/**
 * Tests for EntitiesGenerator.
 */
class EntitiesGeneratorSpec extends BaseMessageToClassGeneratorSpec<EntitiesGenerator> {

  EntitiesGeneratorOptions options

  def setup() {
    options = EntitiesGeneratorOptions.defaultOptions("com.stanfy.helium")
    generator = new EntitiesGenerator(output, options)
  }

  def "process enums"() {
    given:
    project.type "string"
    project.type "day" spec {
      description "Days of week."
      constraints("string") {
        enumeration "mon", "tue", "wed", "thu", "fri", "sat", "sun"
      }
    }

    when:
    generator.handle(project)
    def text = new File("$output/com/stanfy/helium/Day.java").text

    then:
    text == """
package com.stanfy.helium;

/**
 * Days of week.
 */
public enum Day {
  MON,
  TUE,
  WED,
  THU,
  FRI,
  SAT,
  SUN;
}
""".trim() + '\n'
  }

  def "should be able to chain writers"() {
    given:
    options.writerWrapper = Writers.chain(Writers.gson(), Writers.androidParcelable(), Writers.jackson())

    when:
    generator.handle(project)
    def text = new File("$output/com/stanfy/helium/A.java").text

    then:
    text == """
package com.stanfy.helium;

import android.os.Parcel;
import android.os.Parcelable;
import com.fasterxml.jackson.annotation.JsonProperty;
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

  def "should respect message parents"() {
    given:
    options.customParentMapping = [
        Hasky: extending("Dog", "Woofer", "Fetcher")
    ]
    and:
    project.type 'string'
    project.type "Hasky" message {
      name 'string'
    }

    when:
    generator.handle(project)
    def text = new File("$output/com/stanfy/helium/Hasky.java").text

    then:
    text == """
package com.stanfy.helium;

public class Hasky extends Dog
    implements Woofer, Fetcher {

  public String name;


  @Override
  public String toString() {
    return "Hasky: {\\n"
         + "  name=\\"" + name + "\\"\\n"
         + "}";
  }
}""".trim() + '\n'
  }

  def "should check parent defined in dsl and external"() {
    given:
    options.customParentMapping = [
        Pegasus: extending("Bird")
    ]

    and:
    project.type 'Horse' message {}
    project.type 'Pegasus' message(parent: 'Horse') {}

    when:
    generator.handle(project)

    then:
    thrown(IllegalArgumentException)
  }

  def "should allow having external interfaces when message has parent"() {
    given:
    options.customParentMapping = [
        Pegasus: implementing("FlyingThing")
    ]

    and:
    project.type 'Horse' message {}
    project.type 'Pegasus' message(parent: 'Horse') {}

    when:
    generator.handle(project)

    then:
    notThrown(IllegalArgumentException)
  }

  def "enum array field"() {
    given:
    options.useArraysForSequences()

    and:
    project.type "string"
    project.type "testEnum" spec {
      constraints("string") {
        enumeration '1', '2', '3'
      }
    }
    project.type 'Container' message {
      values 'testEnum' sequence
    }

    when:
    generator.handle(project)
    def text = new File("$output/com/stanfy/helium/Container.java").text

    then:
    text == '''
package com.stanfy.helium;

import java.util.Arrays;

public class Container {

  public TestEnum[] values;


  @Override
  public String toString() {
    return "Container: {\\n"
         + "  values=\\"" + (values != null ? Arrays.toString(values) : "null") + "\\"\\n"
         + "}";
  }
}
'''.trim() + '\n'
  }

}
