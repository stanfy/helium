package com.stanfy.helium.dsl

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.stanfy.helium.entities.ConvertValueSyntaxException
import com.stanfy.helium.entities.json.ClosureJsonConverter
import com.stanfy.helium.model.Type
import com.stanfy.helium.utils.ConfigurableProxy
import com.stanfy.helium.utils.DslUtils
import org.joda.time.format.DateTimeFormat

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
      return ClosureJsonConverter.AS_STRING_READER
    }
    public Closure<?> asDate(final String dateFormat) {
      return stringParser("Expected format: $dateFormat") { String str ->
        return DateTimeFormat.forPattern(dateFormat)
            .withLocale(Locale.US)
            .parseDateTime(str)
            .toDate()
      }
    }
    public Closure<?> parseString(Closure<?> parser) {
      return stringParser(null, parser)
    }

    private Closure<?> stringParser(String message, Closure<?> parser) {
      return { JsonReader input ->
        String str = (String) ClosureJsonConverter.AS_STRING_READER(input)
        if (str == null) { return null }
        try {
          return parser(str)
        } catch (IllegalArgumentException e) {
          throw new ConvertValueSyntaxException(str, message ? message : e.message)
        }
      }
    }
  }

  public class To {
    public Closure<?> asString() {
      return ClosureJsonConverter.AS_STRING_WRITER
    }
    public Closure<?> asDate(final String dateFormat) {
      return { JsonWriter input, Object value ->
        if (value == null) {
          input.nullValue()
          return
        }
        if (value instanceof Date) {
          input.value(DateTimeFormat.forPattern(dateFormat)
              .withLocale(Locale.US)
              .print(value.time))
          return
        }
        if (value instanceof String) {
          DateTimeFormat.forPattern(dateFormat).parseDateTime(value.toString()) // try to parse
          input.value((String)value)
          return
        }

        throw new IllegalArgumentException("Cannot interpret '$value' as date. Format: '$dateFormat'.")
      }
    }
    public Closure<?> formatToString(final Closure<String> formatter) {
      return { JsonWriter input, Object value ->
        if (value == null) {
          input.nullValue()
          return
        }
        input.value(formatter(value))
      }
    }

  }

}
