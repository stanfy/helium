package com.stanfy.helium.dsl

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.stanfy.helium.model.Type
import com.stanfy.helium.utils.ConfigurableProxy
import com.stanfy.helium.utils.DslUtils

import java.text.ParseException
import java.text.SimpleDateFormat

/**
 * Proxy for type.
 */
class ConfigurableType extends ConfigurableProxy<Type> {

  Map<String, Closure<?>> readers = [:], writers = [:];

  ConfigurableType(final Type core, final ProjectDsl project) {
    super(core, project)
  }

  public void from(final String format, final Closure<?> spec) {
    readers[format] = DslUtils.runWithProxy(new From(), spec) as Closure<?>
  }

  public void to(final String format, final Closure<?> spec) {
    writers[format] = DslUtils.runWithProxy(new To(), spec) as Closure<?>
  }

  public class From {
    public Closure<?> asString() {
      return DefaultTypeResolver.ClosureJsonConverter.AS_STRING_READER
    }
    public Closure<?> asDate(final String dateFormat) {
      return { JsonReader input ->
        String str = (String) DefaultTypeResolver.ClosureJsonConverter.AS_STRING_READER(input)
        if (str == null) { return null }
        try {
          return new SimpleDateFormat(dateFormat).parse(str)
        } catch (ParseException e) {
          throw new IllegalArgumentException("Bad date '$str'; expected format: '$dateFormat'")
        }
      }
    }
  }

  public class To {
    public Closure<?> asString() {
      return DefaultTypeResolver.ClosureJsonConverter.AS_STRING_WRITER
    }
    public Closure<?> asDate(final String dateFormat) {
      return { JsonWriter input, Object value ->
        if (value == null) {
          input.nullValue()
          return
        }
        if (value instanceof Date) {
          input.value(value.format(dateFormat))
          return
        }
        if (value instanceof String) {
          new SimpleDateFormat(dateFormat).parse((String)value) // try to parse
          input.value((String)value)
          return
        }

        throw new IllegalArgumentException("Cannot interpret '$value' as date. Format: '$dateFormat'.")
      }
    }
  }

}
