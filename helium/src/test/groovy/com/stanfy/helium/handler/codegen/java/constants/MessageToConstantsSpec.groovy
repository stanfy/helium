package com.stanfy.helium.handler.codegen.java.constants

import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type
import spock.lang.Specification

/**
 * Writes constants class.
 */
class MessageToConstantsSpec extends Specification {

  /** Instance under tests. */
  MessageToConstants converter

  /** Output. */
  StringWriter output

  /** Options. */
  ConstantsGeneratorOptions options

  def setup() {
    output = new StringWriter()
    options = ConstantsGeneratorOptions.defaultOptions("com.stanfy.helium")
    converter = new MessageToConstants(output, options)
  }

  def "should write constants"() {
    given:
    Message msg = new Message(name: "Test")
    msg.addField(new Field(name: "a", type: new Type(name: "a")))
    msg.addField(new Field(name: "b", type: new Type(name: "b")))

    when:
    converter.write(msg)

    then:
    output.toString() == """
package com.stanfy.helium;

public class TestConstants {
  public static final String A = "a";
  public static final String B = "b";
}
""".trim() + '\n'
  }

}
