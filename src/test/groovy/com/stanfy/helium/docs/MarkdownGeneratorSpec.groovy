package com.stanfy.helium.docs

import com.stanfy.helium.Helium
import spock.lang.Specification

/**
 * Spec for MarkdownGenerator.
 */
class MarkdownGeneratorSpec extends Specification {

  def script = {
    note "Example Spec"
    type "SomeMessage" message {
      id long required
      text String required
    }
    service {
      name 'Example Service'
      version 1.1
      get "/message/@id" spec {
        name "Get message"
        description "Returnes a message"
        response "SomeMessage"
      }
    }
  }

  def "generates markdown to writer"() {
    when:
    StringWriter out = new StringWriter()
    new Helium().defaultTypes() from script processBy new MarkdownGenerator(out)
    String res = out.toString()

    then:
    !res.empty
    res.startsWith("Example Spec")
  }

}
