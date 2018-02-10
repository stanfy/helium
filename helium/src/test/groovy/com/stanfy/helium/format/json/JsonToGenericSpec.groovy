package com.stanfy.helium.format.json

import com.google.gson.stream.JsonReader
import okio.Buffer
import spock.lang.Specification

/** Generic transformations for JSON input. */
class JsonToGenericSpec extends Specification {

  Buffer inputBuffer
  JsonReader jsonReader

  def setup() {
    inputBuffer = new Buffer()
    jsonReader = new JsonReader(new InputStreamReader(inputBuffer.inputStream(), "UTF-8"))
    jsonReader.lenient = true
  }

  def "read numbers"() {
    when:
    inputBuffer.writeUtf8("123")
    then:
    JsonToGeneric.readValue(jsonReader) == 123
  }

  def "read real numbers"() {
    when:
    inputBuffer.writeUtf8("123.5")
    then:
    JsonToGeneric.readValue(jsonReader) == 123.5
  }

  def "read strings"() {
    when:
    inputBuffer.writeUtf8("\"abc\"")
    then:
    JsonToGeneric.readValue(jsonReader) == "abc"
  }

  def "read boolean"() {
    when:
    inputBuffer.writeUtf8("false")
    then:
    JsonToGeneric.readValue(jsonReader) == false
  }

  def "read null"() {
    when:
    inputBuffer.writeUtf8("null")
    then:
    JsonToGeneric.readValue(jsonReader) == null
  }

  def "read object"() {
    when:
    inputBuffer.writeUtf8("{\"a\": \"b\", \"c\": 123}")
    then:
    JsonToGeneric.readValue(jsonReader) == ['a': 'b', 'c': 123]
  }

  def "read array"() {
    when:
    inputBuffer.writeUtf8("[\"1\", 2, true]")
    then:
    JsonToGeneric.readValue(jsonReader) == ['1', 2, true]
  }
}
