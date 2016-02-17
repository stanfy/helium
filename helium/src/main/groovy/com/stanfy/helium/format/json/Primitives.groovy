package com.stanfy.helium.format.json

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.stanfy.helium.DefaultType
import com.stanfy.helium.model.Type
import groovy.transform.PackageScope

@PackageScope
class Primitives {

  private Primitives() { }

  static ClosureJsonConverter converterFor(Type type) {
    Closure<?> numReader
    switch (type.name) {
      case DefaultType.DOUBLE.langName:
        numReader = { JsonReader reader -> return reader.nextDouble() }
        break
      case DefaultType.FLOAT.langName:
        numReader = { JsonReader reader ->
          double doubleValue = reader.nextDouble()
          return (float)doubleValue;
        }
        break
      case DefaultType.INT32.langName:
        numReader = { JsonReader reader -> return reader.nextInt() }
        break
      case DefaultType.INT64.langName:
        numReader = { JsonReader reader -> return reader.nextLong() }
        break

      case DefaultType.BOOL.langName:
        return new ClosureJsonConverter(
            { JsonReader input -> return input.nextBoolean() },
            { JsonWriter output, Object value -> output.value((Boolean)value) }
        )

      case DefaultType.STRING.langName:
        return new ClosureJsonConverter(
            ClosureJsonConverter.AS_STRING_READER,
            ClosureJsonConverter.AS_STRING_WRITER
        )

      case DefaultType.BYTES.langName:
        return new ClosureJsonConverter(
            { JsonReader reader -> reader.nextString().decodeBase64() },
            { JsonWriter writer, Object value ->
              StringWriter buffer = new StringWriter()
              ((byte[]) value).encodeBase64().writeTo(buffer)
              writer.value(buffer.toString())
            }
        )
    }

    if (numReader) {
      return new ClosureJsonConverter(
          numReader,
          { JsonWriter output, Object value ->
            output.value((Number)value)
          }
      )
    }

    throw new UnsupportedOperationException("Cannot convert " + type + " to json")
  }

}
