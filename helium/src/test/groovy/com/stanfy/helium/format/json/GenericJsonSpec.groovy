package com.stanfy.helium.format.json

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import okio.Buffer
import spock.lang.Specification

/** Generic transformations for JSON input. */
class GenericJsonSpec extends Specification {

  Buffer buffer
  JsonReader jsonReader
  JsonWriter jsonWriter

  def setup() {
    buffer = new Buffer()
    jsonReader = new JsonReader(new InputStreamReader(buffer.inputStream(), "UTF-8"))
    jsonReader.lenient = true
    jsonWriter = new JsonWriter(new OutputStreamWriter(buffer.outputStream(), "UTF-8"))
  }

  def "read numbers"() {
    when:
    buffer.writeUtf8("123")
    then:
    GenericJson.readValue(jsonReader) == 123
  }

  def "read real numbers"() {
    when:
    buffer.writeUtf8("123.5")
    then:
    GenericJson.readValue(jsonReader) == 123.5
  }

  def "read strings"() {
    when:
    buffer.writeUtf8("\"abc\"")
    then:
    GenericJson.readValue(jsonReader) == "abc"
  }

  def "read boolean"() {
    when:
    buffer.writeUtf8("false")
    then:
    GenericJson.readValue(jsonReader) == false
  }

  def "read null"() {
    when:
    buffer.writeUtf8("null")
    then:
    GenericJson.readValue(jsonReader) == null
  }

  def "read object"() {
    when:
    buffer.writeUtf8("{\"a\": \"b\", \"c\": 123}")
    then:
    GenericJson.readValue(jsonReader) == ['a': 'b', 'c': 123]
  }

  def "read array"() {
    when:
    buffer.writeUtf8("[\"1\", 2, true]")
    then:
    GenericJson.readValue(jsonReader) == ['1', 2, true]
  }

  def "write maps"() {
    when:
    GenericJson.writeMap(jsonWriter, ['k1': 'v1', 'k2': 42])
    jsonWriter.close()
    then:
    buffer.readUtf8() == '{"k1":"v1","k2":42}'
  }

  def "write collections"() {
    when:
    GenericJson.writeCollection(jsonWriter, ['one', 2, false])
    jsonWriter.close()
    then:
    buffer.readUtf8() == '["one",2,false]'
  }

  def "write numbers"() {
    when:
    GenericJson.writeValue(jsonWriter, 3.5)
    jsonWriter.close()
    then:
    buffer.readUtf8() == '3.5'
  }
}
